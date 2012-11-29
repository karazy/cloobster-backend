package net.eatsense.controller;

import javax.validation.constraints.Min;

public class SpotsData {
	String name;
	
	@Min(0)
	int startNumber;
	
	@Min(1)
	int count;
	
	@Min(0)
	long areaId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStartNumber() {
		return startNumber;
	}

	public void setStartNumber(int startNumber) {
		this.startNumber = startNumber;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public long getAreaId() {
		return areaId;
	}

	public void setAreaId(long areaId) {
		this.areaId = areaId;
	}
}
