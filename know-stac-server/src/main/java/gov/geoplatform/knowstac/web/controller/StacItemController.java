package gov.geoplatform.knowstac.web.controller;

import java.io.IOException;
import java.util.List;

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

  @PostMapping("item/put")
  public ResponseEntity<StacItem> put(@Valid @RequestBody StacItem item) throws IOException
  {
    StacItem response = this.service.put(getSessionId(), item);

    return new ResponseEntity<StacItem>(response, HttpStatus.OK);
  }

  @PostMapping("item/remove")
  public ResponseEntity<Void> put(@RequestParam(name = "id", required = false) String id) throws IOException
  {
    this.service.remove(getSessionId(), id);

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @GetMapping("item/get")
  public ResponseEntity<StacItem> get(@RequestParam(name = "id", required = false) String id) throws IOException
  {
    StacItem response = this.service.get(getSessionId(), id);

    return new ResponseEntity<StacItem>(response, HttpStatus.OK);
  }

  @GetMapping("item/values")
  public ResponseEntity<List<String>> values(@RequestParam(name = "field", required = false) String field, @RequestParam(name = "text", required = false) String text) throws IOException
  {
    List<String> response = this.service.values(getSessionId(), field, text);

    return new ResponseEntity<List<String>>(response, HttpStatus.OK);
  }

}