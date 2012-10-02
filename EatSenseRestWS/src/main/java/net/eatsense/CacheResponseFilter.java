package net.eatsense;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

/**
 * Sets outgoing response header for cache control.
 * Initally for a workaround due to a iOs6 bug.
 * @author Nils Weiher
 *
 */
public class CacheResponseFilter implements ContainerResponseFilter {

	@Override
	public ContainerResponse filter(ContainerRequest request,
			ContainerResponse response) {
		// Disable caching for all api calls, because of current iOs6 behaviour that caches POST requests to the server.
		// （╯°□°）╯︵ ┻━┻
		response.getHttpHeaders().putSingle("Cache-Control", "no-cache");
		return response;
	}
}
