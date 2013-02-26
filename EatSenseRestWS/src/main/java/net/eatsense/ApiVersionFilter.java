package net.eatsense;

import java.util.HashSet;
import java.util.Set;

import net.eatsense.exceptions.ApiVersionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * Checks Api version header in incoming requests and blocks request with older versions to prevent incorrect usage.
 * 
 * @author Nils Weiher
 *
 */
public class ApiVersionFilter implements ContainerRequestFilter {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final static ImmutableSet<String> ignorePrefixes = ImmutableSet.of("uploads","download","cron","tasks","cron","_ah");
	
	@Override
	public ContainerRequest filter(ContainerRequest request) {
		if(request.getHeaderValue("X-AppEngine-TaskName") != null || request.getHeaderValue("X-AppEngine-Cron") != null) {
			// Skip api version check for task queue and cron job requests
			return request;
		}
		
		if(!request.getPathSegments().isEmpty() && ignorePrefixes.contains(request.getPathSegments().get(0).getPath())) {
			// Skip api version check for task queue access
			return request;
		}
		String appApiVersion = request.getHeaderValue("cloobster-api");
		
		if(appApiVersion == null) {
			appApiVersion = "1";
			logger.info("No valid \"cloobster-api\" value set in header. Assuming version 1.");
		}
		
		String systemApiVersion = System.getProperty("net.karazy.api.version");
		
		if(!appApiVersion.equals(systemApiVersion)) {
			String message = String.format("Incompatible API version. Request version is %s, but system version is %s", appApiVersion, systemApiVersion);
			logger.warn(message);
			throw new ApiVersionException(message,"error.version");
		}
			
		return request;
	}
}
