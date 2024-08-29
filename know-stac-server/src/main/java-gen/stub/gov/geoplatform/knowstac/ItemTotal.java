package gov.geoplatform.knowstac;

import java.util.Optional;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;

import net.geoprism.graph.LabeledPropertyGraphTypeVersion;

public class ItemTotal extends ItemTotalBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID       = 1740552809;

  public static String      ORGANIZATION_HAS_TOTAL = "gov.geoplatform.knowstac.OrganizationHasTotal";

  public ItemTotal()
  {
    super();
  }

  public static Optional<ItemTotal> getForRid(LabeledPropertyGraphTypeVersion version, Object rid)
  {
    TotalEdge totalEdge = TotalEdge.get(version);
    MdEdgeDAOIF mdEdge = MdEdgeDAO.get(totalEdge.getGraphEdgeOid());

    return getForRid(mdEdge, rid);
  }

  public static Optional<ItemTotal> getForRid(MdEdgeDAOIF mdEdge, Object rid)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND(out('" + mdEdge.getDBClassName() + "')) FROM :rid");

    GraphQuery<ItemTotal> query = new GraphQuery<ItemTotal>(statement.toString());
    query.setParameter("rid", rid);

    return Optional.ofNullable(query.getSingleResult());
  }

}
