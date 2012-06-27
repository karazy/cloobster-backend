package net.eatsense.restws.business;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.google.inject.Inject;

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
	public List<ChoiceDTO> getChoices(@QueryParam("productId") long productId) {
		
		return menuCtrl.getChoicesForProduct(business.getKey(), productId);
	}
	
	@POST
	public ChoiceDTO createChoice(ChoiceDTO choiceData) {
		return menuCtrl.createChoice(business, choiceData);
	}
	
	@GET
	@Path("{id}")
	public ChoiceDTO getChoice(@PathParam("id") long id) {
		return new ChoiceDTO(menuCtrl.getChoice(business.getKey(), id));
	}
	
	@PUT
	@Path("{id}")
	public ChoiceDTO updateChoice(@PathParam("id") long id, ChoiceDTO choiceData) {
		return menuCtrl.updateChoice(menuCtrl.getChoice(business.getKey(), id), choiceData);
	}
}
