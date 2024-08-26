package gov.geoplatform.knowstac.core.service.business;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.geoplatform.knowstac.core.model.QueryCriteria;
import gov.geoplatform.knowstac.core.model.StacItem;

@Service
public class StacItemBusinessService
{
  private static Logger   logger = LoggerFactory.getLogger(StacItemBusinessService.class);

  private static StacItem item1  = null;

  private static StacItem item2  = null;

  private static synchronized StacItem getItem()
  {
    if (item1 == null)
    {
      item1 = download("https://osmre-uas-dev-deploy-public.s3.amazonaws.com/-stac-/10b13809-f31b-46ec-84ae-9af239ec28f5.json");
    }

    return item1;
  }

  private static synchronized StacItem getItem2()
  {
    if (item2 == null)
    {
      item2 = download("https://osmre-uas-dev-deploy-public.s3.amazonaws.com/-stac-/bdfe18f9-04dd-4df5-ac4b-63237c5c3d10.json");
    }

    return item2;
  }

  private static StacItem download(String location)
  {
    try
    {
      URL url = new URL(location);

      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(url, StacItem.class);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  public StacItem add(StacItem item)
  {
    // TODO: Handle adding a stac item

    return item;
  }

  public StacItem get(String id)
  {
    return getItem();
  }

  public List<StacItem> find(QueryCriteria criteria)
  {
    return Arrays.asList(getItem(), getItem2());
  }

  public StacItem get(String id, String asset)
  {
    if (asset.contains("bdfe18f9-04dd-4df5-ac4b-63237c5c3d10"))
    {
      return getItem2();
    }

    return getItem();
  }
}
