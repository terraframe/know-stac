package gov.geoplatform.knowstac.core.config;

import java.util.Arrays;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import gov.geoplatform.knowstac.ItemTotal;
import gov.geoplatform.knowstac.Property;
import gov.geoplatform.knowstac.core.model.LocationResult;
import gov.geoplatform.knowstac.core.model.OrganizationResult;
import gov.geoplatform.knowstac.core.model.PropertyType;
import gov.geoplatform.knowstac.core.service.business.LocationBusinessServiceIF;
import gov.geoplatform.knowstac.core.service.business.OrganizationResultBusinessService;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.DateUtil;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.lpg.adapter.RegistryBridge;
import net.geoprism.registry.lpg.adapter.RegistryConnectorFactory;
import net.geoprism.registry.lpg.adapter.RegistryConnectorIF;
import net.geoprism.registry.service.business.GeoObjectTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeSnapshotBusinessServiceIF;
import net.geoprism.registry.service.business.LabeledPropertyGraphSynchronizationBusinessServiceIF;
import net.geoprism.registry.service.business.OrganizationBusinessServiceIF;

@Service
public class DataBuilderService implements Runnable
{
  private static final Logger                                  logger = LoggerFactory.getLogger(DataBuilderService.class);

  @Autowired
  private LabeledPropertyGraphSynchronizationBusinessServiceIF lpgService;

  @Autowired
  private OrganizationBusinessServiceIF                        organizationService;

  @Autowired
  private LocationBusinessServiceIF                            locationService;

  @Autowired
  private OrganizationResultBusinessService                    resultService;

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF               gTypeService;

  @Autowired
  private HierarchyTypeSnapshotBusinessServiceIF               hTypeService;

  @Request
  public void run()
  {
    transaction();

    String url = "https://idm-gpr-alpha.geoprism.net";

    logger.error("Synchronizing organizations from [" + url + "]");

    try (RegistryConnectorIF connector = RegistryConnectorFactory.getConnector(url))
    {
      RegistryBridge bridge = new RegistryBridge(connector);

      JsonArray results = bridge.getOrganizations().getJsonArray();

      organizationService.importJsonTree(results);
    }

    logger.error("Creating Labeled Property Graph Synchronization object");

    String name = "USFS Operational";
    LocalizedValue label = new LocalizedValue(name);
    label.setValue(LocalizedValue.DEFAULT_LOCALE, name);

    LabeledPropertyGraphSynchronization synchronization = new LabeledPropertyGraphSynchronization();
    synchronization.setUrl(url);
    synchronization.setRemoteType("5389f33b-a563-4201-bc03-96ced3000670");
    LocalizedValueConverter.populate(synchronization.getDisplayLabel(), label);
    synchronization.setRemoteEntry("6f72b0d3-d9cc-4085-b276-6b9e3a000673");
    synchronization.setForDate(DateUtil.parseDate("2024-01-01"));
    synchronization.setRemoteVersion("574f734b-7e2e-4e30-90ad-fdfc06000674");
    synchronization.setVersionNumber(0);
    synchronization.apply();

    logger.error("Synchronizing [" + name + "] label property graph from [" + url + "]");

    this.lpgService.executeNoAuth(synchronization);

    logger.error("Assigning item totals to locations for the hierarchy [" + name + "]");

    LabeledPropertyGraphType graphType = synchronization.getGraphType();
    LabeledPropertyGraphTypeVersion version = synchronization.getVersion();

    GeoObjectTypeSnapshot type = this.gTypeService.getRoot(version);
    HierarchyTypeSnapshot hierarchyType = this.hTypeService.get(version, graphType.getHierarchy());

    List<String> uuids = Arrays.asList("7085656c-c21f-4a05-bf1e-c978451eb72a", "42067d2b-cfa0-4583-84e7-bbc10b8e314c");

    for (String uuid : uuids)
    {
      LocationResult child = this.locationService.get(synchronization.getOid(), uuid);

      this.locationService.getAncestors(version, type, hierarchyType, child, null).forEach(location -> {
        ItemTotal.getForRid(version, location.getRid()).ifPresent(total -> {
          logger.error("Updating total for location [" + location.getLabel() + "]");

          total.setNumberOfItems(total.getNumberOfItems() + 1);
          total.apply();
        });
      });
    }

    logger.error("Assigning item totals to organizations");

    List<String> codes = Arrays.asList("100013241");

    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(ItemTotal.ORGANIZATION_HAS_TOTAL);

    for (String code : codes)
    {
      OrganizationResult child = this.resultService.get(code);

      this.resultService.getAncestors(child, null).forEach(organization -> {
        ItemTotal.getForRid(mdEdge, organization.getRid()).ifPresent(total -> {
          logger.error("Updating total for location [" + organization.getLabel() + "]");

          total.setNumberOfItems(total.getNumberOfItems() + 1);
          total.apply();
        });
      });
    }

    logger.error("Building STAC properties");

    Property.create("operational", label.getValue(), PropertyType.LOCATION, synchronization);
    Property.create("title", "Title", PropertyType.STRING);
    Property.create("description", "Description", PropertyType.STRING);
    Property.create("datetime", "Date Time", PropertyType.DATE_TIME);
    Property.create("start_datetime", "Start Date", PropertyType.DATE_TIME);
    Property.create("end_datetime", "End Date", PropertyType.DATE_TIME);
    Property.create("created", "Create Date", PropertyType.DATE_TIME);
    Property.create("updated", "Last Update Date", PropertyType.DATE_TIME);
    Property.create("platform", "Platform", PropertyType.STRING);
    Property.create("sensor", "Sensor", PropertyType.STRING);
    Property.create("collection", "Collection", PropertyType.STRING);
    Property.create("project", "Project", PropertyType.STRING);
    Property.create("site", "Site", PropertyType.STRING);
    Property.create("faaNumber", "UAV FAA Number", PropertyType.ENUMERATION);
    Property.create("serialNumber", "UAV Serial Number", PropertyType.ENUMERATION);
  }

  @Transaction
  protected void transaction()
  {
  }

}
