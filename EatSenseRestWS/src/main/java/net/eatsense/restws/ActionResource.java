package net.eatsense.restws;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import net.eatsense.configuration.addon.AddonConfiguration;
import net.eatsense.controller.ConfigurationController;
import net.eatsense.util.DeviceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Handles incoming requests with prefix /x/{Action}
 * Will analyze and create a redirect based on {@link DeviceType}
 * attempts to start cloobster (or whitelabel) app and execute desired action.
 * 
 * @author Frederik Reifschneider
 *
 */
@Path("x")
public class ActionResource {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Default redirect URL
	 */
	private static final String REDIRECT_URL_PREFIX = "cloobster://";
	
	/**
	 * Prefix for invoking the action.
	 */
	public final static String URL_PREFIX = "/x/";
	
	private final ConfigurationController cfgCtrl;
	
	@Context
	private HttpServletRequest servletRequest;
	@Context
	private HttpServletResponse servletResponse;
	
	@Inject
	public ActionResource(ConfigurationController cfgCtrl) {
		this.cfgCtrl = cfgCtrl;
	}
	
	/**
	 * Invoked by all URLS Starting with /x/spot.
	 * Attempts to start the app when installed, otherwise or when desktop browser will do a redirect do download url.
	 * @param code
	 * 	Spot QR code to do checkin with.
	 * @return
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	@GET
	@Path("spot/{code}")
	@Produces(MediaType.TEXT_HTML)
	public Response doAppCheckIn(@PathParam("code") String code) throws IOException, URISyntaxException {
		ResponseBuilder resp = Response.noContent();

		
		String userAgent = servletRequest.getHeader("User-Agent");	
		String redirectUrl = "http://www.cloobster.com/";
		String downloadUrl = "http://www.cloobster.com/download";

		
		if (userAgent.contains(DeviceType.IPHONE)
				|| userAgent.contains(DeviceType.IPAD)
				|| userAgent.contains(DeviceType.IPOD)) {
			
			
			
			StringBuffer html = new StringBuffer();
			
			//get Whitelabelconfiguration to determine the url scheme
			AddonConfiguration wlCfg = null;
			
			try {
				wlCfg = cfgCtrl.getWhitelabelConfigurationBySpot(code);
			} catch(net.eatsense.exceptions.NotFoundException e) {
				logger.warn(e.getMessage());
			}
			
			if(wlCfg != null && wlCfg.getConfigMap().containsKey("iosUrlScheme") && wlCfg.getConfigMap().get("iosUrlScheme") != null) {
				redirectUrl = wlCfg.getConfigMap().get("iosUrlScheme") + "spot/" + code;
			} else {
				redirectUrl = REDIRECT_URL_PREFIX + "spot/" + code;
			}
			
			//generate a simple html page. tries to start cloobster via url scheme and given action. 
			//if no handler is present, will redirect to download url to load the app
			html.append("<!DOCTYPE html><html><head></head><body><script type='text/javascript'>");
			html.append("setTimeout(function() {");
			html.append("window.location = '"+downloadUrl + "/" + code + "#" + code + "';");
			html.append("}, 100);");
			html.append("window.location = '" + redirectUrl + "'");
			html.append("</script></body></html>");
			resp = Response.ok(html.toString(), MediaType.TEXT_HTML);
		} else if(userAgent.contains(DeviceType.ANDROID)) {
			//Android App will handle everything via download url
			redirectUrl = downloadUrl + "/" + code + "#" + code;
			//use same approach like iOS. A sendRedirect didn't work!!! But here we only need one url.
			StringBuffer html = new StringBuffer();
			html.append("<!DOCTYPE html><html><head></head><body><script type='text/javascript'>");
			html.append("window.location = '" + redirectUrl + "'");
			html.append("</script></body></html>");
			resp = Response.ok(html.toString(), MediaType.TEXT_HTML);
			
		} else {
			//Desktop and other mobile Platforms, append spot to redirect to correct website
			redirectUrl = downloadUrl  + "/" + code + "#" + code;
			resp = Response.seeOther(new URI(redirectUrl));
		}
		
		logger.info("Redirecting to " + redirectUrl);
		return resp.build();
	}

}
