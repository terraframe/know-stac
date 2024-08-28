package gov.geoplatform.knowstac;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdEdge;

import net.geoprism.graph.LabeledPropertyGraphTypeVersion;

public class TotalEdge extends TotalEdgeBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -1697550637;

  public TotalEdge()
  {
    super();
  }

  @Override
  public void delete()
  {
    MdEdge graphEdge = this.getGraphEdge();

    super.delete();

    if (graphEdge != null)
    {
      graphEdge.delete();
    }
  }

  public static TotalEdge get(LabeledPropertyGraphTypeVersion version)
  {
    TotalEdgeQuery query = new TotalEdgeQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(version));

    try (OIterator<? extends TotalEdge> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
  }
}
