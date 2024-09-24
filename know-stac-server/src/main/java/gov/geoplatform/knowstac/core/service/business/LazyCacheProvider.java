package gov.geoplatform.knowstac.core.service.business;

import org.commongeoregistry.adapter.metadata.MetadataCache;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import gov.geoplatform.knowstac.core.model.OrganizationResult;
import net.geoprism.registry.cache.ServerOrganizationCache;
import net.geoprism.registry.service.request.CacheProvider;
import net.geoprism.registry.service.request.CacheProviderIF;

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
  public MetadataCache getAdapterCache()
  {
    load();

    return super.getAdapterCache();
  }

  @Override
  public ServerOrganizationCache getServerCache()
  {
    load();

    return super.getServerCache();
  }

}
