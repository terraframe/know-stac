package gov.geoplatform.knowstac.core.service.business;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.SelectableReference;
import com.runwaysdk.session.Request;

import gov.geoplatform.knowstac.Property;
import gov.geoplatform.knowstac.PropertyQuery;
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
  private static Logger      logger = LoggerFactory.getLogger(StacPropertyBusinessService.class);

  @Autowired
  private CacheProviderIF    provider;

  // Simple cache because we StacProperties are only defined at build time, not
  // during the run time of the application
  private List<StacProperty> cache;

  public StacPropertyBusinessService()
  {
    this.cache = null;
  }

  public List<StacProperty> getAll()
  {
    synchronized (this)
    {
      if (this.cache == null)
      {
        this.cache = this.getAllFromDatabase();
      }
    }

    return this.cache;
  }

  @Request
  private List<StacProperty> getAllFromDatabase()
  {
    PropertyQuery query = new PropertyQuery(new QueryFactory());
    query.ORDER_BY(query.getPropertyName(), SortOrder.ASC);

    try (OIterator<? extends Property> it = query.getIterator())
    {
      return it.getAll().stream().map(p -> p.toDTO()).sorted((a, b) -> {
        if ((a.getType().equals(PropertyType.DATE) || a.getType().equals(PropertyType.DATE_TIME))
            && !(b.getType().equals(PropertyType.DATE) || b.getType().equals(PropertyType.DATE_TIME)))
        {
          return -1;
        }
        
        if (a.getType().equals(PropertyType.ORGANIZATION) && !b.getType().equals(PropertyType.ORGANIZATION))
        {
          return -1;
        }


        return a.getLabel().compareTo(b.getLabel());
      }).collect(Collectors.toList());
    }
  }

  public List<StacProperty> getForOrganization(String code)
  {
    return provider.getServerCache().getOrganization(code).map(organization -> {
      // Two phase query. The first phase traverses the graph tree to find all
      // of the child organizations of a node in the tree. The second phase
      // then queries postgres with the list of child organizations as
      // criteria

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

      PropertyQuery pQuery = new PropertyQuery(factory);
      pQuery.WHERE(pQuery.getSynchronization().EQ(sQuery));

      List<StacProperty> properties = new LinkedList<StacProperty>();

      try (OIterator<? extends Property> iterator = pQuery.getIterator())
      {
        while (iterator.hasNext())
        {
          properties.add(iterator.next().toDTO());
        }
      }

      return properties;
    }).orElse(new LinkedList<>());
  }

}
