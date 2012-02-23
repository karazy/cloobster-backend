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
import javax.ws.rs.core.MediaType;

import net.eatsense.controller.CheckInController;
import net.eatsense.controller.ImportController;
import net.eatsense.controller.MenuController;
import net.eatsense.controller.OrderController;
import net.eatsense.domain.Restaurant;
import net.eatsense.domain.User;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.MenuDTO;
import net.eatsense.representation.OrderDTO;
import net.eatsense.representation.ProductDTO;
import net.eatsense.representation.RestaurantDTO;
import net.eatsense.util.DummyDataDumper;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;

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
	private OrderController orderCtr;

	@Inject
	public RestaurantResource(RestaurantRepository repo, CheckInController checkInCtr, DummyDataDumper ddd, MenuController menuCtr, ImportController importCtr, OrderController orderCtr) {
		this.restaurantrepo = repo;
		this.checkInCtr = checkInCtr;
		this.menuCtr = menuCtr;
		this.ddd = ddd;
		this.importCtr = importCtr;
		this.orderCtr = orderCtr;
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
	@Path("{restaurantId}/menus")
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
		
		ProductDTO product = menuCtr.getProduct(restaurantId, productId);
		
		if(product == null)
			throw new NotFoundException(productId + " id not found.");
		else return product;
	}
	
	@POST
	@Path("{restaurantId}/orders")
	@Produces("text/plain; charset=UTF-8")
	@Consumes("application/json; charset=UTF-8")
	public String placeOrder(@PathParam("restaurantId")Long restaurantId, OrderDTO order, @QueryParam("checkInId") String checkInId) {
		Long orderId = null;
		orderId = orderCtr.placeOrder(restaurantId, checkInId, order);	
		return orderId.toString();
	}
	
	@GET
	@Path("{restaurantId}/orders")
	@Produces("application/json; charset=UTF-8")
	public Collection<OrderDTO> getOrder(@PathParam("restaurantId")Long restaurantId, @QueryParam("checkInId") String checkInId) {
		Collection<OrderDTO> orders = orderCtr.getOrdersForCheckIn(restaurantId, checkInId);
		return orders;
	}
	
	
	@GET
	@Path("{restaurantId}/orders/{orderId}")
	@Produces("application/json; charset=UTF-8")
	public OrderDTO getOrder(@PathParam("restaurantId")Long restaurantId, @PathParam("orderId") Long orderId) {
		OrderDTO order = orderCtr.getOrder(restaurantId, orderId);
		if(order== null)
			throw new NotFoundException();
		return order;
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
