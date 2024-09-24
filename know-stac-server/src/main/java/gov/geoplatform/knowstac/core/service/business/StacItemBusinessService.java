package gov.geoplatform.knowstac.core.service.business;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;

import gov.geoplatform.knowstac.GenericException;
import gov.geoplatform.knowstac.ItemTotal;
import gov.geoplatform.knowstac.core.model.LocationResult;
import gov.geoplatform.knowstac.core.model.OrganizationResult;
import gov.geoplatform.knowstac.core.model.PropertyType;
import gov.geoplatform.knowstac.core.model.QueryCriteria;
import gov.geoplatform.knowstac.core.model.StacItem;
import gov.geoplatform.knowstac.core.model.StacLocation;
import gov.geoplatform.knowstac.core.model.StacOrganization;
import gov.geoplatform.knowstac.core.service.index.IndexIF;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessServiceIF;

@Service
public class StacItemBusinessService
{
  private static Logger                          logger = LoggerFactory.getLogger(StacItemBusinessService.class);

  @Autowired
  private IndexIF                                index;

  @Autowired
  private StacPropertyBusinessService            propertyService;

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF gTypeService;

  @Autowired
  private HierarchyTypeSnapshotBusinessServiceIF hTypeService;

  @Autowired
  private LocationBusinessServiceIF              locationService;

  @Autowired
  private OrganizationResultBusinessService      resultService;

  public StacItem put(StacItem item)
  {
    this.index.getItem(item.getId()).ifPresent(i -> {
      throw new GenericException("A STAC item already exists with the id [" + item.getId() + "]");
    });

    this.validateProperties(item);

    this.index.put(item);

    // Update the totals for the new locations and organization values
    updateTotals(item, 1);

    return item;
  }

  private void validateProperties(StacItem item)
  {
    // Validate location values
    this.propertyService.getAll().stream().filter(p -> p.getType().equals(PropertyType.LOCATION)).forEach(property -> {

      Optional<List<StacLocation>> optional = item.getProperty(property.getName());

      optional.ifPresent(locations -> {
        String synchronizationId = property.getLocation().getSynchronizationId();

        for (StacLocation location : locations)
        {
          LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(synchronizationId);

          LocationResult child = this.locationService.get(synchronization.getOid(), location.getUuid());

          if (child == null)
          {
            throw new GenericException("A location doesn't exist with the UUID [" + location.getUuid() + "]");
          }
        }
      });
    });

    this.propertyService.getAll().stream().filter(p -> p.getType().equals(PropertyType.ORGANIZATION)).forEach(property -> {

      Optional<List<StacOrganization>> optional = item.getProperty(property.getName());

      optional.ifPresent(organizations -> {
        for (StacOrganization organization : organizations)
        {
          OrganizationResult child = this.resultService.get(organization.getCode());

          if (child == null)
          {
            throw new GenericException("A location doesn't exist with the code [" + organization.getCode() + "]");
          }
        }
      });
    });
  }

  private void updateTotals(final StacItem item, final int amount)
  {
    this.propertyService.getAll().stream().filter(p -> p.getType().equals(PropertyType.LOCATION)).forEach(property -> {

      Optional<List<StacLocation>> optional = item.getProperty(property.getName());

      optional.filter(l -> l.size() > 0).ifPresent(locations -> {
        String synchronizationId = property.getLocation().getSynchronizationId();

        StacLocation stacLocation = locations.get(0);

        LabeledPropertyGraphSynchronization synchronization = LabeledPropertyGraphSynchronization.get(synchronizationId);
        LabeledPropertyGraphType graphType = synchronization.getGraphType();
        LabeledPropertyGraphTypeVersion version = synchronization.getVersion();

        GeoObjectTypeSnapshot type = this.gTypeService.getRoot(version);
        HierarchyTypeSnapshot hierarchyType = this.hTypeService.get(version, graphType.getHierarchy());

        logger.info("Assigning item totals to locations for the hierarchy [" + synchronization.getDisplayLabel().getValue() + "]");

        LocationResult child = this.locationService.get(synchronization.getOid(), stacLocation.getUuid());

        this.locationService.getAncestors(version, type, hierarchyType, child, null).forEach(location -> {
          ItemTotal.getForRid(version, location.getRid()).ifPresent(total -> {
            logger.info("Updating total for location [" + location.getLabel() + "]");

            total.setNumberOfItems(total.getNumberOfItems() + amount);
            total.apply();
          });
        });
      });
    });

    logger.info("Assigning item totals to organizations");

    // Update the location totals
    this.propertyService.getAll().stream().filter(p -> p.getType().equals(PropertyType.ORGANIZATION)).forEach(property -> {

      Optional<List<StacOrganization>> optional = item.getProperty(property.getName());

      optional.filter(l -> l.size() > 0).ifPresent(organizations -> {
        StacOrganization stacOrganization = organizations.get(0);

        MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(ItemTotal.ORGANIZATION_HAS_TOTAL);

        OrganizationResult child = this.resultService.get(stacOrganization.getCode());

        this.resultService.getAncestors(child, null).forEach(organization -> {
          ItemTotal.getForRid(mdEdge, organization.getRid()).ifPresent(total -> {
            logger.info("Updating total for location [" + organization.getLabel() + "]");

            total.setNumberOfItems(total.getNumberOfItems() + amount);
            total.apply();
          });
        });
      });
    });
  }

  public StacItem get(String id)
  {
    return this.index.getItem(id).orElseThrow(() -> {
      throw new GenericException("Unabled to find STAC item with the id [" + id + "]");
    });
  }

  public List<StacItem> find(QueryCriteria criteria)
  {
    return this.index.getItems(criteria);
  }

  public List<StacItem> find(Map<String, String> params)
  {
    return this.index.getItems(params);
  }

  public List<String> values(String field, String text)
  {
    return this.index.values(field, text);
  }

  public void remove(String id)
  {
    Optional<StacItem> existing = this.index.getItem(id);

    // Remove from the total the existing hierarchy/organization values because
    // they might have changed
    existing.ifPresent(i -> updateTotals(i, -1));

    this.index.removeStacItem(id);
  }

}
