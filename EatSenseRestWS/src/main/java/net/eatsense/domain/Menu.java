package net.eatsense.domain;

import javax.persistence.Transient;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;

public class Menu extends GenericEntity{
	
	private String title;
	
	private String description;
	
	@Parent
	private Key<Business> business;
	
	
	
	public String getTitle() {
		return title;
	}



	public void setTitle(String title) {
		this.title = title;
	}



	public String getDescription() {
		return description;
	}



	public void setDescription(String description) {
		this.description = description;
	}



	public Key<Business> getBusiness() {
		return business;
	}



	public void setBusiness(Key<Business> business) {
		this.business = business;
	}



	@Transient
	public Key<Menu> getKey() {
		return new Key<Menu>(getBusiness(), Menu.class, super.getId());
	}
	
}
