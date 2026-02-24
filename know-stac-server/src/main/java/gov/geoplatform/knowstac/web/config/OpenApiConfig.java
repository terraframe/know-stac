package gov.geoplatform.knowstac.web.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import gov.geoplatform.knowstac.core.config.AppProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletContext;

@Configuration
public class OpenApiConfig
{

  @Bean
  public OpenAPI customOpenAPI(ServletContext servletContext)
  {
    if (StringUtils.hasText(AppProperties.getOpenApiUrl()))
    {
      return new OpenAPI().servers(List.of(new Server().url(AppProperties.getOpenApiUrl())));
    }

    return new OpenAPI().servers(List.of(new Server().url(servletContext.getContextPath())));
  }
}
