package gov.geoplatform.knowstac.web.controller;

import java.io.IOException;
import java.util.Set;

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
import gov.geoplatform.knowstac.core.service.request.AccessControlService;
import gov.geoplatform.knowstac.core.service.request.StacItemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@Validated
public class StacItemController extends RunwaySpringController
{
  public static class URLBody
  {
    @NotBlank
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
  private StacItemService      service;

  @Autowired
  private AccessControlService accessControl;

  @PostMapping("item/put")
  public ResponseEntity<StacItem> put(@Valid @RequestBody StacItem item, HttpServletRequest request) throws IOException
  {
    if (!accessControl.hasAccess(this.getClientIpAddress(request)))
    {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    StacItem response = this.service.put(getSessionId(), item);

    return new ResponseEntity<StacItem>(response, HttpStatus.OK);
  }

  @PostMapping("item/put-url")
  public ResponseEntity<StacItem> putUrl(@Valid @RequestBody URLBody body, HttpServletRequest request) throws IOException
  {
    if (!accessControl.hasAccess(this.getClientIpAddress(request)))
    {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    StacItem response = this.service.putUrl(getSessionId(), body.getUrl());

    return new ResponseEntity<StacItem>(response, HttpStatus.OK);
  }

  @PostMapping("item/remove")
  public ResponseEntity<Void> remove(@RequestParam(name = "id", required = true) String id, HttpServletRequest request) throws IOException
  {
    if (!accessControl.hasAccess(request.getRemoteAddr()))
    {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

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

  private String getClientIpAddress(HttpServletRequest request)
  {
    String[] headersToCheck = { "X-Forwarded-For", "X-Real-IP" };

    for (String header : headersToCheck)
    {
      String ipList = request.getHeader(header);

      if (ipList != null && ipList.length() != 0 && !"unknown".equalsIgnoreCase(ipList))
      {
        // X-Forwarded-For can contain multiple IPs, first is the real client
        return ipList.split(",")[0].trim();
      }
    }

    // Fallback to direct remote address
    return request.getRemoteAddr();
  }

}