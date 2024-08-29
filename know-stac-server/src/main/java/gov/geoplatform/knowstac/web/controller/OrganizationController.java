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

import gov.geoplatform.knowstac.core.model.OrganizationResult;
import gov.geoplatform.knowstac.core.service.request.OrganizationResultService;
import net.geoprism.registry.controller.RunwaySpringController;

@RestController
@Validated
public class OrganizationController extends RunwaySpringController
{
  public static final String  API_PATH = "organization";

  @Autowired
  private OrganizationResultService service;

  @ResponseBody
  @GetMapping(API_PATH + "/get")
  public ResponseEntity<OrganizationResult> get(@NotEmpty @RequestParam String code) throws ParseException
  {
    OrganizationResult org = this.service.get(this.getSessionId(), code);

    return new ResponseEntity<OrganizationResult>(org, HttpStatus.OK);
  }

  @ResponseBody
  @GetMapping(API_PATH + "/search")
  public ResponseEntity<List<OrganizationResult>> search(@NotEmpty @RequestParam String text) throws ParseException
  {
    List<OrganizationResult> orgs = this.service.search(this.getSessionId(), text);

    return new ResponseEntity<List<OrganizationResult>>(orgs, HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-children")
  public ResponseEntity<String> getChildren(@RequestParam(required = false) String code, @RequestParam(required = false) Integer pageSize, @RequestParam(required = false) Integer pageNumber)
  {
    JsonObject page = this.service.getChildren(this.getSessionId(), code, pageSize, pageNumber);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-ancestor-tree")
  public ResponseEntity<String> getAncestorTree(@RequestParam(required = false) String rootCode, @NotEmpty @RequestParam String code, @RequestParam(required = false) Integer pageSize)
  {
    JsonObject page = this.service.getAncestorTree(this.getSessionId(), rootCode, code, pageSize);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }
}
