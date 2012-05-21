package net.eatsense.restws.customer;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import net.eatsense.controller.BillController;
import net.eatsense.controller.MenuController;
import net.eatsense.controller.OrderController;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.representation.BillDTO;
import net.eatsense.representation.MenuDTO;
import net.eatsense.representation.OrderDTO;
import net.eatsense.representation.ProductDTO;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.ResourceContext;

public class BusinessResource {
	@Context
	private ResourceContext resourceContext;

	private CheckIn checkIn;
	
	private Business business;
	
	private MenuController menuCtrl;
	private OrderController orderCtrl;
	private BillController billCtrl;
	
	@Inject
	public BusinessResource(MenuController menuCtrl, OrderController orderCtrl,
			BillController billCtrl) {
		super();
		this.menuCtrl = menuCtrl;
		this.orderCtrl = orderCtrl;
		this.billCtrl = billCtrl;
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
	public Collection<MenuDTO> getMenus() {
		return menuCtrl.getMenus(business);
	}
	

	@GET
	@Path("products")
	@Produces("application/json; charset=UTF-8")
	public Collection<ProductDTO> getAll() {
		return menuCtrl.getAllProducts(business);
	}
	
	@GET
	@Path("products/{productId}")
	@Produces("application/json; charset=UTF-8")
	public ProductDTO getProduct( @PathParam("productId") Long productId) {
		ProductDTO product = menuCtrl.getProduct(business, productId);
		
		if(product == null)
			throw new NotFoundException(productId + " id not found.");
		else return product;
	}
	
	@POST
	@Path("orders")
	@Produces("text/plain; charset=UTF-8")
	@Consumes("application/json; charset=UTF-8")
	@RolesAllowed({"guest"})
	public String placeOrder(OrderDTO order) {
		Long orderId = null;
		orderId = orderCtrl.placeOrderInCart(business, checkIn, order);	
		return orderId.toString();
	}
	
	
	@GET
	@Path("orders")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({"guest"})
	public Collection<OrderDTO> getOrders( @QueryParam("status") String status) {
		Collection<OrderDTO> orders = orderCtrl.getOrdersAsDto(business, checkIn, status);
		return orders;
	}
	
	@Path("orders/{orderId}")
	@RolesAllowed({"guest"})
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
	@RolesAllowed({"guest"})
	public BillDTO createBill(BillDTO bill) {
		return billCtrl.createBill(business, checkIn, bill);
	}

}
