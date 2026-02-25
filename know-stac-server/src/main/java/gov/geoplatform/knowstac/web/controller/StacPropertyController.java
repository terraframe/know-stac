package gov.geoplatform.knowstac.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.geoplatform.knowstac.core.model.StacProperty;
import gov.geoplatform.knowstac.core.service.request.StacPropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;

@RestController()
@Validated
@RequestMapping("/api/stac-property")
public class StacPropertyController extends RunwaySpringController
{
  @Autowired
  private StacPropertyService service;

  @GetMapping("/get-all")
  @Operation( //
      summary = "Get all properties", //
      description = "Retrieves a list of all registered properties in the system" //
  )  
  public ResponseEntity<List<StacProperty>> get()
  {
    List<StacProperty> response = this.service.getAll(getSessionId());

    return new ResponseEntity<List<StacProperty>>(response, HttpStatus.OK);
  }

  @GetMapping("/get-for-organization")
  public ResponseEntity<List<StacProperty>> getForOrganization( //
      @Parameter( //
          description = "Returns a list of location properties which use locations that have been synchronized for the given organization", //
          example = "DOI" //
      ) //
      @NotBlank @RequestParam(name = "code") String code)
  {
    List<StacProperty> response = this.service.getForOrganization(getSessionId(), code);

    return new ResponseEntity<List<StacProperty>>(response, HttpStatus.OK);
  }

}