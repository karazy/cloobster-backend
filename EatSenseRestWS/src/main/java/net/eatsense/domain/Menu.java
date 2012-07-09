package net.eatsense.domain;

import javax.persistence.Transient;

import com.google.common.base.Objects;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;

public class Menu extends GenericEntity<Menu>{
	
	private String title;
	
	private String description;
	
	@Parent
	private Key<Business> business;
	
	private Integer order;
	
	private boolean active = false;
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		if(!Objects.equal(title, this.title)) {
			this.setDirty(true);
			this.title = title;
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

	@Transient
	public Key<Menu> getKey() {
		return new Key<Menu>(getBusiness(), Menu.class, super.getId());
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		if(!Objects.equal(this.order, order)) {
			this.setDirty(true);
			this.order = order;
		}
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
	
}
