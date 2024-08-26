package gov.geoplatform.knowstac.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

// The object describes the spatio-temporal extents of the Collection. Both
// spatial and temporal extents are required to be specified.
public class Extent
{
  // REQUIRED. Potential spatial extents covered by the Collection.
  @JsonInclude(Include.NON_NULL)
  private Spatial        spatial;

  // REQUIRED. Potential temporal extents covered by the Collection.
  @JsonInclude(Include.NON_NULL)
  private TemporalExtent temporal;

  public Spatial getSpatial()
  {
    return spatial;
  }

  public void setSpatial(Spatial spatial)
  {
    this.spatial = spatial;
  }

  public TemporalExtent getTemporal()
  {
    return temporal;
  }

  public void setTemporal(TemporalExtent temporal)
  {
    this.temporal = temporal;
  }

  public static Extent build(Spatial spatial, TemporalExtent temporal)
  {
    Extent extent = new Extent();
    extent.setSpatial(spatial);
    extent.setTemporal(temporal);

    return extent;
  }
}
