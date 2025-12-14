package gov.geoplatform.knowstac.web.service;

import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service(value = "appSessionService")
@Primary
public class SessionService extends net.geoprism.registry.service.request.SessionService
{
  /*
   * Expose public endpoints to allow non-logged in users to hit controller
   * endpoints
   */
  @Override
  public Set<String> getPublicEndpoints()
  {
    Set<String> endpoints = super.getPublicEndpoints();

    endpoints.add("item/put");
    endpoints.add("item/remove");
    endpoints.add("item/get");
    endpoints.add("item/values");

    endpoints.add("query/collection");

    endpoints.add("stac-property/get-all");
    endpoints.add("stac-property/get-for-organization");

    endpoints.add("organization/get");
    endpoints.add("organization/search");
    endpoints.add("organization/get-children");
    endpoints.add("organization/get-ancestor-tree");

    endpoints.add("location/get");
    endpoints.add("location/search");
    endpoints.add("location/get-children");
    endpoints.add("location/get-ancestor-tree");

    endpoints.add("aws/download");

    return endpoints;
  }
}
