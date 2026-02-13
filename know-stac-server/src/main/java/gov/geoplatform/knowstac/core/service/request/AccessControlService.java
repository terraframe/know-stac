package gov.geoplatform.knowstac.core.service.request;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import gov.geoplatform.knowstac.core.config.AppProperties;

@Service
public class AccessControlService
{
  public boolean hasAccess(String ip)
  {
    boolean access = Arrays.asList(AppProperties.getWhitelist().split(",")) //
        .stream() //
        .map(t -> t.trim()) //
        .anyMatch(t -> {
          return t.equals(ip) || t.equals("*");
        });

    if (!access)
    {
      System.out.println("Rejecting access for ip address [" + ip + "]");
    }

    return access;
  }

}
