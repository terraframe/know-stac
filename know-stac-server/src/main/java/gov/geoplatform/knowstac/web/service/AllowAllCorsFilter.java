package gov.geoplatform.knowstac.web.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class AllowAllCorsFilter implements Filter
{
  /**
   * Special thanks to our friends at the University of Oslo (DHIS2)
   * 
   * https://github.com/dhis2/dhis2-core/blob/c8dd0f5f17e49a44b023a6802b9e5b123a6f02da/dhis-2/dhis-web-api/src/main/java/org/hisp/dhis/webapi/filter/CorsFilter.java
   */

  public static final String   CORS_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

  public static final String   CORS_ALLOW_ORIGIN      = "Access-Control-Allow-Origin";

  public static final String   CORS_MAX_AGE           = "Access-Control-Max-Age";

  public static final String   CORS_ALLOW_HEADERS     = "Access-Control-Allow-Headers";

  public static final String   CORS_EXPOSE_HEADERS    = "Access-Control-Expose-Headers";

  public static final String   CORS_REQUEST_HEADERS   = "Access-Control-Request-Headers";

  public static final String   CORS_ALLOW_METHODS     = "Access-Control-Allow-Methods";

  public static final String   CORS_REQUEST_METHOD    = "Access-Control-Request-Method";

  public static final String   CORS_ORIGIN            = "Origin";

  private static final String  EXPOSED_HEADERS        = "ETag, Location";

  private static final Integer MAX_AGE                = 60 * 60;                           // 1hr
                                                                                           // max-age

  private List<String>         whitelist;

  public AllowAllCorsFilter()
  {
    whitelist = Arrays.asList("*");
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
  {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    final String origin = request.getHeader("Origin");

    // Origin header is required for CORS requests
    if (StringUtils.isEmpty(origin))
    {
      chain.doFilter(req, res);
      return;
    }

    if (!this.isOriginWhitelisted(request, origin))
    {
      chain.doFilter(req, res);
      return;
    }

    response.addHeader(CORS_ALLOW_CREDENTIALS, "true");
    response.addHeader(CORS_ALLOW_ORIGIN, origin);
    response.addHeader("Vary", CORS_ORIGIN);

    if (isPreflight(request))
    {
      String requestHeaders = request.getHeader(CORS_REQUEST_HEADERS);
      String requestMethod = request.getHeader(CORS_REQUEST_METHOD);

      response.addHeader(CORS_ALLOW_METHODS, requestMethod);
      response.addHeader(CORS_ALLOW_HEADERS, requestHeaders);
      response.addHeader(CORS_MAX_AGE, String.valueOf(MAX_AGE));

      response.setStatus(HttpServletResponse.SC_NO_CONTENT);

      return;
    }
    else
    {
      response.addHeader(CORS_EXPOSE_HEADERS, EXPOSED_HEADERS);
    }

    chain.doFilter(req, res);
  }

  private boolean isPreflight(HttpServletRequest request)
  {
    return "OPTIONS".equals(request.getMethod()) && !StringUtils.isEmpty(request.getHeader(CORS_ORIGIN)) && !StringUtils.isEmpty(request.getHeader(CORS_REQUEST_METHOD));
  }

  private boolean isOriginWhitelisted(HttpServletRequest request, final String origin)
  {
    final boolean isWhitelistAll = whitelist.contains("*");

    if (isWhitelistAll)
    {
      return true;
    }
    else
    {
      return origin != null && whitelist.contains(origin);
    }
  }

  @Override
  public void init(FilterConfig filterConfig)
  {

  }

  @Override
  public void destroy()
  {

  }

  /**
   * Simple HttpServletRequestWrapper implementation that makes sure that the
   * query string is properly encoded.
   */
  static class HttpServletRequestEncodingWrapper extends HttpServletRequestWrapper
  {
    public HttpServletRequestEncodingWrapper(HttpServletRequest request)
    {
      super(request);
    }

    @Override
    public String getQueryString()
    {
      String queryString = super.getQueryString();

      if (!StringUtils.isEmpty(queryString))
      {
        try
        {
          return URLEncoder.encode(queryString, "UTF-8");
        }
        catch (UnsupportedEncodingException ignored)
        {
        }
      }

      return queryString;
    }
  }
}
