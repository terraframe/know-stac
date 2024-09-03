package gov.geoplatform.knowstac.web.service;

import java.util.Set;

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

    endpoints.add("api/item/add");
    endpoints.add("api/item/get");
    
    endpoints.add("api/query/collection");
    endpoints.add("api/query/item");
    
    endpoints.add("api/stac-property/get-all");
    endpoints.add("api/stac-property/get-for-organization");

    endpoints.add("api/organization/get");
    endpoints.add("api/organization/search");
    endpoints.add("api/organization/get-children");
    endpoints.add("api/organization/get-ancestor-tree");
    
    endpoints.add("api/location/get");
    endpoints.add("api/location/search");
    endpoints.add("api/location/get-children");
    endpoints.add("api/location/get-ancestor-tree");
    
    endpoints.add("api/aws/download");
    
    return endpoints;
  }

  @Override
  public String getHomeUrl()
  {
    return "/know-stac";
  }

  @Override
  public String getLoginUrl()
  {
    return "/know-stac#/login";
  }
}
