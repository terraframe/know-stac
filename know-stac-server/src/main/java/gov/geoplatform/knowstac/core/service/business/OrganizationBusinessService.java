package gov.geoplatform.knowstac.core.service.business;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import gov.geoplatform.knowstac.ItemTotal;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.OrganizationBusinessServiceIF;

@Service(value = "ksBusinessService")
@Primary
public class OrganizationBusinessService extends net.geoprism.registry.service.business.OrganizationBusinessService implements OrganizationBusinessServiceIF
{
  @Override
  @Transaction
  public void apply(ServerOrganization organization, ServerOrganization parent)
  {
    boolean isNew = organization.isNew();

    super.apply(organization, parent);

    if (isNew)
    {
      MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(ItemTotal.ORGANIZATION_HAS_TOTAL);

      ItemTotal total = new ItemTotal();
      total.setNumberOfItems(0);
      total.apply();

      organization.getGraphOrganization().addChild(total, mdEdge).apply();
    }
  }
}
