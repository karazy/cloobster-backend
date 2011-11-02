package net.eatsense.restws;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import net.eatsense.domain.Restaurant;
import net.eatsense.persistence.RestaurantRepository;

import com.google.inject.Inject;

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
		// return "Hello eatsense";
		if (code != null && code.length() > 0) {
			Restaurant restaurant = restaurantrepo.findByBarcode(code);
			if (restaurant != null) {
				return "You are checked in at " + restaurant.getName();
			} else {
				return "Code not found";
			}
		} else {
			return "No code submited";
		}

	}

}
