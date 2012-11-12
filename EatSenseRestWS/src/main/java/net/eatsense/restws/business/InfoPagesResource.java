package net.eatsense.restws.business;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;

import com.google.inject.Inject;

import net.eatsense.controller.InfoPageController;
import net.eatsense.domain.Business;
import net.eatsense.representation.InfoPageDTO;

@Produces("application/json; charset=UTF-8")
public class InfoPagesResource {
	private final InfoPageController infoPageCtrl;
	private Business business;
	
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
	public InfoPageDTO createInfoPage(InfoPageDTO infoPageData) {
		return infoPageCtrl.create(business.getKey(), infoPageData);
	}

	public void setBusiness(Business business) {
		this.business = business;
	}
}
