package gov.geoplatform.knowstac.core.model;

public class Configuration
{
  public boolean tiling;

  public String  url;

  public boolean isTiling()
  {
    return tiling;
  }

  public void setTiling(boolean tiling)
  {
    this.tiling = tiling;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

}
