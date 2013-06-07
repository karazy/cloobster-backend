package net.eatsense.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.eatsense.restws.DownloadResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet Filter implementation class IOsUrlSchemeFilter
 */
public class IOsUrlSchemeFilter implements Filter {
	private static final String REDIRECT_URL_PREFIX = "cloobster://";

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public final static String URL_PREFIX = "/x/";

    /**
     * Default constructor. 
     */
    public IOsUrlSchemeFilter() {
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			HttpServletRequest servletRequest = (HttpServletRequest) request;
			
			String userAgent = servletRequest.getHeader("User-Agent");
			StringBuffer url = servletRequest.getRequestURL();
			String queryString = servletRequest.getQueryString();
			if (queryString != null) {
			    url.append('?');
			    url.append(queryString);
			}
			String requestUrl = url.toString();
			
			String redirectUrl = "http://www.cloobster.com/";
			
			if (userAgent.contains(DownloadResource.ANDROID)
					|| userAgent.contains(DownloadResource.IPHONE)
					|| userAgent.contains(DownloadResource.IPAD)
					|| userAgent.contains(DownloadResource.IPOD)) {
				String suffix = requestUrl.substring(requestUrl
						.indexOf(URL_PREFIX) + URL_PREFIX.length());
				
				redirectUrl = REDIRECT_URL_PREFIX + suffix;
			}

			HttpServletResponse httpResponse = (HttpServletResponse) response;
			httpResponse.sendRedirect(redirectUrl);
			logger.info("Redirecting to " + redirectUrl);
			return;
		}

		// pass the request along the filter chain
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}

}
