package net.eatsense.restws;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import net.eatsense.configuration.Configuration;
import net.eatsense.configuration.addon.AddonConfiguration;
import net.eatsense.configuration.addon.AddonConfigurationService;
import net.eatsense.controller.CompanyController;
import net.eatsense.controller.LocationController;
import net.eatsense.counter.Counter.PeriodType;
import net.eatsense.counter.CounterService;
import net.eatsense.domain.Business;

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
	
	/**
	 * Default value Google Play redirect url.
	 */
	public final static String GOOGLE_PLAY = "http://play.google.com/store/apps/details?id=net.karazy.cloobster";
	/**
	 * Default value Apple App Store redirect url.
	 */
	public final static String APPLE_APP_STORE = "http://itunes.apple.com/us/app/cloobster/id532351667?l=de&ls=1&mt=8";

	protected final CounterService counters;
	private final CompanyController companyCtrl;
	private final LocationController locationCtrl;
	private final AddonConfigurationService addonConfig;
	private final Iterable<AddonConfiguration> whitelabelCfg;
	private final Configuration config;
	
	//TODO 2013-08-29 Fred maybe move logic into custom controller
	
	@Inject
	public DownloadResource(CounterService counters, CompanyController companyCtrl, LocationController locationCtrl, AddonConfigurationService addonConfig,
			Configuration config) {
		this.counters = counters;
		this.companyCtrl = companyCtrl;
		this.locationCtrl = locationCtrl;
		this.addonConfig = addonConfig;
		this.config = config;
		
		whitelabelCfg = addonConfig.getAll(config.getWhitelabels().getRaw(), false);
		
	}
	
	/**
	 * Analyses User-Agent and redirects to corresponding store
	 * if an iOS or Android device is detected.
	 * @param whitelabel
	 *  If given uses whitelabel to determine correct redirect.
	 * @return
	 * 	Response
	 */
	public Response redirectToStore(AddonConfiguration whitelabel) {
		String userAgent = servletRequest.getHeader("User-Agent");
		ResponseBuilder resp = Response.noContent();
		String googlePlayURI = GOOGLE_PLAY;
		String appleAppStoreURI = APPLE_APP_STORE;
		String desktopURI = "http://www.cloobster.com"; 
		
		//use redirect urls from whitelabel
		if(whitelabel != null) {
			if(whitelabel.getConfigMap().containsKey("ios")) {
				appleAppStoreURI = whitelabel.getConfigMap().get("ios");
			}
			
			if(whitelabel.getConfigMap().containsKey("android")) {
				googlePlayURI = whitelabel.getConfigMap().get("android");
			}
			
			if(whitelabel.getConfigMap().containsKey("desktop")) {
				desktopURI = whitelabel.getConfigMap().get("desktop");
			}
		}
		
		try {
			Date now = new Date();
			if (userAgent.contains(ANDROID)) {
				// redirect to google play
				resp = Response.seeOther(new URI(googlePlayURI));				
				counters.loadAndIncrementCounter("download-android", PeriodType.DAY, now, 0, 0, 1);
				counters.loadAndIncrementCounter("download-android", PeriodType.ALL, null, 0, 0, 1);
			} else if (userAgent.contains(IPAD) || userAgent.contains(IPHONE) || userAgent.contains(IPOD)) {
				// redirect to apple app store
				resp = Response.seeOther(new URI(appleAppStoreURI));
				counters.loadAndIncrementCounter("download-ios", PeriodType.DAY, now, 0, 0, 1);
				counters.loadAndIncrementCounter("download-ios", PeriodType.ALL, null, 0, 0, 1);
			} else {
				// redirect to cloobster.com
				resp = Response.seeOther(new URI(desktopURI));
			}
		} catch (URISyntaxException e) {
			logger.warn("Could not parse URI.", e);
		}
		
		return resp.build();
	}
	
	@GET
	public Response redirectToStore() {
		return redirectToStore(null);
	}
	
	/**
	 * Does a redirect by taking the configured whitelabel into account.
	 * @param spotCode
	 * 	QR Code of Spot
	 * @return
	 */
	@GET
	@Path("{spotCode}")
	public Response redirectToStoreByWhitelabel(@PathParam("spotCode") String spotCode) {
		if(spotCode == null) {
			logger.warn("SpotCode was null.");
			return redirectToStore();			
		}
		
		//TODO use configuration controller to get whitelabel
		
		Business loc = locationCtrl.getLocationBySpotCode(spotCode);
		AddonConfiguration tempCfg;
		AddonConfiguration whitelabelContainer;
		
		if(loc == null) {
			//redirect to default download url
			logger.warn("No location found. Redirect to default download URL.");
			return redirectToStore();
		}
		
		if(loc.getCompany() == null) {
			logger.warn("Location has no company. Redirect to default download URL.");
			return redirectToStore();
		}
		
		whitelabelContainer = addonConfig.get("whitelabel", loc.getCompany().getRaw());
		
		if(whitelabelContainer == null) {
			logger.warn("Could not optain whitelabel from company configuration.");
			return redirectToStore();
		}
		
		Map<String, String> config = whitelabelContainer.getConfigMap();
		
		if(config == null) {
			logger.warn("No whitelabel configuration found. Redirect to default download URL.");
			return redirectToStore();	
		}
		
		String whitelabel = config.get("key");
		
		while(whitelabelCfg.iterator().hasNext()) {
			tempCfg = whitelabelCfg.iterator().next();
			//check if configuration contains a key and value for that key, then check if selected whitelabel matches the config map value
			if(tempCfg.getConfigMap().containsKey("key") && tempCfg.getConfigMap().containsValue(whitelabel) && tempCfg.getConfigMap().get("key").equals(whitelabel)) {
				return redirectToStore(tempCfg);
			}
		}
		
		return redirectToStore(null);
	}

}
