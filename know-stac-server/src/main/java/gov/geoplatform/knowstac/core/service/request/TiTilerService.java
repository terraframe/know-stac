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
package gov.geoplatform.knowstac.core.service.request;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gov.geoplatform.knowstac.core.config.AppProperties;
import net.geoprism.configuration.GeoprismProperties;

@Service
public class TiTilerService
{
  // @GetMapping("/tiles/{tileMatrixSetId}/{z}/{x}/{y}[@{scale}x][.{format}]")
  private static final Logger logger = LoggerFactory.getLogger(TiTilerService.class);

  public CloseableHttpResponse tiles(Map<String, String> pathVarsMap, Map<String, String> params)
  {
    try
    {
      String url = AppProperties.getTitilerHost() + "/stac/tiles";
      url += "/" + pathVarsMap.getOrDefault("tileMatrixSetId", "WebMercatorQuad");
      url += "/" + pathVarsMap.get("z");
      url += "/" + pathVarsMap.get("x");
      url += "/" + pathVarsMap.get("y");
      // url += "/" + pathVarsMap.getOrDefault("scale", "1");
      // url += "/" + pathVarsMap.getOrDefault("format", "None");

      HttpGet httpGet = new HttpGet(url);
      URIBuilder builder = new URIBuilder(httpGet.getURI());
      builder.setPort(AppProperties.getTitilerPort());

      params.entrySet().forEach(entry -> {
        builder.addParameter(entry.getKey(), entry.getValue());
      });

      httpGet.setURI(builder.build());

      CloseableHttpClient client = HttpClients.createDefault();
      return client.execute(httpGet);
    }
    catch (IOException | URISyntaxException e)
    {
      logger.error("Error proxying tile", e);

      throw new UnsupportedOperationException(e);
    }
  }

  // @GetMapping("/tiles/{tileMatrixSetId}/tilejson.json")
  public String tilejson(Map<String, String> pathVarsMap, Map<String, String> params)
  {
    try
    {
      String url = AppProperties.getTitilerHost() + "/stac";
      url += "/" + pathVarsMap.getOrDefault("tileMatrixSetId", "WebMercatorQuad");
      url += "/tilejson.json";

      HttpGet httpGet = new HttpGet(url);

      URIBuilder builder = new URIBuilder(httpGet.getURI());
      builder.setPort(AppProperties.getTitilerPort());

      params.entrySet().forEach(entry -> {
        builder.addParameter(entry.getKey(), entry.getValue());
      });

      httpGet.setURI(builder.build());

      try (CloseableHttpClient client = HttpClients.createDefault(); CloseableHttpResponse response = client.execute(httpGet);)
      {
        HttpEntity entity = response.getEntity();
        String value = EntityUtils.toString(entity, "UTF-8");

        return value.replaceAll(AppProperties.getTitilerHost() + ":" + AppProperties.getTitilerPort() + "/stac/tiles", GeoprismProperties.getRemoteServerUrl() + "api/tiles");
      }
    }
    catch (IOException | URISyntaxException e)
    {
      logger.error("Error proxying tile json", e);

      throw new UnsupportedOperationException(e);
    }
  }
}
