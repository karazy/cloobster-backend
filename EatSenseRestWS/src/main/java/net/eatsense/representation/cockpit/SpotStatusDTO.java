package net.eatsense.representation.cockpit;

import net.eatsense.domain.Spot;

public class SpotStatusDTO {
	/**
	 * A tag which can be used to group spots for easier organisation.
	 * E. g. Outside, Upper floor
	 */
	private String groupTag;
	private String name;
	private Long id;
	private String status;
	private Integer checkInCount;
	private Long areaId;
	
	public SpotStatusDTO() {
	}
	
	public SpotStatusDTO(Spot spot) {
		if(spot == null)
			return;
		
		this.name = spot.getName();
		this.id = spot.getId();
		if(spot.getArea() != null)
			this.areaId = spot.getArea().getId();
	}
	
	public String getGroupTag() {
		return groupTag;
	}
	public void setGroupTag(String groupTag) {
		this.groupTag = groupTag;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getCheckInCount() {
		return checkInCount;
	}
	public void setCheckInCount(Integer checkInCount) {
		this.checkInCount = checkInCount;
	}
	
	public Long getAreaId() {
		return areaId;
	}

	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}
	
	
}
