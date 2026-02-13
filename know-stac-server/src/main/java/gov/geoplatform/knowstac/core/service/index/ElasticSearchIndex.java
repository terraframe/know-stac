/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.knowstac.core.service.index;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.GeoShapeRelation;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping.Builder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import gov.geoplatform.knowstac.core.config.AppProperties;
import gov.geoplatform.knowstac.core.model.PropertyType;
import gov.geoplatform.knowstac.core.model.QueryCriteria;
import gov.geoplatform.knowstac.core.model.StacItem;
import gov.geoplatform.knowstac.core.model.StacProperty;
import gov.geoplatform.knowstac.core.service.business.StacPropertyBusinessService;

@Service
public class ElasticSearchIndex implements IndexIF, DisposableBean
{
  private static Logger               logger = LoggerFactory.getLogger(ElasticSearchIndex.class);

  @Autowired
  private StacPropertyBusinessService service;

  private RestClient                  restClient;

  @Override
  public boolean startup()
  {
    return true;
  }

  private synchronized RestClient getRestClient()
  {
    String username = AppProperties.getElasticsearchUsername();
    String host = AppProperties.getElasticsearchHost();
    String password = AppProperties.getElasticsearchPassword();
    int port = AppProperties.getElasticsearchPort();
    String schema = AppProperties.getElasticsearchSchema();

    if (this.restClient == null)
    {
      final int MAX_TRIES = 5;

      int count = 1;

      while (this.restClient == null && count < MAX_TRIES)
      {
        try
        {
          logger.debug("Attempting to check existence of elasticsearch");

          final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
          credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

          RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, schema)).setHttpClientConfigCallback(new HttpClientConfigCallback()
          {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder)
            {
              return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
          });

          this.restClient = builder.build();
        }
        catch (Exception e)
        {
          this.restClient = null;

          try
          {
            logger.debug("Waiting for Index to start. Attempt [" + count + "]");
            Thread.sleep(1000);
          }
          catch (InterruptedException ex)
          {
            // Ignore
          }

        }

        count++;
      }
    }

    if (this.restClient == null)
    {
      throw new ProgrammingErrorException("Unable to connect to elastic search");
    }

    return this.restClient;

  }

  @Override
  public void createIndex()
  {
    ElasticsearchClient client = this.createClient();

    try
    {
      try
      {
        client.indices().get(g -> g.index(AppProperties.getElasticsearchIndex()));
      }
      catch (ElasticsearchException e)
      {
        // Index doesn't exist, create it
        client.indices().create(i -> i.index(AppProperties.getElasticsearchIndex()).mappings(m -> {
          Builder builder = m.properties("geometry", p -> p.geoShape(v -> v));
          builder = builder.properties("assets", p -> p.object(v -> v.enabled(false)));

          List<StacProperty> properties = this.service.getAll();

          for (StacProperty property : properties)
          {
            if (property.getType().equals(PropertyType.DATE_TIME) || property.getType().equals(PropertyType.DATE))
            {
              builder = builder.properties("properties." + property.getName(), p -> p.date(v -> v));
            }
            else if (property.getType().equals(PropertyType.ORGANIZATION))
            {
              builder = builder.properties("properties." + property.getName() + ".label", p -> p.text(v -> v));
              builder = builder.properties("properties." + property.getName() + ".code", p -> p.text(v -> v));
            }
            else if (property.getType().equals(PropertyType.LOCATION))
            {
              builder = builder.properties("properties." + property.getName() + ".label", p -> p.text(v -> v));
              builder = builder.properties("properties." + property.getName() + ".uuid", p -> p.text(v -> v));
            }
            else if (property.getType().equals(PropertyType.NUMBER))
            {
              builder = builder.properties("properties." + property.getName(), p -> p.double_(v -> v));
            }
            else
            {
              builder = builder.properties("properties." + property.getName(), p -> p.text(v -> v));
            }
          }

          return builder;
        }));
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException("Unable to create STAC index", e);
    }
  }

  @Override
  public synchronized void shutdown()
  {
    try
    {
      if (this.restClient != null)
      {
        this.restClient.close();

        this.restClient = null;
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  private ElasticsearchClient createClient()
  {
    // Create the transport with a Jackson mapper
    ElasticsearchTransport transport = new RestClientTransport(this.getRestClient(), new JacksonJsonpMapper());

    // And create the API client
    return new ElasticsearchClient(transport);
  }

  @Override
  public void clear()
  {
    try
    {
      ElasticsearchClient client = createClient();
      client.indices().delete(i -> i.index(AppProperties.getElasticsearchIndex()));

      this.shutdown();
    }
    catch (Exception e)
    {
      // Ignore
    }
  }

  @Override
  public void put(StacItem item)
  {
    if (item != null)
    {
      try
      {
        ElasticsearchClient client = createClient();

        client.index(i -> i.index(AppProperties.getElasticsearchIndex()).id(item.getId()).document(item));
      }
      catch (IOException e)
      {
        throw new ProgrammingErrorException(e);
      }

    }

    // Due to the fact that the thumbnail endpoints are in the private s3 bucket
    // we must remove the thumbnail asset from the STAC json uploaded to the
    // buckets because we do not want public STAC items to contain urls to
    // private files. However, the front-end uses the thumbnail information on
    // the search results panel. As such, we still need the thumbnail asset in
    // the index.
    item.removeAsset("thumbnail");
  }

  @Override
  public void removeStacItem(String id)
  {
    try
    {
      ElasticsearchClient client = createClient();
      client.deleteByQuery(new DeleteByQueryRequest.Builder() //
          .index(AppProperties.getElasticsearchIndex()) //
          .query(q -> q.match(m -> m.field("id").query(id))).build());
    }
    catch (ElasticsearchException e)
    {
      logger.error("Elasticsearch error", e);
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  public Optional<StacItem> getItem(String id)
  {
    try
    {
      ElasticsearchClient client = createClient();

      SearchRequest.Builder s = new SearchRequest.Builder();
      s.index(AppProperties.getElasticsearchIndex());
      s.query(q -> q.match(m -> m.field("id").query(id)));

      SearchRequest request = s.build();

      SearchResponse<StacItem> search = client.search(request, StacItem.class);
      HitsMetadata<StacItem> hits = search.hits();

      for (Hit<StacItem> hit : hits.hits())
      {
        return Optional.ofNullable(hit.source());
      }

    }
    catch (ElasticsearchException e)
    {
      logger.error("Elasticsearch error", e);
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }

    return Optional.empty();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<StacItem> getItems(QueryCriteria criteria)
  {
    List<StacItem> items = new LinkedList<StacItem>();

    try
    {
      ElasticsearchClient client = createClient();

      SearchRequest.Builder s = new SearchRequest.Builder();
      s.index(AppProperties.getElasticsearchIndex());
      s.size(1000);

      // s.size(pageSize);
      // s.from(pageSize * ( pageNumber - 1 ));

      if (criteria.hasConditions())
      {
        Map<String, StacProperty> properties = this.service.getAll().stream().collect(Collectors.toMap(p -> p.getName(), p -> p));

        s.query(q -> q.bool(b -> {
          Map<String, Object> propertyValues = criteria.getProperties();

          if (propertyValues.size() > 0)
          {
            List<Query> conditions = new LinkedList<Query>();

            propertyValues.forEach((name, value) -> {

              StacProperty property = properties.get(name);

              if (property != null)
              {
                if (property.getType().equals(PropertyType.DATE) || property.getType().equals(PropertyType.DATE_TIME))
                {
                  Map<String, String> range = (Map<String, String>) value;
                  conditions.add(new Query.Builder().range(d -> d.date(r -> {
                    r.field("properties." + name);

                    if (!StringUtils.isBlank(range.get("startDate")))
                    {
                      r.gte(range.get("startDate"));
                    }

                    if (!StringUtils.isBlank(range.get("endDate")))
                    {
                      r.lte(range.get("endDate"));
                    }

                    return r;
                  })).build());
                }
                else if (property.getType().equals(PropertyType.LOCATION))
                {
                  conditions.add(new Query.Builder().queryString(qs -> qs.fields("properties." + name + ".uuid").query(value.toString())).build());
                }
                else if (property.getType().equals(PropertyType.ORGANIZATION))
                {
                  conditions.add(new Query.Builder().queryString(qs -> qs.fields("properties." + name + ".code").query(value.toString())).build());
                }
                else
                {
                  conditions.add(new Query.Builder().queryString(qs -> qs.fields("properties." + name).query(value.toString())).build());
                }
              }
            });

            b.filter(conditions);
          }
          else
          {
            b.must(m -> m.matchAll(ma -> ma.boost(0F)));
          }

          Envelope envelope = criteria.getBbox();
          if (envelope != null)
          {
            List<Query> conditions = new LinkedList<Query>();

            GeometryFactory factory = new GeometryFactory();
            Geometry geometry = factory.toGeometry(envelope);
            WKTWriter writer = new WKTWriter();

            conditions.add(new Query.Builder() //
                .geoShape(qs -> qs.boost(10F) //
                    .field("geometry") //
                    .shape(bb -> bb.relation(GeoShapeRelation.Intersects) //
                        .shape(JsonData.of(writer.write(geometry)))))
                .build());

            b.must(conditions);
          }

          return b;
        }));

      }

      SearchRequest request = s.build();

      SearchResponse<StacItem> search = client.search(request, StacItem.class);
      HitsMetadata<StacItem> hits = search.hits();

      // page.setCount(hits.total().value());

      for (Hit<StacItem> hit : hits.hits())
      {
        items.add(hit.source());
      }

      if (!criteria.hasConditions())
      {
        items.sort((a, b) -> {
          if (a.getCollection() != null && b.getCollection() != null)
          {
            return a.getCollection().compareTo(b.getCollection());
          }

          return 1;
        });
      }
    }
    catch (ElasticsearchException e)
    {
      logger.error("Elasticsearch error", e);
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }

    return items;
  }

  @Override
  public List<StacItem> getItems(Map<String, String> params)
  {
    List<StacItem> items = new LinkedList<StacItem>();

    try
    {
      ElasticsearchClient client = createClient();

      SearchRequest.Builder s = new SearchRequest.Builder();
      s.index(AppProperties.getElasticsearchIndex());

      // s.size(pageSize);
      // s.from(pageSize * ( pageNumber - 1 ));

      if (params.size() > 0)
      {
        Map<String, StacProperty> properties = this.service.getAll().stream().collect(Collectors.toMap(p -> p.getName(), p -> p));

        s.query(q -> q.bool(b -> {
          if (params.size() > 0)
          {
            List<Query> conditions = new LinkedList<Query>();

            params.forEach((name, value) -> {

              StacProperty property = properties.get(name);

              if (property != null)
              {
                if (property.getType().equals(PropertyType.DATE) || property.getType().equals(PropertyType.DATE_TIME))
                {
                  // Map<String, String> range = (Map<String, String>) value;
                  // conditions.add(new Query.Builder().range(r -> {
                  // r.field("properties." + name);
                  //
                  // if (!StringUtils.isBlank(range.get("startDate")))
                  // {
                  // r.gte(JsonData.of(range.get("startDate")));
                  // }
                  //
                  // if (!StringUtils.isBlank(range.get("endDate")))
                  // {
                  // r.lte(JsonData.of(range.get("endDate")));
                  // }
                  //
                  // return r;
                  // }).build());
                }
                else if (property.getType().equals(PropertyType.LOCATION))
                {
                  conditions.add(new Query.Builder().queryString(qs -> qs.fields("properties." + name + ".uuid").query(value.toString())).build());
                }
                else if (property.getType().equals(PropertyType.ORGANIZATION))
                {
                  conditions.add(new Query.Builder().queryString(qs -> qs.fields("properties." + name + ".code").query(value.toString())).build());
                }
                else
                {
                  conditions.add(new Query.Builder().queryString(qs -> qs.fields("properties." + name).query(value.toString())).build());
                }
              }
            });

            b.filter(conditions);
          }
          else
          {
            b.must(m -> m.matchAll(ma -> ma.boost(0F)));
          }

          // Envelope envelope = criteria.getBbox();
          // if (envelope != null)
          // {
          // List<Query> conditions = new LinkedList<Query>();
          //
          // GeometryFactory factory = new GeometryFactory();
          // Geometry geometry = factory.toGeometry(envelope);
          // WKTWriter writer = new WKTWriter();
          //
          // conditions.add(new Query.Builder().geoShape(qs ->
          // qs.boost(10F).field("geometry").shape(bb ->
          // bb.relation(GeoShapeRelation.Intersects).shape(JsonData.of(writer.write(geometry))))).build());
          //
          // b.must(conditions);
          // }

          return b;
        }));

      }

      SearchRequest request = s.build();

      SearchResponse<StacItem> search = client.search(request, StacItem.class);
      HitsMetadata<StacItem> hits = search.hits();

      // page.setCount(hits.total().value());

      for (Hit<StacItem> hit : hits.hits())
      {
        items.add(hit.source());
      }
    }
    catch (ElasticsearchException e)
    {
      logger.error("Elasticsearch error", e);
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }

    return items;
  }

  @Override
  public Set<String> values(String field, String text)
  {
    Set<String> items = new TreeSet<String>();

    try
    {
      ElasticsearchClient client = createClient();

      SearchRequest.Builder s = new SearchRequest.Builder();
      s.index(AppProperties.getElasticsearchIndex());
      s.query(q -> q.wildcard(m -> m.field("properties." + field).value("*" + text.toLowerCase() + "*")));
      s.source(so -> so.filter(f -> f.includes("properties." + field)));

      SearchRequest request = s.build();

      SearchResponse<StacItem> search = client.search(request, StacItem.class);
      HitsMetadata<StacItem> hits = search.hits();

      // page.setCount(hits.total().value());

      for (Hit<StacItem> hit : hits.hits())
      {
        hit.source().getProperty(field).ifPresent(value -> {
          items.add((String) value);
        });
      }
    }
    catch (ElasticsearchException e)
    {
      logger.error("Elasticsearch error", e);
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }

    return items;
  }

  @Override
  public void destroy() throws Exception
  {
    this.shutdown();
  }
}
