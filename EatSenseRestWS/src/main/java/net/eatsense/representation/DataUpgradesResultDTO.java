package net.eatsense.representation;

public class DataUpgradesResultDTO {
	String status;
	int entityCount;

	
	public DataUpgradesResultDTO(String status, int entityCount) {
		super();
		this.status = status;
		this.entityCount = entityCount;
	}

	public DataUpgradesResultDTO() {
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getEntityCount() {
		return entityCount;
	}
	public void setEntityCount(int entityCount) {
		this.entityCount = entityCount;
	}
}
