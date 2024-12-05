package gov.geoplatform.knowstac.core.service.business;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import gov.geoplatform.knowstac.core.model.OrganizationResult;
import net.geoprism.registry.cache.ServerOrganizationCache;
import net.geoprism.registry.service.business.CacheProvider;
import net.geoprism.registry.service.business.CacheProviderIF;

@Service
@Primary
public class LazyCacheProvider extends CacheProvider implements CacheProviderIF
{
  private boolean loaded;

  public LazyCacheProvider()
  {
    super();

    this.loaded = false;
  }

  private synchronized void load()
  {
    if (!loaded)
    {
      OrganizationResult.populateCache(super.getServerCache());
    }

    this.loaded = true;
  }

  @Override
  public ServerOrganizationCache getServerCache()
  {
    load();

    return super.getServerCache();
  }

}
