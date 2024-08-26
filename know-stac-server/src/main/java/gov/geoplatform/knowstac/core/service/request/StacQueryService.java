package gov.geoplatform.knowstac.core.service.request;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import gov.geoplatform.knowstac.core.model.Extent;
import gov.geoplatform.knowstac.core.model.QueryCriteria;
import gov.geoplatform.knowstac.core.model.Spatial;
import gov.geoplatform.knowstac.core.model.StacCollection;
import gov.geoplatform.knowstac.core.model.StacItem;
import gov.geoplatform.knowstac.core.model.StacLink;
import gov.geoplatform.knowstac.core.model.TemporalExtent;
import gov.geoplatform.knowstac.core.service.business.StacItemBusinessService;

@Service
public class StacQueryService
{
  private static Logger           logger = LoggerFactory.getLogger(StacQueryService.class);

  @Autowired
  private StacItemBusinessService service;

  @Request(RequestType.SESSION)
  public StacCollection collection(String sessionId, QueryCriteria criteria)
  {
    List<StacItem> items = this.service.find(criteria);

    Spatial spatial = Spatial.build(items.stream().map(item -> item.getBbox()).collect(Collectors.toList()));
    TemporalExtent temporal = TemporalExtent.build(items.stream().map(item -> item.getProperties().getDatetime()).collect(Collectors.toList()));

    String id = criteria.toEncodedId();
    StacCollection collection = new StacCollection();
    collection.setId(id);
    collection.setTitle("Query result collection");
    collection.setLicense("Apache-2.0");
    collection.setExtent(Extent.build(spatial, temporal));

    collection.addLink(StacLink.build("api/query/collection?criteria=" + id, "self", "application/json"));

    for (StacItem item : items)
    {
      // Assumption that there is a self link in the uploaded s3 item so that we
      // know where to get it
      item.getLinks().stream().filter(link -> link.getRel().equalsIgnoreCase("self")).findAny().ifPresent(link -> {
        collection.addLink(StacLink.build(link.getHref(), "item", "application/geo+json", item.getProperties().getTitle()));
      });

    }

    return collection;
  }

  public StacItem item(String sessionId, String id, String href)
  {
    return this.service.get(id, href);
  }
}
