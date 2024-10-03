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
package gov.geoplatform.knowstac.web.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import gov.geoplatform.knowstac.core.service.request.TiTilerService;

@RestController
@Validated
public class TiTillerController
{
  @Autowired
  private TiTilerService service;

  @GetMapping("/tiles/{tileMatrixSetId}/{z}/{x}/{y}@{scale}x")
  @ResponseBody
  public ResponseEntity<StreamingResponseBody> tiles(@PathVariable Map<String, String> pathVarsMap, @RequestParam Map<String, String> params) throws IOException
  {
    return proxy(this.service.tiles(pathVarsMap, params));
  }

  @GetMapping("/tiles/tilejson.json")
  @ResponseBody
  public ResponseEntity<String> tilejson(@PathVariable Map<String, String> pathVarsMap, @RequestParam Map<String, String> params) throws IOException
  {
    String json = this.service.tilejson(pathVarsMap, params);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    return new ResponseEntity<String>(json, headers, HttpStatus.OK);
  }

  private ResponseEntity<StreamingResponseBody> proxy(CloseableHttpResponse response) throws IOException
  {
    try
    {
      HttpHeaders headers = new HttpHeaders();

      Arrays.asList(response.getAllHeaders()).forEach(header -> {
        headers.set(header.getName(), header.getValue());
      });
      HttpEntity entity = response.getEntity();

      return new ResponseEntity<StreamingResponseBody>((StreamingResponseBody) outputStream -> {
        try
        {
          IOUtils.copy(entity.getContent(), outputStream);
        }
        finally
        {
          response.close();
        }
      }, headers, response.getStatusLine().getStatusCode());
    }
    catch (RuntimeException e)
    {
      response.close();

      throw e;
    }
  }

}
