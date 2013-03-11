package net.eatsense.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import net.eatsense.exceptions.ApiVersionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

/**
 * Checks Api version header in incoming requests and blocks request with older versions to prevent incorrect usage.
 * 
 * @author Nils Weiher
 *
 */
public class ApiVersionFilter implements ContainerRequestFilter,ResourceFilter {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final int acceptedMinVersion;
	private final Set<Integer> acceptedVersions;
	
	
	public ApiVersionFilter( int acceptedMinVersion , List<Integer> acceptedVersions) {
		super();
		this.acceptedMinVersion = acceptedMinVersion;
		if(acceptedVersions != null)
			this.acceptedVersions = ImmutableSet.copyOf(acceptedVersions);
		else
			this.acceptedVersions = ImmutableSet.of();
	}

	@Override
	public ContainerRequest filter(ContainerRequest request) {
		if(request.getHeaderValue("X-AppEngine-TaskName") != null || request.getHeaderValue("X-AppEngine-Cron") != null) {
			// Skip api version check for task queue and cron job requests
			return request;
		}

		String requestApiVersionString = request.getHeaderValue("cloobster-api");
		int requestApiVersionNumber = 1;
		
		if(requestApiVersionString == null) {
			logger.info("No valid \"cloobster-api\" value set in header. Assuming version 1.");
		}
		else {
			requestApiVersionNumber = Integer.parseInt(requestApiVersionString);
		}
			
		if(requestApiVersionNumber < acceptedMinVersion) {
			String message = String.format("Incompatible API version. Request version is %d, but minimum required is %d", requestApiVersionNumber, acceptedMinVersion);
			logger.warn(message);
			throw new ApiVersionException(message, "error.version");
		}
		
		if(!acceptedVersions.isEmpty() && !acceptedVersions.contains(requestApiVersionNumber)) {
			String message = String.format("Incompatible API version. Request version is %d, but required versions are %s", requestApiVersionNumber, acceptedVersions);
			logger.warn(message);
			throw new ApiVersionException(message, "error.version");
		}
		
		return request;
	}

	@Override
	public ContainerRequestFilter getRequestFilter() {
		return this;
	}

	@Override
	public ContainerResponseFilter getResponseFilter() {
		return null;
	}
}
