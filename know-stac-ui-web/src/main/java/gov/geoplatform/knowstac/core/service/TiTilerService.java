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
package gov.geoplatform.knowstac.core.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.geoplatform.knowstac.core.config.AppProperties;
import gov.geoplatform.knowstac.core.model.TiTilerStacAssetInfo;
import gov.geoplatform.knowstac.core.model.TiTilerStacBandStatistic;
import gov.geoplatform.knowstac.core.model.TiTilerStacInfo;
import gov.geoplatform.knowstac.core.model.TiTilerStacStatistics;
import gov.geoplatform.knowstac.core.model.TiTillerStacBandMetadata;

@Service
public class TiTilerService
{
  private static final String MULTISPECTRAL = "multispectral";

  private static final String URL           = "url";

  private static final String ASSETS        = "assets";

  // @GetMapping("/tiles/{tileMatrixSetId}/{z}/{x}/{y}[@{scale}x][.{format}]")
  private static final Logger logger        = LoggerFactory.getLogger(TiTilerService.class);

  @Autowired
  private AppProperties       properties;

  public CloseableHttpResponse tiles(Map<String, String> pathVarsMap, Map<String, String> params)
  {
    try
    {
      String url = properties.getTitilerHost() + "/stac/tiles";
      url += "/" + pathVarsMap.getOrDefault("tileMatrixSetId", "WebMercatorQuad");
      url += "/" + pathVarsMap.get("z");
      url += "/" + pathVarsMap.get("x");
      url += "/" + pathVarsMap.get("y");
      // url += "/" + pathVarsMap.getOrDefault("scale", "1");
      // url += "/" + pathVarsMap.getOrDefault("format", "None");

      HttpGet httpGet = new HttpGet(url);
      URIBuilder builder = new URIBuilder(httpGet.getURI());
      builder.setPort(properties.getTitilerPort());

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
      String url = properties.getTitilerHost() + "/stac";
      url += "/" + pathVarsMap.getOrDefault("tileMatrixSetId", "WebMercatorQuad");
      url += "/tilejson.json";

      HttpGet httpGet = new HttpGet(url);

      URIBuilder builder = new URIBuilder(httpGet.getURI());
      builder.setPort(properties.getTitilerPort());

      params.entrySet().stream() //
          .filter(entry -> !entry.getKey().equals(MULTISPECTRAL)) //
          .forEach(entry -> {
            builder.addParameter(entry.getKey(), entry.getValue());
          });

      if (params.containsKey(MULTISPECTRAL) && Boolean.valueOf(params.get(MULTISPECTRAL)))
      {
        this.calculateAndRescaleBands(builder, params);
      }

      httpGet.setURI(builder.build());

      try (CloseableHttpClient client = HttpClients.createDefault(); CloseableHttpResponse response = client.execute(httpGet);)
      {
        HttpEntity entity = response.getEntity();
        String value = EntityUtils.toString(entity, "UTF-8");

        return value.replaceAll(properties.getTitilerHost() + ":" + properties.getTitilerPort() + "/stac/tiles", properties.getServerUrl() + "api/tiles");
      }
    }
    catch (IOException | URISyntaxException e)
    {
      logger.error("Error proxying tile json", e);

      throw new UnsupportedOperationException(e);
    }
  }

