package gov.geoplatform.knowstac.core.service.request;

import java.util.List;

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
  public StacItem put(String sessionId, StacItem item)
  {
    return this.service.put(item);
  }

  @Request(RequestType.SESSION)
  public StacItem get(String sessionId, String id)
  {
    return this.service.get(id);
  }

  @Request(RequestType.SESSION)
  public List<String> values(String sessionId, String field, String text)
  {
    return this.service.values(field, text);
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String id)
  {
    this.service.remove(id);
  }
}
