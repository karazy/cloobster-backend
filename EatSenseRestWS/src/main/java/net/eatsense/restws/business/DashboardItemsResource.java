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

import net.eatsense.auth.Role;
import net.eatsense.controller.DashboardController;
import net.eatsense.domain.Business;
import net.eatsense.domain.DashboardItem;
import net.eatsense.representation.DashboardConfigDTO;
import net.eatsense.representation.DashboardItemDTO;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

@Produces("application/json; charset=UTF-8")
public class DashboardItemsResource {
	
	private final DashboardController ctrl;
	private Business location;

	@Inject
	public DashboardItemsResource(DashboardController ctrl) {
		this.ctrl = ctrl;
	}

	public void setLocation(Business location) {
		this.location = location;
	}
	
	/**
	 * @return all current dashboard items for this location
	 */
	@GET
	public List<DashboardItemDTO> get() {
		return Lists.transform(ctrl.getItems(location.getKey()), DashboardItemDTO.toDTO);
	}
	
	/**
	 * @param itemData field data for the new dashboard item
	 * @return the newly created dashboard item entity
	 */
	@POST
	@Consumes("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public DashboardItemDTO create( DashboardItemDTO itemData) {
		return new DashboardItemDTO(ctrl.createAndSave(location.getKey(), itemData));
	}
	
	/**
	 * @param configData containing itemIds array for ordering the dashboard
	 * @return updated config data
	 */
	@PUT
	@Consumes("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public DashboardConfigDTO updateOrder(DashboardConfigDTO configData) {
		return new DashboardConfigDTO(ctrl.getAndUpdateConfig(location.getKey(), configData));
	}
	
	/**
	 * @param id
	 * @return the dashboard item saved under this id if present
	 */
	@GET
	@Path("{itemId}")
	public DashboardItemDTO get( @PathParam("itemId") long id) {
		return new DashboardItemDTO(ctrl.get(location.getKey(), id));
	}
	
	@PUT
	@Path("{itemId}")
	@Consumes("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public DashboardItemDTO update(@PathParam("itemId") long id, DashboardItemDTO itemData) {
		return new DashboardItemDTO(ctrl.getAndUpdate(location.getKey(), id, itemData));
	}

	@DELETE
	@Path("{itemId}")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public void delete(@PathParam("itemId") long id) {
		ctrl.delete(location.getKey(), id);
	}
}
