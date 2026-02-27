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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.geoplatform.knowstac.core.model.InvalidAccessException;
import gov.geoplatform.knowstac.core.model.StacItem;
import gov.geoplatform.knowstac.core.service.request.AccessControlService;
import gov.geoplatform.knowstac.core.service.request.StacItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@Validated
@RequestMapping("/api/item")
public class StacItemController extends RunwaySpringController
{
  public static class URLBody
  {
    @NotBlank
    @Schema( //
        description = "URL of Stac Item", //
        example = "https://127.0.0.1/item.json" //
    )
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

  @PostMapping("/put")
  @Operation( //
      summary = "Put STAC item", //
      description = "Puts the STAC item into the system and indexes it for querying" //
  )
  public ResponseEntity<StacItem> put( //
      @io.swagger.v3.oas.annotations.parameters.RequestBody( //
          description = "Stac Item to put", //
          required = true, //
          content = @Content(mediaType = "application/json", //
              schema = @Schema(implementation = StacItem.class) //
          )) //
      @Valid @RequestBody StacItem item, HttpServletRequest request) throws IOException
  {
    if (!accessControl.hasAccess(this.getClientIpAddress(request)))
    {
      throw new InvalidAccessException();
    }

    StacItem response = this.service.put(getSessionId(), item);

    return new ResponseEntity<StacItem>(response, HttpStatus.OK);
  }

  @PostMapping("/put-url")
  @Operation( //
      summary = "Put a STAC item from a URL", //
      description = "Puts the STAC item defined at the given URL into the system and indexes it for querying" //
  )
  public ResponseEntity<StacItem> putUrl( //
      @io.swagger.v3.oas.annotations.parameters.RequestBody( //
          description = "URL of Stac Item to ingest", //
          required = true, //
          content = @Content(mediaType = "application/json", //
              schema = @Schema(implementation = URLBody.class) //
          )) //
      @Valid @RequestBody URLBody body, HttpServletRequest request) throws IOException
  {
    if (!accessControl.hasAccess(this.getClientIpAddress(request)))
    {
      throw new InvalidAccessException();
    }

    StacItem response = this.service.putUrl(getSessionId(), body.getUrl());

    return new ResponseEntity<StacItem>(response, HttpStatus.OK);
  }

  @PostMapping("/remove")
  @Operation( //
      summary = "Removes a STAC item", //
      description = "Removes the STAC item from the system" //
  )
  public ResponseEntity<Void> remove( //
      @Parameter( //
          description = "Id of the Stac item to remove", //
          example = "item-1" //
      ) //
      @RequestParam(name = "id", required = true) String id, HttpServletRequest request) throws IOException
  {
    if (!accessControl.hasAccess(this.getClientIpAddress(request)))
    {
      throw new InvalidAccessException();
    }

    this.service.remove(getSessionId(), id);

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @GetMapping("/get")
  @Operation( //
      summary = "Get a STAC item", //
      description = "Retrieves a STAC item from the system" //
  )
  public ResponseEntity<StacItem> get( //
      @Parameter( //
          description = "Id of the Stac item to retrieve", //
          example = "item-1" //
      ) //
      @RequestParam(name = "id", required = true) String id) throws IOException
  {
    StacItem response = this.service.get(getSessionId(), id);

    return new ResponseEntity<StacItem>(response, HttpStatus.OK);
  }

  @GetMapping("/values")
  @Operation( //
      summary = "Get distinct values", //
      description = "Retrieves a list of distinct values for the given STAC property" //
  )
  public ResponseEntity<Set<String>> values( //
      @Parameter( //
          description = "name of the registered Stac property to get the distinct values", //
          example = "mission", //
          required = false
      ) //
      @RequestParam(name = "field", required = false) String field, //
      @Parameter( //
          description = "Value text to restrict the results", //
          example = "Spring", //
          required = false
      ) //
      @RequestParam(name = "text", required = false) String text) throws IOException
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