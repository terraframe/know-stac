package gov.geoplatform.knowstac.core.config;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.registry.DateUtil;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.lpg.adapter.RegistryBridge;
import net.geoprism.registry.lpg.adapter.RegistryConnectorFactory;
import net.geoprism.registry.lpg.adapter.RegistryConnectorIF;
import net.geoprism.registry.service.business.LabeledPropertyGraphSynchronizationBusinessServiceIF;
import net.geoprism.registry.service.business.OrganizationBusinessServiceIF;

@Service
public class StacPropertyBuilderService implements Runnable
{
  private static final Logger                                  logger = LoggerFactory.getLogger(StacPropertyBuilderService.class);

  @Autowired
  private LabeledPropertyGraphSynchronizationBusinessServiceIF lpgService;

  @Autowired
  private OrganizationBusinessServiceIF                        organizationService;

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
  }

  @Transaction
  protected void transaction()
  {
  }
}
