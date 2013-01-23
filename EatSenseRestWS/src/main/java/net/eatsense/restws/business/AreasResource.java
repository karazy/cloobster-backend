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
import net.eatsense.controller.LocationController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.representation.AreaDTO;

@Produces("application/json; charset=UTF-8")
public class AreasResource {
	private Business business;
	
	public void setBusiness(Business business) {
		this.business = business;
	}

	private final LocationController businessCtrl;
	private Account account;
	
	@Inject
	public AreasResource(LocationController businessCtrl) {
		super();
		this.businessCtrl = businessCtrl;
	}
	
	/**
	 * @return All areas for this business.
	 */
	@GET
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public List<AreaDTO> getAreas() {
		return businessCtrl.getAreas(business.getKey(), false);
	}
	
	/**
	 * @param areaId
	 * @return Area with this id.
	 */
	@GET
	@Path("{areaId}")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public AreaDTO getArea(@PathParam("areaId") long areaId) {
		return new AreaDTO(businessCtrl.getArea(business.getKey(), areaId));
	}
	
	/**
	 * @param areaData
	 * @return A new Area object.
	 */
	@POST
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	@Consumes("application/json; charset=UTF-8")
	public AreaDTO createArea(AreaDTO areaData) {
		return new AreaDTO(businessCtrl.createArea(business.getKey(), areaData));
	}
	
	/**
	 * @param areaId
	 * @param areaData Area object with updated data.
	 * @return Updated Area object.
	 */
	@PUT
	@Path("{areaId}")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	@Consumes("application/json; charset=UTF-8")
	public AreaDTO updateArea(@PathParam("areaId") long areaId, AreaDTO areaData) {
		return new AreaDTO(businessCtrl.updateArea(businessCtrl.getArea(business.getKey(), areaId), areaData));
	}
	
	@DELETE
	@Path("{areaId}")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public void deleteArea(@PathParam("areaId") long areaId) {
		businessCtrl.deleteArea(businessCtrl.getArea(business.getKey(), areaId), account);
	}

	public void setAccount(Account account) {
		this.account = account;
	}
}
