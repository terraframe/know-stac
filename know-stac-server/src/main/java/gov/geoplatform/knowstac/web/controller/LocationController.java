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
import jakarta.validation.constraints.NotBlank;
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
  public ResponseEntity<LocationResult> get(@NotBlank @RequestParam(name = "synchronizationId") String synchronizationId, @NotBlank @RequestParam(name = "uid") String uid) throws ParseException
  {
    LocationResult location = this.service.get(this.getSessionId(), synchronizationId, uid);

    return new ResponseEntity<LocationResult>(location, HttpStatus.OK);
  }

  @ResponseBody
  @GetMapping(API_PATH + "/search")
  public ResponseEntity<List<LocationResult>> search(@NotBlank @RequestParam(name = "synchronizationId") String synchronizationId, @NotBlank @RequestParam(name = "text") String text) throws ParseException
  {
    List<LocationResult> locations = this.service.search(this.getSessionId(), synchronizationId, text);

    return new ResponseEntity<List<LocationResult>>(locations, HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-children")
  public ResponseEntity<String> getChildren( //
      @NotBlank @RequestParam(name = "synchronizationId") String synchronizationId, //
      @NotBlank @RequestParam(name = "uid", required = false) String uid, //
      @RequestParam(name = "pageSize", required = false) Integer pageSize, //
      @RequestParam(name = "pageNumber", required = false) Integer pageNumber)
  {
    JsonObject page = this.service.getChildren(this.getSessionId(), synchronizationId, uid, pageSize, pageNumber);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-ancestor-tree")
  public ResponseEntity<String> getAncestorTree( //
      @NotBlank @RequestParam(name = "synchronizationId") String synchronizationId, //
      @RequestParam(required = false, name = "rootUid") String rootUid, //
      @NotBlank @RequestParam(name = "uid") String uid, //
      @RequestParam(required = false, name = "pageSize") Integer pageSize)
  {
    JsonObject page = this.service.getAncestorTree(this.getSessionId(), synchronizationId, rootUid, uid, pageSize);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }
}
