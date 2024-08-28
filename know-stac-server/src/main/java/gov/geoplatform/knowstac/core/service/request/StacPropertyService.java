package gov.geoplatform.knowstac.core.service.request;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.knowstac.core.model.StacProperty;
import gov.geoplatform.knowstac.core.service.business.StacPropertyBusinessService;

@Service
public class StacPropertyService
{
  @Autowired
  private StacPropertyBusinessService service;

  @Request(RequestType.SESSION)
  public List<StacProperty> getAll(String sessionId)
  {
    return this.service.getAll();
  }

  @Request(RequestType.SESSION)
  public List<StacProperty> getForOrganization(String sessionId, String code)
  {
    return this.service.getForOrganization(code);
  }
}
