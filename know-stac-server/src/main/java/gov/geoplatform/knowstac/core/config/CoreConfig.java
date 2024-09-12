package gov.geoplatform.knowstac.core.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import net.geoprism.registry.service.SessionFilter;

// Configuration of components which DO NOT have web container dependencies
@Configuration
@ComponentScan(
  basePackages = { 
    "net.geoprism.registry.service", 
    "net.geoprism.registry.spring", 
    "net.geoprism.spring", 
    "gov.geoplatform.knowstac.core.config", 
    "gov.geoplatform.knowstac.core.service"
  },
  excludeFilters = @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          classes = {
              SessionFilter.class
}))
public class CoreConfig
{
}
