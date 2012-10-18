package net.eatsense.representation;

public class ImageCropDTO {
	private String blobKey;
	private double leftX;
	private double topY;
	private double rightX;
	private double bottomY;
	
	public double getLeftX() {
		return leftX;
	}
	public void setLeftX(double leftX) {
		this.leftX = leftX;
	}
	public double getTopY() {
		return topY;
	}
	public void setTopY(double topY) {
		this.topY = topY;
	}
	public double getRightX() {
		return rightX;
	}
	public void setRightX(double rightX) {
		this.rightX = rightX;
	}
	public double getBottomY() {
		return bottomY;
	}
	public void setBottomY(double bottomY) {
		this.bottomY = bottomY;
	}
	public String getBlobKey() {
		return blobKey;
	}
	public void setBlobKey(String blobKey) {
		this.blobKey = blobKey;
	}
}
