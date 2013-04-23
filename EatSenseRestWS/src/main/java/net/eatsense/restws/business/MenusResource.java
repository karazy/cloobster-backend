package net.eatsense.restws.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.inject.Inject;

import net.eatsense.auth.Role;
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
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public List<MenuDTO> getMenus(@QueryParam("lang") Locale locale) {
		return menuCtrl.getMenus(business, Optional.fromNullable(locale));
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
	public MenuDTO getMenu(@PathParam("id") long id, @QueryParam("lang") String locale) {
		if(Strings.isNullOrEmpty(locale))
			return menuCtrl.getMenuDTOWithProducts(business, id);
		
		List<Locale> locales = new ArrayList<Locale>();
		String[] split = locale.split(",");
		for(String localeString : split){
			locales.add(new Locale(localeString));
		}
		
		return menuCtrl.getWithTranslations(business.getKey(), id, locales);
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
}
