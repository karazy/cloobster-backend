package net.eatsense.representation;

public class HistoryStatusDTO {
	private int visitCount;
	private String installId;
	
	public int getVisitCount() {
		return visitCount;
	}
	public void setVisitCount(int visitCount) {
		this.visitCount = visitCount;
	}
	public String getInstallId() {
		return installId;
	}
	public void setInstallId(String installId) {
		this.installId = installId;
	}
}
