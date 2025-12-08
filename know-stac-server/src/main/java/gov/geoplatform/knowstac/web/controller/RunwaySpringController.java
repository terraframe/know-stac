package gov.geoplatform.knowstac.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.constants.ClientRequestIF;

public abstract class RunwaySpringController
{
  @Autowired
  private HttpServletRequest  request;

  @Autowired
  private HttpServletResponse response;

  protected HttpServletRequest getRequest()
  {
    return request;
  }

  protected HttpServletResponse getResponse()
  {
    return response;
  }

  protected String getSessionId()
  {
    return getClientRequest().getSessionId();
  }

  public ClientRequestIF getClientRequest()
  {
    return (ClientRequestIF) request.getAttribute(ClientConstants.CLIENTREQUEST);
  }
}
