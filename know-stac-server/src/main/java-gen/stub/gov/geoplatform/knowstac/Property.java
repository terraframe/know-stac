package gov.geoplatform.knowstac;

import org.apache.commons.lang.StringUtils;

import gov.geoplatform.knowstac.core.model.PropertyType;
import gov.geoplatform.knowstac.core.model.StacProperty;
import gov.geoplatform.knowstac.core.model.StacProperty.Location;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;

public class Property extends PropertyBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -2073826874;

  public Property()
  {
    super();
  }

  public StacProperty toDTO()
  {
    if (!StringUtils.isBlank(this.getSynchronizationOid()))
    {

      LabeledPropertyGraphSynchronization synchronization = this.getSynchronization();

      Location location = Location.build(synchronization.getOid(), synchronization.getForDate(), synchronization.getDisplayLabel().getValue());

      return StacProperty.build(this.getPropertyName(), this.getLabel(), PropertyType.valueOf(this.getPropertyType()), location);
    }

    return StacProperty.build(this.getPropertyName(), this.getLabel(), PropertyType.valueOf(this.getPropertyType()));
  }

  public static Property create(String name, String label, PropertyType type, LabeledPropertyGraphSynchronization synchronization)
  {
    Property property = new Property();
    property.setPropertyName(name);
    property.setLabel(label);
    property.setPropertyType(type.name());
    property.setSynchronization(synchronization);
    property.apply();

    return property;
  }

  public static Property create(String name, String label, PropertyType type)
  {
    Property property = new Property();
    property.setPropertyName(name);
    property.setLabel(label);
    property.setPropertyType(type.name());
    property.apply();

    return property;
  }

}
