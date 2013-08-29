package net.eatsense.restws;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import net.eatsense.configuration.addon.AddonConfigurationService;
import net.eatsense.controller.CompanyController;
import net.eatsense.controller.LocationController;
import net.eatsense.counter.CounterService;
import net.eatsense.counter.Counter.PeriodType;
import net.eatsense.domain.Business;
import net.eatsense.restws.business.CompanyResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

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
	
	
	public final static String ANDROID = "Android";	
	public final static String IPHONE = "iPhone";
	public final static String IPOD = "iPod";
	public final static String IPAD = "iPad";
	public final static String GOOGLE_PLAY = "http://play.google.com/store/apps/details?id=net.karazy.cloobster";
	public final static String APPLE_APP_STORE = "http://itunes.apple.com/us/app/cloobster/id532351667?l=de&ls=1&mt=8";

	protected final CounterService counters;
	private final CompanyController companyCtrl;
	private final LocationController locationCtrl;
	private final AddonConfigurationService addonConfig;
	
	@Inject
	public DownloadResource(CounterService counters, CompanyController companyCtrl, LocationController locationCtrl, AddonConfigurationService addonConfig) {
		this.counters = counters;
		this.companyCtrl = companyCtrl;
		this.locationCtrl = locationCtrl;
		this.addonConfig = addonConfig;
	}
	
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
			Date now = new Date();
			if (userAgent.contains(ANDROID)) {
				// redirect to google play
				resp = Response.seeOther(new URI(GOOGLE_PLAY));				
				counters.loadAndIncrementCounter("download-android", PeriodType.DAY, now, 0, 0, 1);
				counters.loadAndIncrementCounter("download-android", PeriodType.ALL, null, 0, 0, 1);
			} else if (userAgent.contains(IPAD) || userAgent.contains(IPHONE) || userAgent.contains(IPOD)) {
				// redirect to apple app store
				resp = Response.seeOther(new URI(APPLE_APP_STORE));
				counters.loadAndIncrementCounter("download-ios", PeriodType.DAY, now, 0, 0, 1);
				counters.loadAndIncrementCounter("download-ios", PeriodType.ALL, null, 0, 0, 1);
			} else {
				// redirect to cloobster.com
				resp = Response.seeOther(new URI("http://www.cloobster.com"));
			}
		} catch (URISyntaxException e) {
			logger.warn("Could not parse URI.", e);
		}
		
		return resp.build();
	}
	
	@GET
	@Path("{spotCode}")
	public Response redirectToStoreByWhitelabel(@PathParam("spotCode") String spotCode) {
//		if(spotCode == null) {
//			logger.warn();
//			return redirectToStore();			
//		}
		
		Business loc = locationCtrl.getLocationBySpotCode(spotCode);
		
		if(loc == null) {
			//redirect to default download url
			logger.warn("No location found. Redirect to default download URL.");
			return redirectToStore();	
		}
		
		Map<String, String> config = addonConfig.get("whitelabel", loc.getCompany().getRaw()).getConfigMap();
		
		if(config == null) {
			logger.warn("No whitelabel configuration found. Redirect to default download URL.");
			return redirectToStore();	
		}
		
		String whitelabel = config.get("key");
		
		if(whitelabel.equals("net.karazy.cloobster")) {
			return redirectToStore();	
		} else if(whitelabel.equals("net.karazy.cloobster.frizz")) {
			
		} else if(whitelabel.equals("net.karazy.cloobster.darmstadt")) {
			
		}
		
		
		return null;
	}

}
