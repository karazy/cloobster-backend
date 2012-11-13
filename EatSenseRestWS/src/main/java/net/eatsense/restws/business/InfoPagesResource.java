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

import com.google.appengine.api.images.ImagesServicePb.ImageData;
import com.google.inject.Inject;

import net.eatsense.auth.Role;
import net.eatsense.controller.InfoPageController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.representation.ImageDTO;
import net.eatsense.representation.InfoPageDTO;

@Produces("application/json; charset=UTF-8")
public class InfoPagesResource {
	private final InfoPageController infoPageCtrl;
	private Business business;
	private Account account;
	
	@Inject
	public InfoPagesResource(InfoPageController infoPageCtrl) {
		super();
		this.infoPageCtrl = infoPageCtrl;
	}
	
	@GET
	public List<InfoPageDTO> getInfoPages() {
		return infoPageCtrl.getAll(business.getKey());
	}
	
	@POST
	@Consumes("application/json; charset=UTF-8")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public InfoPageDTO createInfoPage(InfoPageDTO infoPageData) {
		return infoPageCtrl.create(business.getKey(), infoPageData);
	}
	
	@PUT
	@Path("{id}")
	@Consumes("application/json; charset=UTF-8")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public InfoPageDTO updateInfoPage(@PathParam("id") Long id, InfoPageDTO infoPageData) {
		return infoPageCtrl.update(infoPageCtrl.get(business.getKey(), id), infoPageData);
	}
	
	@POST
	@Path("{id}/image")
	@Consumes("application/json; charset=UTF-8")
	public ImageDTO saveImage(@PathParam("id") Long id, ImageDTO imageData) {
		return infoPageCtrl.updateImage(account, infoPageCtrl.get(business.getKey(), id), imageData);
	}	

	public void setBusiness(Business business) {
		this.business = business;
	}

	public void setAccount(Account account) {
		this.account = account;
	}
}
