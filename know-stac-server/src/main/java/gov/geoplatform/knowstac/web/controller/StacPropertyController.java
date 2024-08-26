package gov.geoplatform.knowstac.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.geoplatform.knowstac.core.model.StacProperty;
import gov.geoplatform.knowstac.core.service.request.StacPropertyService;

@RestController
@Validated
public class StacPropertyController extends RunwaySpringController
{
  @Autowired
  private StacPropertyService service;

  @GetMapping("stac-property/get-all")
  public ResponseEntity<List<StacProperty>> get()
  {
    List<StacProperty> response = this.service.getAll(getSessionId());

    return new ResponseEntity<List<StacProperty>>(response, HttpStatus.OK);
  }

}