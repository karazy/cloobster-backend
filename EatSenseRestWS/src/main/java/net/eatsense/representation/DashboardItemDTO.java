package net.eatsense.representation;

import java.util.List;

import com.google.common.base.Function;

import net.eatsense.domain.DashboardItem;

public class DashboardItemDTO {
	private Long id;
	private String type;
	private List<Long> entityIds;
	
	public DashboardItemDTO() {
	}

	public DashboardItemDTO(DashboardItem item) {
		if(item == null)
			return;
		this.id = item.getId();
		this.type = item.getType();
		this.entityIds = item.getEntityIds();
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
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Transform function for collection operations.
	 */
	public final static Function<DashboardItem, DashboardItemDTO> toDTO = new Function<DashboardItem, DashboardItemDTO>() {
		@Override
		public DashboardItemDTO apply(DashboardItem input) {
			return new DashboardItemDTO(input);
		}
	};
}
