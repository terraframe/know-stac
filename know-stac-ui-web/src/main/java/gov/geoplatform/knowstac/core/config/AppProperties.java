package gov.geoplatform.knowstac.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class AppProperties
{
  @Autowired
  private Environment env;

  public String getTitilerHost()
  {
    return env.getProperty("titiler.host", "https://titiler.xyz");
  }

  public Integer getTitilerPort()
  {
    return env.getProperty("titiler.port", Integer.class, 443);
  }

  public Boolean isTitilerEnabled()
  {
    return env.getProperty("titiler.enabled", Boolean.class, false);
  }

  public String getServerUrl()
  {
    return env.getProperty("server.url", "http://127.0.0.1:8080/");
  }

  public String getApiUrl()
  {
    return env.getProperty("api.url", "https://localhost:8444");
  }

}
