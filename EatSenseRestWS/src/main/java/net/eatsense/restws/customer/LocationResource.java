package net.eatsense.restws.customer;

import java.util.Collection;
import java.util.List;

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

import net.eatsense.auth.Role;
import net.eatsense.controller.BillController;
import net.eatsense.controller.FeedbackController;
import net.eatsense.controller.InfoPageController;
import net.eatsense.controller.LocationController;
import net.eatsense.controller.MenuController;
import net.eatsense.controller.OrderController;
import net.eatsense.controller.SubscriptionController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.domain.Subscription;
import net.eatsense.representation.AreaDTO;
import net.eatsense.representation.BillDTO;
import net.eatsense.representation.FeedbackDTO;
import net.eatsense.representation.FeedbackFormDTO;
import net.eatsense.representation.LocationProfileDTO;
import net.eatsense.representation.MenuDTO;
import net.eatsense.representation.OrderDTO;
import net.eatsense.representation.ProductDTO;
import net.eatsense.representation.SpotDTO;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.ResourceContext;

public class LocationResource {
	@Context
	private ResourceContext resourceContext;

	private CheckIn checkIn;
	private Account account;
	
	private Business business;
	
	private MenuController menuCtrl;
	private OrderController orderCtrl;
	private BillController billCtrl;
	private SubscriptionController subCtrl;

	private FeedbackController feedbackCtrl;

	private LocationController locationCtrl;
	
	@Inject
	public LocationResource(MenuController menuCtrl, OrderController orderCtrl,
			BillController billCtrl, FeedbackController feedbackCtrl, InfoPageController infoPageCtrl, SubscriptionController subCtrl, LocationController locationCtrl) {
		super();
		this.subCtrl = subCtrl;
		this.feedbackCtrl = feedbackCtrl;
		this.menuCtrl = menuCtrl;
		this.orderCtrl = orderCtrl;
		this.billCtrl = billCtrl;
		this.locationCtrl = locationCtrl;
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
	@Produces("application/json; charset=UTF-8")
	public LocationProfileDTO getBusiness() {
		LocationProfileDTO businessDto = new LocationProfileDTO(business);
		Subscription activeSubscription = subCtrl.getActiveSubscription(business);
		businessDto.setBasic(activeSubscription != null ? activeSubscription.isBasic() : true);
		return businessDto;
	}
	
	@GET
	@Path("menus")
	@Produces("application/json; charset=UTF-8")
	public Collection<MenuDTO> getMenus(@QueryParam("areaId")long areaId, @QueryParam("includeProducts") Boolean includeProducts) {
		if(includeProducts != null && includeProducts) {
			return menuCtrl.getMenusWithProducts(business.getKey(), areaId);
		}
		else {
			return menuCtrl.getMenusForArea(business.getKey(), areaId);	
		}
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
		if( account != null && checkInId != null && checkInId.longValue() != 0) {
			return orderCtrl.getOrdersAsDtoForVisit(business, account, new Key<CheckIn>(CheckIn.class, checkInId), status);
		}
		else {
			return orderCtrl.getOrdersAsDto(business, checkIn.getKey(), status);
		}
		
		
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
		return feedbackCtrl.getActiveFeedbackFormForLocation(business);
	}
	
	@POST
	@Path("feedback")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed(Role.GUEST)
	public FeedbackDTO postFeedback(FeedbackDTO feedbackData) {
		return new FeedbackDTO(feedbackCtrl.createFeedback(business, checkIn, feedbackData));
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

	@Path("infopages")
	public InfoPagesResource getInfoPagesResource() {
		InfoPagesResource infoPageResource = resourceContext.getResource(InfoPagesResource.class);
		infoPageResource.setBusiness(business);
		
		return infoPageResource;
	}
	
	@GET
	@Path("areas")
	@Produces("application/json; charset=UTF-8")
	public List<AreaDTO> getAreas() {
		return locationCtrl.getAreas(business.getKey(),true, true);
	}
	
	/**
	 * Load Spot entities for this Business (and Area if specified).
	 * 
	 * @param areaId Specify the Area, for which to return the Spot entites.
	 * @return All Spot entities for the Area specified by the areaId.
	 */
	@GET
	@Path("spots")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed(Role.GUEST)
	public List<SpotDTO> getSpots(@QueryParam("areaId")long areaId) {
		return locationCtrl.getSpots(business.getKey(), areaId, false, true);
	}
	
	@Path("dashboarditems")
	public DashboardItemsResource getDashboardItemsResource() {
		DashboardItemsResource resource = resourceContext.getResource(DashboardItemsResource.class);
		resource.setLocation(business);
		
		return resource;
	}
}
