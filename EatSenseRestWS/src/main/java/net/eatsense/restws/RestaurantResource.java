package net.eatsense.restws;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import net.eatsense.controller.CheckInController;
import net.eatsense.domain.Restaurant;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.representation.CheckIn;
import net.eatsense.util.DummyDataDumper;

import com.google.inject.Inject;

/**
 * Provides a restful interface to access restaurants. That could be optaining
 * informations to a restaurant, checkIn etc.
 * 
 * @author Frederik Reifschneider
 * 
 */
@Path("/restaurant")
public class RestaurantResource {

	private RestaurantRepository restaurantrepo;
	private DummyDataDumper ddd;
	private CheckInController checkInCtr;

	@Inject
	public RestaurantResource(RestaurantRepository repo, CheckInController checkInCtr, DummyDataDumper ddd) {
		this.restaurantrepo = repo;
		this.checkInCtr = checkInCtr;
		this.ddd = ddd;
	}

	/**
	 * Intent to check in a restaurant.
	 * 
	 * @param code
	 *            barcode
	 * @return {@link CheckIn} information providing status etc.
	 */
	@GET
	@Produces("application/json")
	@Path("spot/{code}")
	public CheckIn checkInIntent(@PathParam("code") String code) {
		return checkInCtr.checkInIntent(code);
	}

	/**
	 * A real check in after user confirmed his wish to check in.
	 * 
	 * @param code
	 */
	@PUT
	@Path("spot/{code}")
	public void checkIn(@PathParam("code") String code) {
		checkInCtr.checkIn(code);
	}

	/**
	 * Returns a list of all restaurants
	 * 
	 * @return all restaurants
	 */
	@GET
	@Produces("application/json")
	public String listAll() {
		Collection<Restaurant> list = restaurantrepo.getAll(Restaurant.class);
		StringBuffer sb = new StringBuffer();
		sb.append("Restaurants: ");
		for (Restaurant restaurant : list) {
			sb.append(restaurant.getName());
			sb.append(" - ");
		}
		return sb.toString();
	}

	@PUT
	@Path("dummies")
	public void dummyData() {
		ddd.generateDummyRestaurants();
	}

}