  protected void calculateAndRescaleBands(URIBuilder builder, Map<String, String> params)
  {
    final String assetName = params.get(ASSETS);

    this.getStacInfo(params).ifPresent(info -> {
      TiTilerStacAssetInfo asset = info.getAsset(assetName);

      AtomicInteger redIdx = new AtomicInteger(asset.getColorinterp().indexOf("red"));
      AtomicInteger greenIdx = new AtomicInteger(asset.getColorinterp().indexOf("green"));
      AtomicInteger blueIdx = new AtomicInteger(asset.getColorinterp().indexOf("blue"));

      if (redIdx.intValue() != -1 && greenIdx.intValue() != -1 && blueIdx.intValue() != -1)
      {
        redIdx.incrementAndGet();
        greenIdx.incrementAndGet();
        blueIdx.incrementAndGet();

        this.getStacStatistics(params).ifPresent(stats -> {

          builder.addParameter("asset_bidx", assetName + "|" + String.valueOf(redIdx.intValue()) + "," + String.valueOf(greenIdx.intValue()) + "," + String.valueOf(blueIdx.intValue()));

          TiTillerStacBandMetadata redMetadata = asset.getBandMetadata().get(redIdx.get());
          TiTillerStacBandMetadata greenMetadata = asset.getBandMetadata().get(greenIdx.get());
          TiTillerStacBandMetadata blueMetadata = asset.getBandMetadata().get(blueIdx.get());

          TiTilerStacBandStatistic redStat = stats.getAssetBand(assetName + "_" + redMetadata.getName());
          TiTilerStacBandStatistic greenStat = stats.getAssetBand(assetName + "_" + greenMetadata.getName());
          TiTilerStacBandStatistic blueStat = stats.getAssetBand(assetName + "_" + blueMetadata.getName());

          Double min = Math.min(redStat.getMin(), Math.min(greenStat.getMin(), blueStat.getMin()));
          Double max = Math.max(redStat.getMax(), Math.max(greenStat.getMax(), blueStat.getMax()));

          // min = (min < 0) ? 0 : min; // TODO : No idea how the min value
          // could be negative. But it's happening on my sample data and it
          // doesn't render properly if it is.

          builder.addParameter("rescale", String.valueOf(min) + "," + String.valueOf(max));
        });
      }
    });
  }

  public Optional<TiTilerStacInfo> getStacInfo(Map<String, String> params)
  {
    try
    {
      if (params.containsKey(URL))
      {
        String url = properties.getTitilerHost() + "/stac/info";

        HttpGet httpGet = new HttpGet(url);

        URIBuilder builder = new URIBuilder(httpGet.getURI());
        builder.setPort(properties.getTitilerPort());
        builder.addParameter(URL, params.get(URL));
        builder.addParameter(ASSETS, params.get(ASSETS));

        httpGet.setURI(builder.build());

        try (CloseableHttpClient client = HttpClients.createDefault(); CloseableHttpResponse response = client.execute(httpGet);)
        {
          HttpEntity entity = response.getEntity();

          ObjectMapper mapper = new ObjectMapper();
          return Optional.ofNullable(mapper.readValue(entity.getContent(), TiTilerStacInfo.class));
        }
      }

      return Optional.empty();
    }
    catch (URISyntaxException | IOException e)
    {
      logger.error("Error getting cog bands", e);

      throw new UnsupportedOperationException(e);
    }
  }

  public Optional<TiTilerStacStatistics> getStacStatistics(Map<String, String> params)
  {
    try
    {
      if (params.containsKey(URL))
      {
        String url = properties.getTitilerHost() + "/stac/statistics";

        HttpGet httpGet = new HttpGet(url);

        URIBuilder builder = new URIBuilder(httpGet.getURI());
        builder.setPort(properties.getTitilerPort());
        builder.addParameter(URL, params.get(URL));
        builder.addParameter(ASSETS, params.get(ASSETS));

        httpGet.setURI(builder.build());

        try (CloseableHttpClient client = HttpClients.createDefault(); CloseableHttpResponse response = client.execute(httpGet);)
        {
          HttpEntity entity = response.getEntity();

          ObjectMapper mapper = new ObjectMapper();
          return Optional.ofNullable(mapper.readValue(entity.getContent(), TiTilerStacStatistics.class));
        }
      }

      return Optional.empty();
    }
    catch (URISyntaxException | IOException e)
    {
      logger.error("Error getting cog bands", e);

      throw new UnsupportedOperationException(e);
    }
  }

}
