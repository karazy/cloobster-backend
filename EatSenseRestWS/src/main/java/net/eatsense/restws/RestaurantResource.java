package net.eatsense.restws;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import net.eatsense.controller.BillController;
import net.eatsense.controller.ImportController;
import net.eatsense.controller.MenuController;
import net.eatsense.controller.OrderController;
import net.eatsense.controller.BusinessController;
import net.eatsense.domain.Restaurant;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.representation.BillDTO;
import net.eatsense.representation.MenuDTO;
import net.eatsense.representation.OrderDTO;
import net.eatsense.representation.ProductDTO;
import net.eatsense.representation.RestaurantDTO;
import net.eatsense.representation.cockpit.SpotStatusDTO;
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

	private RestaurantRepository restaurantRepo;
	private DummyDataDumper ddd;
	private MenuController menuCtrl;
	private ImportController importCtrl;
	private OrderController orderCtrl;
	private BillController billCtrl;
	private BusinessController restaurantCtrl;

	@Inject
	public RestaurantResource(BusinessController restaurantCtr, RestaurantRepository repo, DummyDataDumper ddd, MenuController menuCtr, ImportController importCtr, OrderController orderCtr, BillController billCtr) {
		this.restaurantRepo = repo;
		this.restaurantCtrl = restaurantCtr;
		this.menuCtrl = menuCtr;
		this.ddd = ddd;
		this.importCtrl = importCtr;
		this.orderCtrl = orderCtr;
		this.billCtrl = billCtr;
	}

	/**
	 * Returns a list of all restaurants
	 * 
	 * @return all restaurants
	 */
	@GET
	@Produces("application/json; charset=UTF-8")
	public Collection<Restaurant> listAll() {
		Collection<Restaurant> list =  restaurantRepo.getAll();
		return list;
	}
	
	@GET
	@Path("{restaurantId}/spots")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({"restaurantadmin"})
	public Collection<SpotStatusDTO> getSpotCockpitInformation(@PathParam("restaurantId") Long restaurantId) throws Exception {
		return restaurantCtrl.getSpotStatusData(restaurantId);
	}
	
	@GET
	@Path("{restaurantId}/menus")
	@Produces("application/json; charset=UTF-8")
	public Collection<MenuDTO> getMenus(@PathParam("restaurantId") Long restaurantId)
	{
		return menuCtrl.getMenus(restaurantId);
	}
	

	@GET
	@Path("{restaurantId}/products")
	@Produces("application/json; charset=UTF-8")
	public Collection<ProductDTO> getAll(@PathParam("restaurantId")Long restaurantId) {
		return menuCtrl.getAllProducts(restaurantId);
	}
	
	@GET
	@Path("{restaurantId}/products/{productId}")
	@Produces("application/json; charset=UTF-8")
	public ProductDTO getProduct(@PathParam("restaurantId")Long restaurantId, @PathParam("productId") Long productId) {
		
		ProductDTO product = menuCtrl.getProduct(restaurantId, productId);
		
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
		orderId = orderCtrl.placeOrder(restaurantId, checkInId, order);	
		return orderId.toString();
	}
	
	
	@GET
	@Path("{restaurantId}/orders")
	@Produces("application/json; charset=UTF-8")
	public Collection<OrderDTO> getOrders(@PathParam("restaurantId")Long restaurantId, @QueryParam("checkInId") String checkInId, @QueryParam("status") String status) {
		Collection<OrderDTO> orders = orderCtrl.getOrdersAsDto(restaurantId, checkInId, status);
		return orders;
	}
	
	@GET
	@Path("{restaurantId}/orders/{orderId}")
	@Produces("application/json; charset=UTF-8")
	public OrderDTO getOrder(@PathParam("restaurantId")Long restaurantId, @PathParam("orderId") Long orderId) {
		OrderDTO order = orderCtrl.getOrderAsDTO(restaurantId, orderId);
		if(order== null)
			throw new NotFoundException();
		return order;
	}
	
	@PUT
	@Path("{restaurantId}/orders/{orderId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public OrderDTO updateOrder(@PathParam("restaurantId")Long restaurantId, @PathParam("orderId") Long orderId, @QueryParam("checkInId") String checkInId, OrderDTO order) {
		return orderCtrl.updateOrder(restaurantId, orderId, order, checkInId);
	}
	
	@DELETE
	@Path("{restaurantId}/orders/{orderId}")
	public void deleteOrder(@PathParam("restaurantId")Long restaurantId, @PathParam("orderId") Long orderId, @QueryParam("checkInId") String checkInId) {
		orderCtrl.deleteOrder(restaurantId, orderId);
	}
	
	@POST
	@Path("{restaurantId}/bills")
	@Produces("application/json; charset=UTF-8")
	@Consumes("application/json; charset=UTF-8")
	public BillDTO createBill(@PathParam("restaurantId")Long restaurantId, BillDTO bill, @QueryParam("checkInId") String checkInId) {
		return billCtrl.createBill(restaurantId, checkInId, bill);
	}
	
	
	@PUT
	@Path("dummies")
	public void dummyData() {
		ddd.generateDummyRestaurants();
	}
	
	@PUT
	@Path("dummieusers")
	public void dummyUsers() {
		ddd.generateDummyUsers();
	}
	
	
	@PUT
	@Path("import")
	@Consumes("application/json; charset=UTF-8")
	@Produces("text/plain; charset=UTF-8")
	public String importNewRestaurant(RestaurantDTO newRestaurant ) {
		Long id =  importCtrl.addRestaurant(newRestaurant);
		
		if(id == null)
			return "Error:\n" + importCtrl.getReturnMessage();
		else
		    return id.toString();
	}
	
	
	
	/**
	 * ATTENTION! THIS METHOD IS DANGEROUS AND SHOULD NOT MAKE IT INTO PRODUCTION
	 * {@link ImportController#deleteAllData()}
	 * Also recreates the dummy users.
	 */
	@DELETE
	@Path("all")
	public void deleteAllData() {
		//TODO DELETE
		importCtrl.deleteAllData();
	}
	
	/**
	 * ATTENTION! THIS METHOD IS DANGEROUS AND SHOULD NOT MAKE IT INTO PRODUCTION
	 * {@link ImportController#deleteAllData()}
	 */
	@DELETE
	@Path("livedata")
	public void deleteLiveData() {
		//TODO DELETE
		importCtrl.deleteLiveData();
	}

}
