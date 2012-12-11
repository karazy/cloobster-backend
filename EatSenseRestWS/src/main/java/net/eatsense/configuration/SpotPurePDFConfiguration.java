package net.eatsense.configuration;

import javax.validation.constraints.Min;

public class SpotPurePDFConfiguration {
	@Min(0)
	private double textPositionX;
	@Min(0)
	private double textPositionY;
	@Min(0)
	private double barcodePositionX;
	@Min(0)
	private double barcodePositionY;
	
	public double getTextPositionX() {
		return textPositionX;
	}
	public void setTextPositionX(double textPositionX) {
		this.textPositionX = textPositionX;
	}
	public double getTextPositionY() {
		return textPositionY;
	}
	public void setTextPositionY(double textPositionY) {
		this.textPositionY = textPositionY;
	}
	public double getBarcodePositionX() {
		return barcodePositionX;
	}
	public void setBarcodePositionX(double barcodePositionX) {
		this.barcodePositionX = barcodePositionX;
	}
	public double getBarcodePositionY() {
		return barcodePositionY;
	}
	public void setBarcodePositionY(double barcodePositionY) {
		this.barcodePositionY = barcodePositionY;
	}
}
