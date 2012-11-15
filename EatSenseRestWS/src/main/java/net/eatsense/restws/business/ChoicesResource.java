package net.eatsense.restws.business;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
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
import net.eatsense.representation.ChoiceDTO;

public class ChoicesResource {
	
	private Business business;
	private final MenuController menuCtrl;
	
	@Inject
	public ChoicesResource(MenuController menuCtrl) {
		super();
		this.menuCtrl = menuCtrl;
	}

	public void setBusiness(Business business) {
		this.business = business;
	}
	
	@GET
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public List<ChoiceDTO> getChoices(@QueryParam("productId") long productId) {
		return menuCtrl.getChoices(business.getKey(), productId);
	}
	
	@POST
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public ChoiceDTO createChoice(ChoiceDTO choiceData) {
		return menuCtrl.createChoice(business.getKey(), choiceData);
	}
	
	@GET
	@Path("{id}")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public ChoiceDTO getChoice(@PathParam("id") long id) {
		return new ChoiceDTO(menuCtrl.getChoice(business.getKey(), id));
	}
	
	@PUT
	@Path("{id}")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public ChoiceDTO updateChoice(@PathParam("id") long id, ChoiceDTO choiceData) {
		return menuCtrl.updateChoice(menuCtrl.getChoice(business.getKey(), id), choiceData);
	}
	
	/**
	 * Delete a Choice from a Product, and eventually from the data store.
	 * 
	 * @param id Id of the Choice entity.
	 * @param productId Id of the Product entity, from which the choice should be removed.
	 */
	@DELETE
	@Path("{id}")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public void deleteChoice(@PathParam("id") long id, @QueryParam("productId") long productId) {
		menuCtrl.deleteChoice(business.getKey(), id, productId);
	}
}
