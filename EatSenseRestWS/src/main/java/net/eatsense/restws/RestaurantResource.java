package net.eatsense.restws;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import net.eatsense.persistence.RestaurantRepository;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

@Path("/restaurant")
public class RestaurantResource {
	
	private RestaurantRepository restaurantrepo;
	
	@Inject
	public RestaurantResource(RestaurantRepository repo) {
		this.restaurantrepo = repo;
	}
	
	@GET
	@Produces("text/plain")
	public String checkIn(@QueryParam("code") String code) {
		
//		restaurantrepo.
		
		
		return "Hello CheckIn " + code;
	}
	
	
	

}
