package net.eatsense.domain;

import java.util.List;

import com.google.common.base.Objects;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

@Cached
public class Area extends GenericEntity<Area> {
	
	private String name;
	private String description;
	
	@Parent
	private Key<Business> business;
	
	private List<Key<Menu>> menus;
	
	private boolean active = true;
	
	@Unindexed
	private boolean barcodeRequired;
	
	private boolean welcome;

	public List<Key<Menu>> getMenus() {
		return menus;
	}

	public void setMenus(List<Key<Menu>> menus) {
		if(!Objects.equal(this.menus, menus)) {
			this.setDirty(true);
			this.menus = menus;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(!Objects.equal(this.name, name)) {
			this.setDirty(true);
			this.name = name;
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		if(!Objects.equal(this.description, description)) {
			this.setDirty(true);
			this.description = description;
		}
	}

	public Key<Business> getBusiness() {
		return business;
	}

	public void setBusiness(Key<Business> business) {
		if(!Objects.equal(this.business, business)) {
			this.setDirty(true);
			this.business = business;
		}
	}

	@Override
	public Key<Area> getKey() {
		return new Key<Area>(business, Area.class, getId());
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		if(!Objects.equal(this.active, active)) {
			this.setDirty(true);
			this.active = active;
		}
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
		if(!Objects.equal(this.barcodeRequired, barcodeRequired)) {
			this.setDirty(true);
			this.barcodeRequired = barcodeRequired;
		}
	}
}
