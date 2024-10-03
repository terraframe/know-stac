package gov.geoplatform.knowstac.core.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

// Configuration of components which DO NOT have web container dependencies
@Configuration
@ComponentScan(
  basePackages = { 
    "net.geoprism.registry.service.business", 
    "net.geoprism.registry.service.permission", 
    "gov.geoplatform.knowstac.core.config", 
    "gov.geoplatform.knowstac.core.service",
    "net.geoprism.spring"
  }
)
public class CoreConfig
{
}
