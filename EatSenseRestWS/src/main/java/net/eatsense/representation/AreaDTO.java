package net.eatsense.representation;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.bval.constraints.NotEmpty;

import com.googlecode.objectify.Key;

import net.eatsense.domain.Area;
import net.eatsense.domain.Menu;

public class AreaDTO {
	private Long id;
	
	@NotNull
	@NotEmpty
	private String name;
	
	private String description;
	private List<Long> menuIds;
	private Long businessId;
	private boolean welcome;
	private boolean active;
	private boolean barcodeRequired = true;
	
	public AreaDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public AreaDTO(Area area) {
		super();
		id = area.getId();
		name = area.getName();
		description = area.getDescription();
		active = area.isActive();
		barcodeRequired = area.isBarcodeRequired();
		
		if(area.getBusiness() != null) {
			businessId = area.getBusiness().getId();
		}
		menuIds = new ArrayList<Long>();
		if(area.getMenus() != null && !area.getMenus().isEmpty()) {
			for (Key<Menu> menuKey : area.getMenus()) {
				menuIds.add(menuKey.getId());
			}
		}
		welcome = area.isWelcome();		
	}
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public void setDescription(String descpription) {
		this.description = descpription;
	}
	public List<Long> getMenuIds() {
		return menuIds;
	}
	public void setMenuIds(List<Long> menuIds) {
		this.menuIds = menuIds;
	}
	public Long getBusinessId() {
		return businessId;
	}
	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isWelcome() {
		return welcome;
	}

	public void setWelcome(boolean welcome) {
		this.welcome = welcome;
	}

	public boolean isBarcodeRequired() {
		return barcodeRequired;
	}

	public void setBarcodeRequired(boolean barcodeRequired) {
		this.barcodeRequired = barcodeRequired;
	}
}
