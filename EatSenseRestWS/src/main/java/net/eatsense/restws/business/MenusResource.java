package net.eatsense.restws.business;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.google.inject.Inject;

import net.eatsense.controller.MenuController;
import net.eatsense.domain.Business;
import net.eatsense.representation.MenuDTO;

public class MenusResource {

	private Business business;
	private MenuController menuCtrl;
	
	@Inject
	public MenusResource(MenuController menuCtrl) {
		super();
		this.menuCtrl = menuCtrl;
	}


	public void setBusiness(Business business) {
		this.business = business;
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	public List<MenuDTO> getMenus() {
		return menuCtrl.getMenus(business);
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public MenuDTO createMenu( MenuDTO menuData) {
		return menuCtrl.createMenu(business, menuData);
	}

	@GET
	@Path("{id}")
	@Produces("application/json; charset=UTF-8")
	public MenuDTO getMenu(@PathParam("id") long id) {
		return new MenuDTO(menuCtrl.getMenu(business, id));
	}
	
	@PUT
	@Path("{id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public MenuDTO updateMenu(@PathParam("id") long id, MenuDTO menuData) {
		return menuCtrl.updateMenu(menuCtrl.getMenu(business, id), menuData);
	}	
}
