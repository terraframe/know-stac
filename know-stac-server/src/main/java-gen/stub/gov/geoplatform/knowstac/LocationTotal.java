package gov.geoplatform.knowstac;

import java.util.Optional;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;

import net.geoprism.graph.LabeledPropertyGraphTypeVersion;

public class LocationTotal extends LocationTotalBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 848621707;

  public LocationTotal()
  {
    super();
  }

  public static Optional<LocationTotal> getForRid(LabeledPropertyGraphTypeVersion version, Object rid)
  {
    TotalEdge totalEdge = TotalEdge.get(version);
    MdEdgeDAOIF mdEdge = MdEdgeDAO.get(totalEdge.getGraphEdgeOid());

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND(out('" + mdEdge.getDBClassName() + "')) FROM :rid");

    GraphQuery<LocationTotal> query = new GraphQuery<LocationTotal>(statement.toString());
    query.setParameter("rid", rid);

    return Optional.ofNullable(query.getSingleResult());
  }
}
