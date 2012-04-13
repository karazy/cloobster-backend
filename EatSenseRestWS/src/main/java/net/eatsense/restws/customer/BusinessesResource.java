package net.eatsense.restws.customer;

import java.util.Collection;

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
import net.eatsense.domain.Business;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.representation.BillDTO;
import net.eatsense.representation.BusinessImportDTO;
import net.eatsense.representation.MenuDTO;
import net.eatsense.representation.OrderDTO;
import net.eatsense.representation.ProductDTO;
import net.eatsense.util.DummyDataDumper;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;

/**
 * Provides a restful interface to access businesses. That could be optaining
 * informations to a business, checkIn etc.
 * 
 * @author Frederik Reifschneider
 * 
 */
@Path("c/businesses")
public class BusinessesResource{

	private BusinessRepository businessRepo;
	private DummyDataDumper ddd;
	private MenuController menuCtrl;
	private ImportController importCtrl;
	private OrderController orderCtrl;
	private BillController billCtrl;

	@Inject
	public BusinessesResource(BusinessRepository repo, DummyDataDumper ddd, MenuController menuCtr, ImportController importCtr, OrderController orderCtr, BillController billCtr) {
		this.businessRepo = repo;
		this.menuCtrl = menuCtr;
		this.ddd = ddd;
		this.importCtrl = importCtr;
		this.orderCtrl = orderCtr;
		this.billCtrl = billCtr;
	}

	/**
	 * Returns a list of all businesses
	 * 
	 * @return all businesses
	 */
	@GET
	@Produces("application/json; charset=UTF-8")
	public Collection<Business> listAll() {
		Collection<Business> list =  businessRepo.getAll();
		return list;
	}
	
	@GET
	@Path("{businessId}/menus")
	@Produces("application/json; charset=UTF-8")
	public Collection<MenuDTO> getMenus(@PathParam("businessId") Long businessId)
	{
		return menuCtrl.getMenus(businessId);
	}
	

	@GET
	@Path("{businessId}/products")
	@Produces("application/json; charset=UTF-8")
	public Collection<ProductDTO> getAll(@PathParam("businessId")Long businessId) {
		return menuCtrl.getAllProducts(businessId);
	}
	
	@GET
	@Path("{businessId}/products/{productId}")
	@Produces("application/json; charset=UTF-8")
	public ProductDTO getProduct(@PathParam("businessId")Long businessId, @PathParam("productId") Long productId) {
		
		ProductDTO product = menuCtrl.getProduct(businessId, productId);
		
		if(product == null)
			throw new NotFoundException(productId + " id not found.");
		else return product;
	}
	
	@POST
	@Path("{businessId}/orders")
	@Produces("text/plain; charset=UTF-8")
	@Consumes("application/json; charset=UTF-8")
	public String placeOrder(@PathParam("businessId")Long businessId, OrderDTO order, @QueryParam("checkInId") String checkInId) {
		Long orderId = null;
		orderId = orderCtrl.placeOrderInCart(businessId, checkInId, order);	
		return orderId.toString();
	}
	
	
	@GET
	@Path("{businessId}/orders")
	@Produces("application/json; charset=UTF-8")
	public Collection<OrderDTO> getOrders(@PathParam("businessId")Long businessId, @QueryParam("checkInId") String checkInId, @QueryParam("status") String status) {
		Collection<OrderDTO> orders = orderCtrl.getOrdersAsDto(businessId, checkInId, status);
		return orders;
	}
	
	@GET
	@Path("{businessId}/orders/{orderId}")
	@Produces("application/json; charset=UTF-8")
	public OrderDTO getOrder(@PathParam("businessId")Long businessId, @PathParam("orderId") Long orderId) {
		OrderDTO order = orderCtrl.getOrderAsDTO(businessId, orderId);
		if(order== null)
			throw new NotFoundException();
		return order;
	}
	
	@PUT
	@Path("{businessId}/orders/{orderId}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public OrderDTO updateOrder(@PathParam("businessId")Long businessId, @PathParam("orderId") Long orderId, @QueryParam("checkInId") String checkInId, OrderDTO order) {
		return orderCtrl.updateOrder(businessId, orderId, order, checkInId);
	}
	
	@DELETE
	@Path("{businessId}/orders/{orderId}")
	public void deleteOrder(@PathParam("businessId")Long businessId, @PathParam("orderId") Long orderId, @QueryParam("checkInId") String checkInId) {
		orderCtrl.deleteOrder(businessId, orderId);
	}
	
	@POST
	@Path("{businessId}/bills")
	@Produces("application/json; charset=UTF-8")
	@Consumes("application/json; charset=UTF-8")
	public BillDTO createBill(@PathParam("businessId")Long businessId, BillDTO bill, @QueryParam("checkInId") String checkInId) {
		return billCtrl.createBill(businessId, checkInId, bill);
	}
	
	
	@PUT
	@Path("dummies")
	public void dummyData() {
		ddd.generateDummyBusinesses();
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
	public String importNewBusiness(BusinessImportDTO newBusiness ) {
		Long id =  importCtrl.addBusiness(newBusiness);
		
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
