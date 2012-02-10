package net.eatsense.restws;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import net.eatsense.controller.CheckInController;
import net.eatsense.controller.ImportController;
import net.eatsense.controller.MenuController;
import net.eatsense.domain.Restaurant;
import net.eatsense.domain.User;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.MenuDTO;
import net.eatsense.representation.ProductDTO;
import net.eatsense.representation.RestaurantDTO;
import net.eatsense.util.DummyDataDumper;

import com.google.inject.Inject;

/**
 * Provides a restful interface to access restaurants. That could be optaining
 * informations to a restaurant, checkIn etc.
 * 
 * @author Frederik Reifschneider
 * 
 */
@Path("/restaurants")
public class RestaurantResource{

	private RestaurantRepository restaurantrepo;
	private DummyDataDumper ddd;
	private CheckInController checkInCtr;
	private MenuController menuCtr;
	private ImportController importCtr;

	@Inject
	public RestaurantResource(RestaurantRepository repo, CheckInController checkInCtr, DummyDataDumper ddd, MenuController menuCtr, ImportController importCtr) {
		this.restaurantrepo = repo;
		this.checkInCtr = checkInCtr;
		this.menuCtr = menuCtr;
		this.ddd = ddd;
		this.importCtr = importCtr;
	}

	/**
	 * Intent to check in a restaurant.
	 * 
	 * @param code
	 *            barcode
	 * @return {@link CheckInDTO} information providing status etc.
	 */
	@GET
	@Produces("application/json; charset=UTF-8")
	@Path("spot/{code}")
	public CheckInDTO checkInIntent(@PathParam("code") String code) {
		return checkInCtr.checkInIntent(code);
	}

	/**
	 * A real check in after user confirmed his wish to check in.
	 * 
	 * @param code
	 */
	@PUT
	@Path("spot/{userId}")
	@Produces("application/json; charset=UTF-8")
	@Consumes("application/json; charset=UTF-8")
	public CheckInDTO checkIn(@PathParam("userId") String userId, CheckInDTO checkIn) { 
		
		return checkInCtr.checkIn(userId, checkIn);
	}
	
	/**
	 * Loads other users checkedIn at this spot.
	 * @param userId
	 * @return collection of checkedIn users
	 */
	@GET
	@Path("spot/users/")
	@Produces("application/json; charset=UTF-8")
	public Collection<User> getUsersAtSpot(@QueryParam("userId") String userId) { 
		return checkInCtr.getUsersAtSpot(userId);
	}

	/**
	 * Loads other users checkedIn at this spot.
	 * @param userId
	 */
	@POST
	@Path("spot/users/")
	public void linkToUser(@FormParam(value = "userId") String userId,@FormParam(value = "linkedUserId") String linkedUserId) { 
		checkInCtr.linkToUser(userId, linkedUserId);
	}

	
	/**
	 * Called if user cancels checkIn
	 * @param userId
	 */
	@DELETE
	@Path("spot/{userId}")
	public void cancelCheckIn(@PathParam("userId") String userId) {
		checkInCtr.cancelCheckIn(userId);
	}

	/**
	 * Returns a list of all restaurants
	 * 
	 * @return all restaurants
	 */
	@GET
	@Produces("application/json; charset=UTF-8")
	public Collection<Restaurant> listAll() {
		Collection<Restaurant> list =  restaurantrepo.getAll();
		return list;
	}
	
	@GET
	@Path("{restaurantId}/menu")
	@Produces("application/json; charset=UTF-8")
	public Collection<MenuDTO> getMenus(@PathParam("restaurantId") Long restaurantId)
	{
		return menuCtr.getMenus(restaurantId);
	}
	

	@GET
	@Path("{restaurantId}/products")
	@Produces("application/json; charset=UTF-8")
	public Collection<ProductDTO> getAll(@PathParam("restaurantId")Long restaurantId) {
		return menuCtr.getAllProducts(restaurantId);
	}
	
	@GET
	@Path("{restaurantId}/products/{productId}")
	@Produces("application/json; charset=UTF-8")
	public ProductDTO getProduct(@PathParam("restaurantId")Long restaurantId, @PathParam("productId") Long productId) {
	    return menuCtr.getProduct(restaurantId, productId);
	}

	
	@PUT
	@Path("dummies")
	public void dummyData() {
		ddd.generateDummyRestaurants();
	}
	
	@PUT
	@Path("import")
	@Consumes("application/json; charset=UTF-8")
	@Produces("text/plain; charset=UTF-8")
	public String importNewRestaurant(RestaurantDTO newRestaurant ) {
		Long id =  importCtr.addRestaurant(newRestaurant);
		
		if(id == null)
			return "Error:\n" + importCtr.getReturnMessage();
		else
		    return id.toString();
	}
	
	/**
	 * ATTENTION! THIS METHOD IS DANGEROUS AND SHOULD NOT MAKE IT INTO PRODUCTION
	 * {@link ImportController#deleteAllData()}
	 */
	@GET
	@Path("deleteall")
	public void deleteAllData() {
		importCtr.deleteAllData();
	}

}
