package gov.geoplatform.knowstac.web.controller;

import java.io.IOException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.geoplatform.knowstac.core.model.StacItem;
import gov.geoplatform.knowstac.core.service.request.StacItemService;

@RestController
@Validated
public class StacItemController extends RunwaySpringController
{
  @Autowired
  private StacItemService service;

  @PostMapping("stac-item/add")
  public ResponseEntity<StacItem> add(@Valid @RequestBody StacItem item) throws IOException
  {
    StacItem response = this.service.add(getSessionId(), item);

    return new ResponseEntity<StacItem>(response, HttpStatus.OK);
  }

  @GetMapping("stac-item/get")
  public ResponseEntity<StacItem> get(@RequestParam(name = "oid", required = false) String oid) throws IOException
  {
    StacItem response = this.service.get(getSessionId(), oid);

    return new ResponseEntity<StacItem>(response, HttpStatus.OK);
  }

}