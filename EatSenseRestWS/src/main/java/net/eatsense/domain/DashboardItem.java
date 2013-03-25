package net.eatsense.domain;

import java.util.List;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;

/**
 * Representing one field of the dynamic location dashboard in the App.
 * 
 * @author Nils Weiher
 *
 */
public class DashboardItem extends GenericEntity<DashboardItem> {
	private String type;
	
	private List<Long> entityIds;
	
	@Parent
	private Key<Business> location;

	private Key<DashboardItem> key;
	
	@Override
	public Key<DashboardItem> getKey() {
		if(key == null) {
			key = Key.create(location, DashboardItem.class, this.getId());
		}
		
		return key;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Long> getEntityIds() {
		return entityIds;
	}

	public void setEntityIds(List<Long> entityIds) {
		this.entityIds = entityIds;
	}

	public Key<Business> getLocation() {
		return location;
	}

	public void setLocation(Key<Business> location) {
		this.location = location;
	}
}
