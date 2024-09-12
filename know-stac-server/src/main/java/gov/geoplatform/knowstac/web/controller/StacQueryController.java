package gov.geoplatform.knowstac.web.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.geoplatform.knowstac.core.model.QueryCriteria;
import gov.geoplatform.knowstac.core.model.StacCollection;
import gov.geoplatform.knowstac.core.service.request.StacQueryService;

@RestController
@Validated
public class StacQueryController extends RunwaySpringController
{
  @Autowired
  private StacQueryService service;

  @GetMapping("query/collection")
  public ResponseEntity<StacCollection> collection(@RequestParam(name = "criteria", required = false) QueryCriteria criteria)
  {
    StacCollection response = this.service.collection(getSessionId(), criteria);

    return new ResponseEntity<StacCollection>(response, HttpStatus.OK);
  }

  @GetMapping("query/search")
  public ResponseEntity<StacCollection> search(@RequestParam Map<String, String> params)
  {
    StacCollection response = this.service.search(getSessionId(), params);

    return new ResponseEntity<StacCollection>(response, HttpStatus.OK);
  }
}