package gov.geoplatform.knowstac.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.geoplatform.knowstac.core.model.QueryCriteria;
import gov.geoplatform.knowstac.core.service.index.IndexIF;

@Service
public class ElasticSearchTestService implements Runnable
{
  @Autowired
  private IndexIF index;

  public void run()
  {
    this.index.getItems(new QueryCriteria());
  }
}
