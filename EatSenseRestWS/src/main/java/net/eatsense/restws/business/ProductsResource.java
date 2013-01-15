package net.eatsense.restws.business;

import java.util.List;

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

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;

import net.eatsense.auth.Role;
import net.eatsense.controller.MenuController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.representation.ImageDTO;
import net.eatsense.representation.ProductDTO;

/**
 * Contains method to create update and query for Products with a business account.
 * 
 * @author Nils Weiher
 *
 */
public class ProductsResource {

	private final MenuController menuCtrl;

	private Business business;

	private Account account;

	@Inject
	public ProductsResource(MenuController menuCtrl) {
		super();
		this.menuCtrl = menuCtrl;
	}

	public void setBusiness(Business business) {
		this.business = business;
		
	}

	public void setAccount(Account account) {
		this.account = account;	
	}

	
	/**
	 * Get Products, filtered by the menuId if different from zero.
	 * 
	 * @param menuId If different from 0, return only Products for the menu with this id.
	 * @param choiceId If menuId is not set and different from 0,
	 * 		return only Products with a choice with this Id.
	 * @param noMenu If true, return Products with no menu associated.
	 * @return List of Product objects.
	 */
	@GET
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public List<ProductDTO> getProducts(@QueryParam("menuId") long menuId, @QueryParam("choiceId") long choiceId,@QueryParam("noMenu") boolean noMenu) {
		if(noMenu) {
			return menuCtrl.getProductsWithNoMenu(business);
		}
		if(menuId != 0) {
			return menuCtrl.getProductsForMenu(business, menuId);
		}
		if(choiceId != 0) {
			return menuCtrl.getProductsForChoice(business, choiceId);
		}
		
		return menuCtrl.getProducts(business);
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
	
	@DELETE
	@Path("{id}")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public void markProductForDeletion(@PathParam("id") long id) {
		menuCtrl.trashProduct(menuCtrl.getProduct(business.getKey(), id), account);
	}
	
	@DELETE
	@Path("{id}/image")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public void removeImage(@PathParam("id") long id) {
		if(!menuCtrl.removeProductImage(menuCtrl.getProduct(business.getKey(), id))) {
			throw new net.eatsense.exceptions.NotFoundException();
		}
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@Path("{id}/image")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public ImageDTO updateImage(@PathParam("id") long id, ImageDTO imageData) {
		return menuCtrl.updateProductImage(account, menuCtrl.getProduct(business.getKey(), id), imageData);
	}
}
