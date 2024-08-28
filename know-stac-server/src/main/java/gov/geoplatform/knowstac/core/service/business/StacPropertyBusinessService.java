package gov.geoplatform.knowstac.core.service.business;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.SelectableReference;

import gov.geoplatform.knowstac.core.model.PropertyType;
import gov.geoplatform.knowstac.core.model.StacProperty;
import gov.geoplatform.knowstac.core.model.StacProperty.Location;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphSynchronizationQuery;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeQuery;
import net.geoprism.registry.graph.GraphOrganization;
import net.geoprism.registry.service.request.CacheProviderIF;

@Service
public class StacPropertyBusinessService
{
  private static Logger   logger = LoggerFactory.getLogger(StacPropertyBusinessService.class);

  @Autowired
  private CacheProviderIF provider;

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

  public List<StacProperty> getForOrganization(String code)
  {
    return provider.getServerCache().getOrganization(code).map(organization -> {
      // Two phase query. The first phase traverses the graph tree to find all
      // of
      // the child organizations of a node in the tree. The second phase then
      // queries postgres with the list of child organizations as criteria

      StringBuilder statement = new StringBuilder();
      statement.append("TRAVERSE OUT('organization_hierarchy') FROM :organization");

      GraphQuery<GraphOrganization> gQuery = new GraphQuery<GraphOrganization>(statement.toString());
      gQuery.setParameter("organization", organization.getGraphOrganization().getRID());

      String[] organizationIds = gQuery.getResults().stream().map(org -> org.getOrganizationOid()).toArray(String[]::new);

      QueryFactory factory = new QueryFactory();

      LabeledPropertyGraphTypeQuery query = new LabeledPropertyGraphTypeQuery(factory);
      query.WHERE( ( (SelectableReference) query.get(LabeledPropertyGraphType.ORGANIZATION) ).IN(organizationIds));

      LabeledPropertyGraphSynchronizationQuery sQuery = new LabeledPropertyGraphSynchronizationQuery(factory);
      sQuery.WHERE(sQuery.getGraphType().EQ(query));

      List<StacProperty> properties = new LinkedList<StacProperty>();

      try (OIterator<? extends LabeledPropertyGraphSynchronization> iterator = sQuery.getIterator())
      {
        while (iterator.hasNext())
        {
          LabeledPropertyGraphSynchronization synchronization = iterator.next();
          String label = synchronization.getDisplayLabel().getValue();
          Location location = StacProperty.Location.build(synchronization.getOid(), synchronization.getForDate(), label);

          properties.add(StacProperty.build("operational", label, PropertyType.LOCATION, "agency", location));
        }
      }

      return properties;
    }).orElse(new LinkedList<>());
  }

}
