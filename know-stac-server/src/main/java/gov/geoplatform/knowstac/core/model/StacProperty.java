package gov.geoplatform.knowstac.core.model;

import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StacProperty
{
  public static class Location
  {
    private String synchronizationId;

    // Remote Labeled Property Graph Entry
    private Date   forDate;

    // Remote Labeled Property Graph label
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
  private String       name;

  @NotBlank
  private String       label;

  @NotNull
  private PropertyType type;

  // Optional properties for location types
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
