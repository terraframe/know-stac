package gov.geoplatform.knowstac.web.controller;

import java.io.IOException;
import java.util.Set;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;
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
  public static class URLBody
  {
    @NotEmpty
    private String url;

    public String getUrl()
    {
      return url;
    }

    public void setUrl(String url)
    {
      this.url = url;
    }
  }

  @Autowired
  private StacItemService service;

  @PostMapping("item/put")
  public ResponseEntity<StacItem> put(@Valid @RequestBody StacItem item) throws IOException
  {
    StacItem response = this.service.put(getSessionId(), item);

    return new ResponseEntity<StacItem>(response, HttpStatus.OK);
  }

  @PostMapping("item/put-url")
  public ResponseEntity<StacItem> putUrl(@Valid @RequestBody URLBody body) throws IOException
  {
    StacItem response = this.service.putUrl(getSessionId(), body.getUrl());

    return new ResponseEntity<StacItem>(response, HttpStatus.OK);
  }

  @PostMapping("item/remove")
  public ResponseEntity<Void> remove(@RequestParam(name = "id", required = true) String id) throws IOException
  {
    this.service.remove(getSessionId(), id);

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @GetMapping("item/get")
  public ResponseEntity<StacItem> get(@RequestParam(name = "id", required = true) String id) throws IOException
  {
    StacItem response = this.service.get(getSessionId(), id);

    return new ResponseEntity<StacItem>(response, HttpStatus.OK);
  }

  @GetMapping("item/values")
  public ResponseEntity<Set<String>> values(@RequestParam(name = "field", required = false) String field, @RequestParam(name = "text", required = false) String text) throws IOException
  {
    Set<String> response = this.service.values(getSessionId(), field, text);

    return new ResponseEntity<Set<String>>(response, HttpStatus.OK);
  }

}