package net.eatsense.restws;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/restaurant")
public class RestaurantResource {
	
	@GET
	public String checkIn() {
		
		return "Hello CheckIn";
	}
	
	public RestaurantResource() {
	
	}

}
