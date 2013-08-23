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

import com.google.inject.Inject;

import net.eatsense.auth.Role;
import net.eatsense.controller.MenuController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.representation.ImageDTO;
import net.eatsense.representation.MenuDTO;

public class MenusResource {

	private Business business;
	private MenuController menuCtrl;
	private Account account;
	
	@Inject
	public MenusResource(MenuController menuCtrl) {
		super();
		this.menuCtrl = menuCtrl;
	}


	public void setBusiness(Business business) {
		this.business = business;
	}
	

	public void setAccount(Account account) {
		this.account = account;
	}


	@GET
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public List<MenuDTO> getMenus() {
		return menuCtrl.getMenus(business);
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public MenuDTO createMenu( MenuDTO menuData) {
		return menuCtrl.createMenu(business, menuData);
	}

	@GET
	@Path("{id}")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public MenuDTO getMenu(@PathParam("id") long id) {
		return menuCtrl.getMenuDTOWithProducts(business, id);
	}
	
	@PUT
	@Path("{id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public MenuDTO updateMenu(@PathParam("id") long id, MenuDTO menuData) {
		return menuCtrl.updateMenu(menuCtrl.getMenu(business, id), menuData);
	}
	
	@DELETE
	@Path("{id}")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public void deleteMenu(@PathParam("id") long id) {
		menuCtrl.deleteMenu(business, id);
	}
	
	@DELETE
	@Path("{id}/image")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public void removeImage(@PathParam("id") long id) {
		if(!menuCtrl.removeMenuImage(menuCtrl.getMenu(business, id))) {
			throw new net.eatsense.exceptions.NotFoundException();
		}
	}
	
	@POST
	@Path("{id}/image")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public ImageDTO updateImage(@PathParam("id") long id, ImageDTO imageData) {
		return menuCtrl.updateMenuImage(account, menuCtrl.getMenu(business, id), imageData);
	}
}
