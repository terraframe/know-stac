/**
 * Copyright 2020 The Department of Interior
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.geoplatform.knowstac.core.config;

import java.util.List;

import com.runwaysdk.configuration.ConfigurationManager;
import com.runwaysdk.configuration.ConfigurationReaderIF;

public class AppProperties
{
  /**
   * The app.properties configuration file
   */
  private ConfigurationReaderIF props;

  private AppProperties()
  {
    this.props = ConfigurationManager.getReader(ConfigGroup.COMMON, "app.properties");
  }

  private static class Singleton
  {
    private static AppProperties INSTANCE = new AppProperties();

    private static AppProperties getInstance()
    {
      // INSTANCE will only ever be null if there is a problem. The if check is
      // to allow for debugging.
      if (INSTANCE == null)
      {
        INSTANCE = new AppProperties();
      }

      return INSTANCE;
    }

    private static ConfigurationReaderIF getProps()
    {
      return getInstance().props;
    }
  }

  public static String getElasticsearchHost()
  {
    return Singleton.getProps().getString("elasticsearch.host", "localhost");
  }

  public static String getElasticsearchSchema()
  {
    return Singleton.getProps().getString("elasticsearch.schema", "http");
  }

  public static int getElasticsearchPort()
  {
    return Singleton.getProps().getInteger("elasticsearch.port", 9200);
  }

  public static String getElasticsearchUsername()
  {
    return Singleton.getProps().getString("elasticsearch.username", "elastic");
  }

  public static String getElasticsearchPassword()
  {
    return Singleton.getProps().getString("elasticsearch.password", "elastic");
  }

  public static String getElasticsearchIndex()
  {
    return Singleton.getProps().getString("elasticsearch.index", "knowstac");
  }
}
