package gov.geoplatform.knowstac.core.service.business;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import gov.geoplatform.knowstac.core.model.QueryCriteria;
import gov.geoplatform.knowstac.core.model.StacItem;
import gov.geoplatform.knowstac.core.service.index.IndexIF;

@Service
public class StacItemBusinessService
{
  private static Logger logger = LoggerFactory.getLogger(StacItemBusinessService.class);

  @Autowired
  private IndexIF       index;

  public StacItem add(StacItem item)
  {
    // TODO: Handle adding a stac item

    return item;
  }

  public StacItem get(String id)
  {
    return this.index.getItem(id).orElseThrow(() -> {
      // TODO: Exception with message
      throw new ProgrammingErrorException("Unabled to find STAC item with the id [" + id + "]");
    });
  }

  public List<StacItem> find(QueryCriteria criteria)
  {
    return this.index.getItems(criteria);
  }

  public StacItem get(String id, String href)
  {
    List<StacItem> items = this.index.getItems(null);

    if (href.contains("bdfe18f9-04dd-4df5-ac4b-63237c5c3d10"))
    {
      return items.get(1);
    }

    return items.get(0);
  }
}
