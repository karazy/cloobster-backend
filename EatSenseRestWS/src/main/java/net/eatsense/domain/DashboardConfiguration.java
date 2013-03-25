package net.eatsense.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Unindexed;

@Unindexed
public class DashboardConfiguration {
	@Id
	private String name = "dashboard";
	
	private Key<Business> location;
	
	private List<Key<DashboardItem>> items = new ArrayList<Key<DashboardItem>>();

	private Key<DashboardConfiguration> key; 

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Key<DashboardItem>> getItems() {
		return items;
	}

	public void setItems(List<Key<DashboardItem>> items) {
		this.items = items;
	}

	public Key<Business> getLocation() {
		return location;
	}

	public void setLocation(Key<Business> location) {
		this.location = location;
	}
	
	public Key<DashboardConfiguration> getKey() {
		if(this.key == null)
			this.key = Key.create(location, DashboardConfiguration.class, name);
		
		return this.key;
	}
}
