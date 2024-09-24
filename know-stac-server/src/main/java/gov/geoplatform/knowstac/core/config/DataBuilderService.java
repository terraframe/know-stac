package gov.geoplatform.knowstac.core.config;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import gov.geoplatform.knowstac.core.model.LocationResult;
import gov.geoplatform.knowstac.core.model.OrganizationResult;
import gov.geoplatform.knowstac.core.model.StacItem;
import gov.geoplatform.knowstac.core.model.StacLocation;
import gov.geoplatform.knowstac.core.model.StacOrganization;
import gov.geoplatform.knowstac.core.service.business.LocationBusinessServiceIF;
import gov.geoplatform.knowstac.core.service.business.OrganizationResultBusinessService;
import gov.geoplatform.knowstac.core.service.business.StacItemBusinessService;
import gov.geoplatform.knowstac.core.service.index.IndexIF;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphSynchronizationQuery;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
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
  private OrganizationResultBusinessService                    oResultService;

  @Autowired
  private LocationBusinessServiceIF                            locationService;

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF               gTypeService;

  @Autowired
  private HierarchyTypeSnapshotBusinessServiceIF               hTypeService;

  @Autowired
  private StacItemBusinessService                              itemService;

  @Autowired
  private IndexIF                                              index;

  @Request
  public void run()
  {
    // String url = "https://idm-gpr-alpha.geoprism.net";
    //
    // logger.error("Synchronizing organizations from [" + url + "]");
    //
    // try (RegistryConnectorIF connector =
    // RegistryConnectorFactory.getConnector(url))
    // {
    // RegistryBridge bridge = new RegistryBridge(connector);
    //
    // JsonArray results = bridge.getOrganizations().getJsonArray();
    //
    // organizationService.importJsonTree(results);
    // }
    //
    // logger.error("Creating Labeled Property Graph Synchronization object");
    //
    // String name = "USFS Operational";
    // LocalizedValue label = new LocalizedValue(name);
    // label.setValue(LocalizedValue.DEFAULT_LOCALE, name);
    //
    // LabeledPropertyGraphSynchronization synchronization = new
    // LabeledPropertyGraphSynchronization();
    // synchronization.setUrl(url);
    // synchronization.setRemoteType("5389f33b-a563-4201-bc03-96ced3000670");
    // LocalizedValueConverter.populate(synchronization.getDisplayLabel(),
    // label);
    // synchronization.setRemoteEntry("6f72b0d3-d9cc-4085-b276-6b9e3a000673");
    // synchronization.setForDate(DateUtil.parseDate("2024-01-01"));
    // synchronization.setRemoteVersion("574f734b-7e2e-4e30-90ad-fdfc06000674");
    // synchronization.setVersionNumber(0);
    // synchronization.apply();
    //
    // logger.error("Synchronizing [" + name + "] label property graph from [" +
    // url + "]");
    //
    // this.lpgService.executeNoAuth(synchronization);
    //
    // logger.error("Building STAC properties");
    //
    // Property.create("agency", "Agency", PropertyType.ORGANIZATION);
    // Property.create("operational", label.getValue(), PropertyType.LOCATION,
    // synchronization);
    // Property.create("title", "Title", PropertyType.STRING);
    // Property.create("description", "Description", PropertyType.STRING);
    // Property.create("datetime", "Date Time", PropertyType.DATE);
    //// Property.create("start_datetime", "Start Date",
    // PropertyType.DATE_TIME);
    //// Property.create("end_datetime", "End Date", PropertyType.DATE_TIME);
    //// Property.create("created", "Create Date", PropertyType.DATE_TIME);
    //// Property.create("updated", "Last Update Date", PropertyType.DATE_TIME);
    // Property.create("platform", "Platform", PropertyType.STRING);
    // Property.create("sensor", "Sensor", PropertyType.STRING);
    // Property.create("collection", "Collection", PropertyType.STRING);
    // Property.create("project", "Project", PropertyType.STRING);
    // Property.create("site", "Site", PropertyType.STRING);
    // Property.create("faaNumber", "UAV FAA Number", PropertyType.ENUMERATION);
    // Property.create("serialNumber", "UAV Serial Number",
    // PropertyType.ENUMERATION);
    //
    // logger.error("Building the index");
    //
    this.index.clear();
    this.index.createIndex();

    populateIndex();
  }

  private void populateIndex()
  {
    logger.error("Populating index");

    LabeledPropertyGraphSynchronizationQuery query = new LabeledPropertyGraphSynchronizationQuery(new QueryFactory());
    try (OIterator<? extends LabeledPropertyGraphSynchronization> iterator = query.getIterator())
    {
      LabeledPropertyGraphSynchronization synchronization = iterator.next();
      LabeledPropertyGraphType graphType = synchronization.getGraphType();
      LabeledPropertyGraphTypeVersion version = synchronization.getVersion();

      GeoObjectTypeSnapshot type = this.gTypeService.getRoot(version);
      HierarchyTypeSnapshot hierarchyType = this.hTypeService.get(version, graphType.getHierarchy());

      String[] itemUrls = new String[] { "https://osmre-uas-dev-deploy-public.s3.amazonaws.com/-stac-/10b13809-f31b-46ec-84ae-9af239ec28f5.json", "https://osmre-uas-dev-deploy-public.s3.amazonaws.com/-stac-/bdfe18f9-04dd-4df5-ac4b-63237c5c3d10.json", "https://osmre-uas-staging-public.s3.amazonaws.com/-stac-/0077ebc5-2226-46b6-86ea-6b0edec56569.json", "https://osmre-uas-staging-public.s3.amazonaws.com/-stac-/d03fa932-c852-48ea-8523-739a204bd368.json", "https://osmre-uas-staging-public.s3.amazonaws.com/-stac-/5f6c046b-54e2-4026-b4b7-d8a8d44d8c10.json", "https://osmre-uas-staging-public.s3.amazonaws.com/-stac-/7b63d5d3-c0c6-4359-944f-5e83792ae82f.json", "https://osmre-uas-staging-public.s3.amazonaws.com/-stac-/162667b1-8496-4f4f-bccc-ef3491f262fb.json",
          "https://osmre-uas-staging-public.s3.amazonaws.com/-stac-/0eafc08c-6860-4c36-adaa-6fcbf6694c02.json" };

      for (int i = 0; i < itemUrls.length; i++)
      {
        String itemUrl = itemUrls[i];

        StacItem item = this.download(itemUrl);
        item.setProperty("agency", Arrays.asList(StacOrganization.build("100013241", "Department of Interior"), StacOrganization.build("100013241", "Forest Service")));

        // Add location information
        String uuid = i % 2 == 0 ? "7085656c-c21f-4a05-bf1e-c978451eb72a" : "42067d2b-cfa0-4583-84e7-bbc10b8e314c";
        LocationResult location = this.locationService.get(synchronization.getOid(), uuid);

        List<StacLocation> operational = this.locationService.getAncestors(version, type, hierarchyType, location, null).stream().map(r -> {
          return StacLocation.build(r.getUuid(), r.getLabel());
        }).collect(Collectors.toList());

        item.setProperty("operational", operational);

        // Add agency information
        String code = "100013241";
        OrganizationResult organization = this.oResultService.get(code);

        List<StacOrganization> agency = this.oResultService.getAncestors(organization, null).stream().map(r -> {
          return StacOrganization.build(r.getCode(), r.getLabel());
        }).collect(Collectors.toList());

        item.setProperty("agency", agency);

        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
          String serialized = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(item);

          System.out.println(serialized);

          item = objectMapper.readValue(serialized, StacItem.class);
        }
        catch (JsonProcessingException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

        this.itemService.put(item);

      }
    }
  }

  private StacItem download(String location)
  {
    try
    {
      URL url = new URL(location);

      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(url, StacItem.class);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }
}
