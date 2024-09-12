package gov.geoplatform.knowstac.web.service;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.web.ServletUtility;
import com.runwaysdk.web.WebClientSession;

import net.geoprism.registry.service.LoginBruteForceGuardService;
import net.geoprism.registry.service.request.SessionServiceIF;

@Component("sessionFilter")
public class SessionFilter implements Filter
{
  @Autowired
  protected SessionServiceIF            sessionService;

  @Autowired
  protected LoginBruteForceGuardService loginGuard;

  public void init(FilterConfig filterConfig) throws ServletException
  {
  }

  public void destroy()
  {
  }

  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
  {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    // response time logging
    req.setAttribute("startTime", (Long) ( new Date().getTime() ));

    HttpSession session = request.getSession();

    WebClientSession clientSession = (WebClientSession) session.getAttribute(ClientConstants.CLIENTSESSION);

    boolean loggedIn = clientSession != null && clientSession.getRequest() != null && clientSession.getRequest().isSessionValid();

    if (!loggedIn)
    {
      Locale[] locales = ServletUtility.getLocales(request);

      clientSession = WebClientSession.createAnonymousSession(locales);

      request.getSession().setMaxInactiveInterval(CommonProperties.getSessionTime());
      request.getSession().setAttribute(ClientConstants.CLIENTSESSION, clientSession);
    }

    req.setAttribute(ClientConstants.CLIENTREQUEST, clientSession.getRequest());

    chain.doFilter(req, res);
    
    return;
  }
}
