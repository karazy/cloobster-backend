package net.eatsense.representation;

import java.util.List;

import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;

import net.eatsense.domain.DashboardConfiguration;
import net.eatsense.domain.DashboardItem;

public class DashboardConfigDTO {
	private String name;
	private List<Long> itemIds;
	
	public DashboardConfigDTO() {
	}
	
	public DashboardConfigDTO(DashboardConfiguration config) {
		if(config == null)
			return;
		itemIds = Lists.newArrayList();
		for (Key<DashboardItem> itemKey : config.getItems()) {
			itemIds.add(itemKey.getId());
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Long> getItemIds() {
		return itemIds;
	}

	public void setItemIds(List<Long> itemIds) {
		this.itemIds = itemIds;
	}
}
