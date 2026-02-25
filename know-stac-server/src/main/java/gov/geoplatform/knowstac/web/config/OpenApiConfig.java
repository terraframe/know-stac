package gov.geoplatform.knowstac.web.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import gov.geoplatform.knowstac.core.config.AppProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletContext;

@Configuration
public class OpenApiConfig
{

  @Bean
  public OpenAPI customOpenAPI(ServletContext servletContext)
  {
    StringBuilder description = new StringBuilder();
    description.append("Know-STAC (Knowledge-Enabled STAC) is a semantic layer and storage application for making STAC searches interoperable across agencies and domains.\n");
    description.append("<br>Know-STAC supports the STAC Item and STAC Collection specifications from the collection of STAC specifications (https://stacspec.org/en/).\n");
    description.append("<br>Know-STAC does NOT support the STAC Catalog or STAC API specifications.");

    OpenAPI api = new OpenAPI().info( //
        new Info() //
            .title("Know-STAC") //
            .version("0.0.1") //
            .description(description.toString()));

    if (StringUtils.hasText(AppProperties.getOpenApiUrl()))
    {
      return api.servers(List.of(new Server().url(AppProperties.getOpenApiUrl())));
    }

    return api.servers(List.of(new Server().url(servletContext.getContextPath())));
  }
}
