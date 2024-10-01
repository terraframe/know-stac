package com.runwaysdk.build.domain;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.runwaysdk.dataaccess.cache.globalcache.ehcache.CacheShutdown;

import gov.geoplatform.knowstac.core.config.CoreConfig;
import gov.geoplatform.knowstac.core.config.DataBuilderService;

public class DataBuilder
{
  public static void main(String[] args)
  {
    OGlobalConfiguration.NETWORK_BINARY_MAX_CONTENT_LENGTH.setValue(56384);

    boolean standalone = args.length > 0 && Boolean.valueOf(args[0]);

    try
    {
      try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(CoreConfig.class))
      {
        DataBuilderService obj = context.getBean(DataBuilderService.class);
        obj.run();
      }
    }
    finally
    {
      if (standalone)
      {
        CacheShutdown.shutdown();
      }
    }

  }
}
