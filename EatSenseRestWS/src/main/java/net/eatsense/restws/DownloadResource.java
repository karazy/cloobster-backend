package net.eatsense.restws;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import net.eatsense.counter.CounterService;
import net.eatsense.counter.Counter.PeriodType;

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
	
	@Inject
	public DownloadResource(CounterService counters) {
		this.counters = counters;
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

}
