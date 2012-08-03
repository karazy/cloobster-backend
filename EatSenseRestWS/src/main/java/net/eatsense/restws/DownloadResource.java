package net.eatsense.restws;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This resource is used to route incoming gets from 
 * mobile browsers (iOS, Android) to corresponding Cloobster App.
 * The class doesn't represent an actual resource.
 * 
 * @author Frederik Reifschneider
 *
 */
@Path("download")
public class DownloadResource {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Context
	private HttpServletRequest servletRequest;
	
	
	protected final String ANDROID = "Android";	
	protected final String IPHONE = "iPhone";
	protected final String IPOD = "iPod";
	protected final String IPAD = "iPad";
	protected final String GOOGLE_PLAY = "http://play.google.com/store/apps/details?id=net.karazy.cloobster";
	protected final String APPLE_APP_STORE = "http://itunes.apple.com/us/app/cloobster/id532351667?l=de&ls=1&mt=8";
	
	/**
	 * Analyses User-Agent and redirects to corresponding store
	 * if an iOS or Android device is detected.
	 * @return
	 * 	Response
	 */
	@GET
	public Response redirectToStore() {
		String userAgent = servletRequest.getHeader("User-Agent");
		ResponseBuilder resp = Response.noContent();
		try {
			if (userAgent.contains(ANDROID)) {
				// redirect to google play
				resp = Response.seeOther(new URI(GOOGLE_PLAY));
			} else if (userAgent.contains(IPAD) || userAgent.contains(IPHONE) || userAgent.contains(IPOD)) {
				// redirect to apple app store
				resp = Response.seeOther(new URI(APPLE_APP_STORE));
			} else {
				// redirect to cloobster.com
				resp = Response.seeOther(new URI("http://www.cloobster.com"));
			}
		} catch (URISyntaxException e) {
			logger.warn("Could not parse URI.", e);
		}
		
		return resp.build();
		
	}

}
