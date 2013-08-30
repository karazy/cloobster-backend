package net.eatsense.filter;

import java.io.IOException;
import java.io.PrintWriter;

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
	public final static String URL_PREFIX = "/x/";

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
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
			String downloadUrl = "http://www.cloobster.com/download";
			
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			
			if (userAgent.contains(DownloadResource.IPHONE)
					|| userAgent.contains(DownloadResource.IPAD)
					|| userAgent.contains(DownloadResource.IPOD)) {
				String suffix = requestUrl.substring(requestUrl
						.indexOf(URL_PREFIX) + URL_PREFIX.length());
				
				redirectUrl = REDIRECT_URL_PREFIX + suffix;
				
				PrintWriter pw = httpResponse.getWriter();
				StringBuffer html = new StringBuffer();
				//generate a simple html page. tries to start cloobster via url scheme and given action. 
				//if no handler is present, will redirect to download url to load the app
				html.append("<!DOCTYPE html><html><head></head><body><script type='text/javascript'>");
				html.append("setTimeout(function() {");
				html.append("window.location = '"+downloadUrl + "/" + suffix + "#" + suffix + "';");
				html.append("}, 100);");
				html.append("window.location = '" + redirectUrl + "'");
				html.append("</script></body></html>");
				pw.print(html.toString());
			} else if(userAgent.contains(DownloadResource.ANDROID)) {
				String suffix = requestUrl.substring(requestUrl
						.indexOf(URL_PREFIX) + URL_PREFIX.length());
				
				//Remove after next update. Android App will handle everything via download url
				//if app is not installed, will redirect to store
				//OLD URL
				//redirectUrl = REDIRECT_URL_PREFIX + suffix;
				redirectUrl = downloadUrl + "/" + suffix + "#" + suffix;
//				httpResponse.sendRedirect(redirectUrl);
				//use same approach like iOS. A sendRedirect won't work. But here we only need one url.
				PrintWriter pw = httpResponse.getWriter();
				StringBuffer html = new StringBuffer();
				html.append("<!DOCTYPE html><html><head></head><body><script type='text/javascript'>");
				html.append("window.location = '" + redirectUrl + "'");
				html.append("</script></body></html>");
				pw.print(html.toString());
			} else {
				httpResponse.sendRedirect(redirectUrl);
			}
			
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
