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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.geoplatform.knowstac.core.model.LocationResult;
import gov.geoplatform.knowstac.core.model.ResultPage;
import gov.geoplatform.knowstac.core.model.TreeNode;
import gov.geoplatform.knowstac.core.service.request.LocationServiceIF;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import net.geoprism.registry.controller.RunwaySpringController;

@RestController
@Validated
@RequestMapping("/api/location")
public class LocationController extends RunwaySpringController
{
  @Autowired
  private LocationServiceIF service;

  @ResponseBody
  @GetMapping("/get")
  @Operation( //
      summary = "Get a location", //
      description = "Retrieves a single location in the system." //
  )
  public ResponseEntity<LocationResult> get( //
      @Parameter( //
          description = "Synchronization profile ID which loaded the location data", //
          example = "d6156b28-6f62-4408-8fb3-b4641e1e9dcc" //
      ) //
      @NotBlank @RequestParam(name = "synchronizationId") String synchronizationId, //
      @Parameter( //
          description = "Universal ID of the location to retrieve", //
          example = "597a2963-6f71-4373-87ec-ca3d332bb148" //
      ) //
      @NotBlank @RequestParam(name = "uid") String uid) throws ParseException
  {
    LocationResult location = this.service.get(this.getSessionId(), synchronizationId, uid);

    return new ResponseEntity<LocationResult>(location, HttpStatus.OK);
  }

  @ResponseBody
  @GetMapping("/search")
  @Operation( //
      summary = "Get all locations which contain the provided label", //
      description = "Retrieves a list of all registered locations in the system which contain the location label." //
  )
  public ResponseEntity<List<LocationResult>> search( //
      @Parameter( //
          description = "Synchronization profile ID which loaded the location data", //
          example = "d6156b28-6f62-4408-8fb3-b4641e1e9dcc" //
      ) //
      @NotBlank @RequestParam(name = "synchronizationId") String synchronizationId, //
      @Parameter( //
          description = "Location label in which to search", //
          example = "Colo", //
          required = false
      ) //
      @NotBlank @RequestParam(name = "text", required = false, defaultValue = "") String text) throws ParseException
  {
    List<LocationResult> locations = this.service.search(this.getSessionId(), synchronizationId, text);

    return new ResponseEntity<List<LocationResult>>(locations, HttpStatus.OK);
  }

  @GetMapping("/get-children")
  @Operation( //
      summary = "Pagination list of the children locations for a location", //
      description = "Retrieves a paginated list of the children locations of a location according to the synchronized hierarchy." //
  )
  public ResponseEntity<ResultPage<LocationResult>> getChildren( //
      @Parameter( //
          description = "Synchronization profile ID which loaded the location data", //
          example = "d6156b28-6f62-4408-8fb3-b4641e1e9dcc" //
      ) //
      @NotBlank @RequestParam(name = "synchronizationId") String synchronizationId, //
      @Parameter( //
          description = "Universal ID of the location to retrieve", //
          example = "597a2963-6f71-4373-87ec-ca3d332bb148" //
      ) //
      @RequestParam(name = "uid", required = false) String uid, //
      @Parameter( //
          description = "Pagination page size", //
          example = "20", //
          required = false
      ) //
      @RequestParam(name = "pageSize", required = false) Integer pageSize, //
      @Parameter( //
          description = "Pagination page number", //
          example = "1", //
          required = false
      ) //
      @RequestParam(name = "pageNumber", required = false) Integer pageNumber)
  {
    ResultPage<LocationResult> page = this.service.getChildren(this.getSessionId(), synchronizationId, uid, pageSize, pageNumber);

    return ResponseEntity.ok(page);
  }

  @GetMapping("/get-ancestor-tree")
  @Operation( //
      summary = "Flatened list of the ancestor locations for a location", //
      description = "Retrieves a flatened list of the ancestor locations of a location according to the synchronized hierarchy." //
  )
  public ResponseEntity<TreeNode<LocationResult>> getAncestorTree( //
      @Parameter( //
          description = "Synchronization profile ID which loaded the location data", //
          example = "d6156b28-6f62-4408-8fb3-b4641e1e9dcc" //
      ) //
      @NotBlank @RequestParam(name = "synchronizationId") String synchronizationId, //
      @Parameter( //
          description = "Universal ID of the ancestor location to stop at", //
          example = "597a2963-6f71-4373-87ec-ca3d332bb148", //
          required = false
      ) //
      @RequestParam(required = false, name = "rootUid") String rootUid, //
      @Parameter( //
          description = "Universal ID of the location to retrieve", //
          example = "597a2963-6f71-4373-87ec-ca3d332bb148" //
      ) //
      @NotBlank @RequestParam(name = "uid") String uid, //
      @Parameter( //
          description = "Page size to limit the results", //
          example = "10", //
          required = false
      ) //
      @RequestParam(required = false, name = "pageSize") Integer pageSize)
  {
    TreeNode<LocationResult> node = this.service.getAncestorTree(this.getSessionId(), synchronizationId, rootUid, uid, pageSize);

    return ResponseEntity.ok(node);
  }
}
