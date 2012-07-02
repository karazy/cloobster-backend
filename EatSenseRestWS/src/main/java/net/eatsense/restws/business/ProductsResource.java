package net.eatsense.restws.business;

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

import com.google.inject.Inject;

import net.eatsense.auth.Role;
import net.eatsense.controller.MenuController;
import net.eatsense.domain.Business;
import net.eatsense.representation.ProductDTO;

public class ProductsResource {

	private final MenuController menuCtrl;

	private Business business;

	@Inject
	public ProductsResource(MenuController menuCtrl) {
		super();
		this.menuCtrl = menuCtrl;
	}

	public void setBusiness(Business business) {
		this.business = business;
		
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public List<ProductDTO> getProductsForMenu(@QueryParam("menuId") long id) {
		return menuCtrl.getProductsForMenu(business, id);
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public ProductDTO createProduct(ProductDTO productData) {
		return menuCtrl.createProduct(business, productData);
	}
	
	@GET
	@Path("{id}")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public ProductDTO getProduct(@PathParam("id") long id) {
		return new ProductDTO(menuCtrl.getProduct(business.getKey(), id));
	}
	
	@PUT
	@Path("{id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public ProductDTO updateProduct(@PathParam("id") long id, ProductDTO productData) {
		return menuCtrl.updateProduct(menuCtrl.getProduct(business.getKey(), id), productData);
	}
}
