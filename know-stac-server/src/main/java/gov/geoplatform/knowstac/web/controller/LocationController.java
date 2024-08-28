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

import java.text.ParseException;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;

import gov.geoplatform.knowstac.core.model.LocationResult;
import gov.geoplatform.knowstac.core.service.request.LocationServiceIF;
import net.geoprism.registry.controller.RunwaySpringController;

@RestController
@Validated
public class LocationController extends RunwaySpringController
{
  public static final String API_PATH = "location";

  @Autowired
  private LocationServiceIF  service;

  @ResponseBody
  @GetMapping(API_PATH + "/get")
  public ResponseEntity<LocationResult> get(@NotEmpty @RequestParam String synchronizationId, @NotEmpty @RequestParam String uuid) throws ParseException
  {
    LocationResult location = this.service.get(this.getSessionId(), synchronizationId, uuid);

    return new ResponseEntity<LocationResult>(location, HttpStatus.OK);
  }

  @ResponseBody
  @GetMapping(API_PATH + "/search")
  public ResponseEntity<List<LocationResult>> search(@NotEmpty @RequestParam String synchronizationId, @NotEmpty @RequestParam String text) throws ParseException
  {
    List<LocationResult> locations = this.service.search(this.getSessionId(), synchronizationId, text);

    return new ResponseEntity<List<LocationResult>>(locations, HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-children")
  public ResponseEntity<String> getChildren(@NotEmpty @RequestParam String synchronizationId, @RequestParam(required = false) String uuid, @RequestParam(required = false) Integer pageSize, @RequestParam(required = false) Integer pageNumber)
  {
    JsonObject page = this.service.getChildren(this.getSessionId(), synchronizationId, uuid, pageSize, pageNumber);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-ancestor-tree")
  public ResponseEntity<String> getAncestorTree(@NotEmpty @RequestParam String synchronizationId, @RequestParam(required = false) String rootUuid, @NotEmpty @RequestParam String uuid, @RequestParam(required = false) Integer pageSize)
  {
    JsonObject page = this.service.getAncestorTree(this.getSessionId(), synchronizationId, rootUuid, uuid, pageSize);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }
}
