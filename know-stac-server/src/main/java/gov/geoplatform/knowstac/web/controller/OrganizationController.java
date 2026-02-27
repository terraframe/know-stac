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

import gov.geoplatform.knowstac.core.model.OrganizationResult;
import gov.geoplatform.knowstac.core.model.ResultPage;
import gov.geoplatform.knowstac.core.model.TreeNode;
import gov.geoplatform.knowstac.core.service.request.OrganizationResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import net.geoprism.registry.controller.RunwaySpringController;

@RestController
@Validated
@RequestMapping("/api/organization")
public class OrganizationController extends RunwaySpringController
{
  @Autowired
  private OrganizationResultService service;

  @ResponseBody
  @GetMapping("/get")
  @Operation( //
      summary = "Get an organization", //
      description = "Retrieves an organization in the system." //
  )
  public ResponseEntity<OrganizationResult> get( //
      @Parameter( //
          description = "Organization code", //
          example = "DOI") //
      @NotBlank @RequestParam(name = "code") String code) throws ParseException
  {
    OrganizationResult org = this.service.get(this.getSessionId(), code);

    return new ResponseEntity<OrganizationResult>(org, HttpStatus.OK);
  }

  @ResponseBody
  @GetMapping("/search")
  @Operation( //
      summary = "Get all organizations which contain the provided label", //
      description = "Retrieves a list of all registered organizations in the system which contain the label." //
  )
  public ResponseEntity<List<OrganizationResult>> search( //
      @Parameter( //
          description = "Text to restirct the organizations", //
          example = "Department", //
          required = false) //
      @NotBlank @RequestParam(name = "text", required = false, defaultValue = "") String text) throws ParseException
  {
    List<OrganizationResult> orgs = this.service.search(this.getSessionId(), text);

    return new ResponseEntity<List<OrganizationResult>>(orgs, HttpStatus.OK);
  }

  @GetMapping("/get-children")
  @Operation( //
      summary = "Pagination list of the sub organizations under the given organization", //
      description = "Retrieves a paginated list of the sub organizations under the given organization ." //
  )
  public ResponseEntity<ResultPage<OrganizationResult>> getChildren( //
      @Parameter( //
          description = "Universal organization code", //
          example = "DOI", //
          required = false) //
      @RequestParam(required = false, name = "code") String code, //
      @Parameter( //
          description = "Pagination page size", //
          example = "20", //
          required = false) //
      @RequestParam(name = "pageSize", required = false) Integer pageSize, //
      @Parameter( //
          description = "Pagination page number", //
          example = "1", //
          required = false) //
      @RequestParam(name = "pageNumber", required = false) Integer pageNumber)
  {
    ResultPage<OrganizationResult> page = this.service.getChildren(this.getSessionId(), code, pageSize, pageNumber);

    return ResponseEntity.ok(page);
  }

  @GetMapping("/get-ancestor-tree")
  @Operation( //
      summary = "Flatened list of the ancestor organizations for an organization", //
      description = "Retrieves a flatened list of the ancestor organizations of a sub organization." //
  )
  public ResponseEntity<TreeNode<OrganizationResult>> getAncestorTree( //
      @Parameter( //
          description = "Universal organization code of root level organization", //
          example = "DOI", //
          required = false) //
      @RequestParam(required = false, name = "rootCode") String rootCode, //
      @Parameter( //
          description = "Universal organization code of starting organization", //
          example = "USFS", //
          required = false) //
      @NotBlank @RequestParam(name = "code") String code, //
      @Parameter( //
          description = "Pagination page size", //
          example = "20", //
          required = false) //
      @RequestParam(required = false, name = "pageSize") Integer pageSize)
  {
    TreeNode<OrganizationResult> node = this.service.getAncestorTree(this.getSessionId(), rootCode, code, pageSize);

    return ResponseEntity.ok(node);
  }
}
