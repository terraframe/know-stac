package gov.geoplatform.knowstac.core.service.request;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gov.geoplatform.knowstac.core.config.AppProperties;

@Service
public class AccessControlService
{
  private static Logger logger = LoggerFactory.getLogger(AccessControlService.class);

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
      logger.info("Rejecting access for ip address [" + ip + "]");
    }

    return access;
  }

}
