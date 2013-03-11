package net.eatsense.filter;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

public class CrossOriginResourceSharingFilter implements
		ContainerResponseFilter {

	@Override
	public ContainerResponse filter(ContainerRequest request,
			ContainerResponse response) {
		String requestedHeader = request.getHeaderValue("Access-Control-Request-Headers");
		if(requestedHeader == null)
        	requestedHeader = "origin, x-requested-with";
		
		response.getHttpHeaders().putSingle("Access-Control-Allow-Origin", "*");
		response.getHttpHeaders().putSingle("Access-Control-Allow-Credentials", "true");
        response.getHttpHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        response.getHttpHeaders().putSingle("Access-Control-Allow-Headers", requestedHeader);
        response.getHttpHeaders().putSingle("Access-Control-Max-Age", "1728000");
        
        if(request.getMethod().equals("OPTIONS")) {
        	// This was an OPTIONS request, which should contain no data.
        	response.setStatus(204);
        }
        return response;
	}
}
