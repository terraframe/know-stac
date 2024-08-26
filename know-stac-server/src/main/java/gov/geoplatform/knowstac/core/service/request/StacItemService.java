package gov.geoplatform.knowstac.core.service.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.knowstac.core.model.StacItem;
import gov.geoplatform.knowstac.core.service.business.StacItemBusinessService;

@Service
public class StacItemService
{
  private static Logger           logger = LoggerFactory.getLogger(StacItemService.class);

  @Autowired
  private StacItemBusinessService service;

  @Request(RequestType.SESSION)
  public StacItem add(String sessionId, StacItem item)
  {
    return this.service.add(item);
  }

  @Request(RequestType.SESSION)
  public StacItem get(String sessionId, String oid)
  {
    return this.service.get(oid);
  }
}
