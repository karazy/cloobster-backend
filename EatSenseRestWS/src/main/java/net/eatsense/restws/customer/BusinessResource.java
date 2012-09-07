package net.eatsense.restws.customer;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.lucene.index.CheckIndex;

import net.eatsense.auth.Role;
import net.eatsense.controller.BillController;
import net.eatsense.controller.FeedbackController;
import net.eatsense.controller.MenuController;
import net.eatsense.controller.OrderController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.representation.BillDTO;
import net.eatsense.representation.FeedbackDTO;
import net.eatsense.representation.FeedbackFormDTO;
import net.eatsense.representation.MenuDTO;
import net.eatsense.representation.OrderDTO;
import net.eatsense.representation.ProductDTO;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.ResourceContext;

public class BusinessResource {
	@Context
	private ResourceContext resourceContext;

	private CheckIn checkIn;
	private Account account;
	
	private Business business;
	
	private MenuController menuCtrl;
	private OrderController orderCtrl;
	private BillController billCtrl;

	private FeedbackController feedbackCtrl;
	
	@Inject
	public BusinessResource(MenuController menuCtrl, OrderController orderCtrl,
			BillController billCtrl, FeedbackController feedbackCtrl) {
		super();
		this.feedbackCtrl = feedbackCtrl;
		this.menuCtrl = menuCtrl;
		this.orderCtrl = orderCtrl;
		this.billCtrl = billCtrl;
	}
	
	public void setAccount(Account account) {
		this.account = account;
	}	

	public void setCheckIn(CheckIn checkIn) {
		this.checkIn = checkIn;
	}

	public void setBusiness(Business business) {
		this.business = business;
	}
	
	@GET
	@Path("menus")
	@Produces("application/json; charset=UTF-8")
	public Collection<MenuDTO> getMenus(@QueryParam("areaId")long areaId) {
		return menuCtrl.getMenusWithProducts(business.getKey(), areaId);
	}
	

	@GET
	@Path("products")
	@Produces("application/json; charset=UTF-8")
	public Collection<ProductDTO> getAll() {
		return menuCtrl.getProductsWithChoices(business);
	}
	
	@GET
	@Path("products/{productId}")
	@Produces("application/json; charset=UTF-8")
	public ProductDTO getProduct( @PathParam("productId") Long productId) {
		return menuCtrl.getProductWithChoices(business.getKey(), productId);
	}
	
	/**
	 * Save a new Order for this customer, status will be cart.
	 * 
	 * @param order Data object to use for the new Order.
	 * @return The id of the created Order entity.
	 */
	@POST
	@Path("orders")
	@Produces("text/plain; charset=UTF-8")
	@Consumes("application/json; charset=UTF-8")
	@RolesAllowed(Role.GUEST)
	public String placeOrder(OrderDTO order) {
		Long orderId = null;
		orderId = orderCtrl.placeOrderInCart(business, checkIn, order);	
		return orderId.toString();
	}
	
	
	/**
	 * Get orders for the checkedIn customer, optionally filtered by status.
	 * 
	 * @param status Order status to filter the list by.
	 * @return List of Order transfer objects.
	 */
	@GET
	@Path("orders")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.GUEST, Role.USER})
	public Collection<OrderDTO> getOrders( @QueryParam("status") String status, @QueryParam("checkInId") Long checkInId) {
		Key<CheckIn> checkInKey;
		if(checkInId != null && checkInId.longValue() != 0) {
			checkInKey = new Key<CheckIn>(CheckIn.class, checkInId);
		}
		else {
			checkInKey = checkIn.getKey();
		}
		 
		Collection<OrderDTO> orders = orderCtrl.getOrdersAsDto(business, checkInKey, status);
		return orders;
	}
	
	@Path("orders/{orderId}")
	public OrderResource getOrderResource(@PathParam("orderId") Long orderId) {
		Order order = orderCtrl.getOrder(business, orderId);
		if( order == null)
			throw new NotFoundException();
		
		boolean authorized = checkIn == null ? false: order.getCheckIn().getId() == checkIn.getId().longValue();
		OrderResource orderResource = resourceContext.getResource(OrderResource.class);
		orderResource.setBusiness(business);
		orderResource.setCheckIn(checkIn);
		orderResource.setOrder(order);
		orderResource.setAuthorized(authorized);
		
		return orderResource;
	}
	
	@POST
	@Path("bills")
	@Produces("application/json; charset=UTF-8")
	@Consumes("application/json; charset=UTF-8")
	@RolesAllowed(Role.GUEST)
	public BillDTO createBill(BillDTO bill) {
		return billCtrl.createBill(business, checkIn, bill);
	}
	
	@GET
	@Path("feedbackforms")
	@Produces("application/json; charset=UTF-8")
	public FeedbackFormDTO getFeedbackForm() {
		return feedbackCtrl.getFeedbackFormForBusiness(business);
	}
	
	@POST
	@Path("feedback")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed(Role.GUEST)
	public FeedbackDTO postFeedback(FeedbackDTO feedbackData) {
		return new FeedbackDTO(feedbackCtrl.addFeedback(business, checkIn, feedbackData));
	}
	
	@GET
	@Path("feedback/{id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed(Role.GUEST)
	public FeedbackDTO getPreviousFeedback(@PathParam("id") long id) {
		return feedbackCtrl.getFeedbackForCheckIn(checkIn, id);
	}
	
	@PUT
	@Path("feedback/{id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed(Role.GUEST)
	public FeedbackDTO updatePreviousFeedback(@PathParam("id") long id,FeedbackDTO feedbackData) {
		if(checkIn.getFeedback() != null && checkIn.getFeedback().getId() == id) {
			return new FeedbackDTO(feedbackCtrl.updateFeedback(checkIn, feedbackData));
		}
		else {
			throw new net.eatsense.exceptions.NotFoundException("unknown feedback id");
		}
	}

}
