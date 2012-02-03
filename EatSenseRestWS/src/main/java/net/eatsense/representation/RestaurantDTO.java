package net.eatsense.representation;

import java.util.Collection;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.bval.constraints.NotEmpty;

/**
 * POJO for data transfer, which represents a location where you can check in and order food/drinks what ever.
 * 
 * @author Nils Weiher
 *
 */
public class RestaurantDTO {

	/**
	 * Name of location.
	 */
	@NotNull
	@NotEmpty
	@Size(min=1)
	private String name;

	/**
	 * Description of location.
	 */
	private String description;

	/**
	 * All menus the restaurant is offering.
	 */
	@NotNull
	@NotEmpty
	@Valid
	private Collection<MenuDTO> menus;
	
	/**
	 * All different spots (e.g. tables, seats, areas) where a customer is able to checkin. 
	 */
	@NotNull
	@NotEmpty
	@Valid
	private Collection<SpotDTO> spots;

	public RestaurantDTO() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public Collection<MenuDTO> getMenus() {
		return menus;
	}

	public void setMenus(Collection<MenuDTO> menus) {
		this.menus = menus;
	}


	public Collection<SpotDTO> getSpots() {
		return spots;
	}

	public void setSpots(Collection<SpotDTO> spots) {
		this.spots = spots;
	}
}
