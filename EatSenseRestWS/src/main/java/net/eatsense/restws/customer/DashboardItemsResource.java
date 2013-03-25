package net.eatsense.restws.customer;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

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
}
