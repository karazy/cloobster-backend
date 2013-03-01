package net.eatsense.representation;

import java.util.List;

public class ProductsData {
	private Boolean active;
	private Boolean hideInDashboard;
	private Boolean special;
	
	private List<Long> ids;

	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getHideInDashboard() {
		return hideInDashboard;
	}

	public void setHideInDashboard(Boolean hideInDashboard) {
		this.hideInDashboard = hideInDashboard;
	}

	public Boolean getSpecial() {
		return special;
	}

	public void setSpecial(Boolean special) {
		this.special = special;
	}
}
