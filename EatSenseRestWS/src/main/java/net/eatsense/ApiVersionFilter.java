package net.eatsense;

import net.eatsense.exceptions.ApiVersionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	@Override
	public ContainerRequest filter(ContainerRequest request) {
		String appApiVersion = request.getHeaderValue("cloobster-api");
		
		if(appApiVersion == null) {
			appApiVersion = "1";
			logger.warn("No valid \"cloobster-api\" value set in header. Assuming version 1.");
		}
		
		String systemApiVersion = System.getProperty("net.karazy.api.version");
		
		if(!appApiVersion.equals(systemApiVersion)) {
			String message = String.format("Incompatible API version. Request version is %s, but system version is %s", appApiVersion, systemApiVersion);
			logger.error(message);
			throw new ApiVersionException(message,"error.version");
		}
		
		return request;
	}
}
