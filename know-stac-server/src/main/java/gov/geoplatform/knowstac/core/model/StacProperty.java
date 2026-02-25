package gov.geoplatform.knowstac.core.model;

import java.util.Date;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StacProperty
{
  public static class Location
  {
    @Schema( //
        description = "Synchronization profile ID which loaded the location data", //
        requiredMode = RequiredMode.REQUIRED, //
        example = "d6156b28-6f62-4408-8fb3-b4641e1e9dcc" //
    )
    private String synchronizationId;

    // Remote Labeled Property Graph Entry
    @Schema( //
        description = "Period of validaity date for the location", //
        requiredMode = RequiredMode.REQUIRED, //
        example = "2021-01-01" //
    )
    private Date   forDate;

    // Remote Labeled Property Graph label
    @Schema( //
        description = "Location label", //
        requiredMode = RequiredMode.REQUIRED, //
        example = "Colorado" //
    )
    private String label;

    public String getSynchronizationId()
    {
      return synchronizationId;
    }

    public void setSynchronizationId(String synchronizationId)
    {
      this.synchronizationId = synchronizationId;
    }

    public Date getForDate()
    {
      return forDate;
    }

    public void setForDate(Date forDate)
    {
      this.forDate = forDate;
    }

    public String getLabel()
    {
      return label;
    }

    public void setLabel(String label)
    {
      this.label = label;
    }

    public static Location build(String synchronizationId, Date forDate, String label)
    {
      Location location = new Location();
      location.setSynchronizationId(synchronizationId);
      location.setForDate(forDate);
      location.setLabel(label);

      return location;
    }

  }

  @NotBlank
  @Schema( //
      description = "Property name", //
      requiredMode = RequiredMode.REQUIRED, //
      example = "location" //
  )
  private String       name;

  @NotBlank
  @Schema( //
      description = "Property label", //
      requiredMode = RequiredMode.REQUIRED, //
      example = "Operational Location" //
  )
  private String       label;

  @NotNull
  @Schema( //
      description = "Type of property.  Must be one of: STRING, DATE, DATE_TIME, NUMBER, LOCATION, ENUMERATION, ORGANIZATION", //
      requiredMode = RequiredMode.REQUIRED, //
      example = "LOCATION" //
  )
  private PropertyType type;

  // Optional properties for location types
  @Schema( //
      description = "Optional root location for a location attributes", //
      requiredMode = RequiredMode.NOT_REQUIRED //
  )
  private Location     location;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public PropertyType getType()
  {
    return type;
  }

  public void setType(PropertyType type)
  {
    this.type = type;
  }

  public Location getLocation()
  {
    return location;
  }

  public void setLocation(Location location)
  {
    this.location = location;
  }

  public static StacProperty build(String name, String label, PropertyType type)
  {
    StacProperty property = new StacProperty();
    property.setName(name);
    property.setLabel(label);
    property.setType(type);

    return property;
  }

  public static StacProperty build(String name, String label, PropertyType type, Location location)
  {
    StacProperty property = new StacProperty();
    property.setName(name);
    property.setLabel(label);
    property.setType(type);
    property.setLocation(location);

    return property;
  }
}
