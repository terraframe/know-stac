package com.runwaysdk.build.domain;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.runwaysdk.dataaccess.cache.globalcache.ehcache.CacheShutdown;

import gov.geoplatform.knowstac.core.config.CoreConfig;
import gov.geoplatform.knowstac.core.config.StacPropertyBuilderService;

public class StacPropertyBuilder
{
  public static void main(String[] args)
  {
    try
    {
      try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(CoreConfig.class))
      {
        StacPropertyBuilderService obj = context.getBean(StacPropertyBuilderService.class);
        obj.run();
      }
    }
    finally
    {
      if (args.length > 0 && Boolean.valueOf(args[0]))
      {
        CacheShutdown.shutdown();
      }
    }

  }
}
