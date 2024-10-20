package gov.geoplatform.knowstac.core.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan("gov.geoplatform.knowstac.core")
public class CoreConfiguration
{


}