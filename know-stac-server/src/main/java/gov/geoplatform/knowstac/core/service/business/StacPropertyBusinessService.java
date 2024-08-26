package gov.geoplatform.knowstac.core.service.business;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import gov.geoplatform.knowstac.core.model.PropertyType;
import gov.geoplatform.knowstac.core.model.StacProperty;
import gov.geoplatform.knowstac.core.model.StacProperty.Location;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphSynchronizationQuery;

@Service
public class StacPropertyBusinessService
{
  private static Logger logger = LoggerFactory.getLogger(StacPropertyBusinessService.class);

  public List<StacProperty> getAll()
  {
    LabeledPropertyGraphSynchronizationQuery query = new LabeledPropertyGraphSynchronizationQuery(new QueryFactory());

    List<StacProperty> properties = new LinkedList<>();
    properties.add(StacProperty.build("agency", "Agency", PropertyType.ORGANIZATION));

    try (OIterator<? extends LabeledPropertyGraphSynchronization> it = query.getIterator())
    {
      if (it.hasNext())
      {
        LabeledPropertyGraphSynchronization synchronization = it.next();

        String label = synchronization.getDisplayLabel().getValue();
        Location location = StacProperty.Location.build(synchronization.getOid(), synchronization.getForDate(), label);

        properties.add(StacProperty.build("operational", label, PropertyType.LOCATION, "agency", location));
      }
    }

    properties.add(StacProperty.build("title", "Title", PropertyType.STRING));
    properties.add(StacProperty.build("description", "Description", PropertyType.STRING));
    properties.add(StacProperty.build("datetime", "Date Time", PropertyType.DATE_TIME));
    properties.add(StacProperty.build("start_datetime", "Start Date", PropertyType.DATE_TIME));
    properties.add(StacProperty.build("end_datetime", "End Date", PropertyType.DATE_TIME));
    properties.add(StacProperty.build("created", "Create Date", PropertyType.DATE_TIME));
    properties.add(StacProperty.build("updated", "Last Update Date", PropertyType.DATE_TIME));
    properties.add(StacProperty.build("platform", "Platform", PropertyType.STRING));
    properties.add(StacProperty.build("sensor", "Sensor", PropertyType.STRING));
    properties.add(StacProperty.build("collection", "Collection", PropertyType.STRING));
    properties.add(StacProperty.build("project", "Project", PropertyType.STRING));
    properties.add(StacProperty.build("site", "Site", PropertyType.STRING));
    properties.add(StacProperty.build("faaNumber", "UAV FAA Number", PropertyType.ENUMERATION));
    properties.add(StacProperty.build("serialNumber", "UAV Serial Number", PropertyType.ENUMERATION));

    return properties;
  }

}
