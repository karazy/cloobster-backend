package net.eatsense.configuration;

import javax.validation.constraints.Min;

import com.google.common.base.Objects;

public class SpotPurePDFConfiguration {
	@Min(0)
	private double textPositionX;
	@Min(0)
	private double textPositionY;
	@Min(0)
	private double barcodePositionX;
	@Min(0)
	private double barcodePositionY;
	@Min(0)
	private double pageWidth;
	@Min(0)
	private double pageHeight;
	@Min(72)
	private int qrImageDPI;
	
	@Min(2)
	private double fontSize;
	
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
	public double getPageWidth() {
		return pageWidth;
	}
	public void setPageWidth(double pageWidth) {
		this.pageWidth = pageWidth;
	}
	public double getPageHeight() {
		return pageHeight;
	}
	public void setPageHeight(double pageHeight) {
		this.pageHeight = pageHeight;
	}
	public int getQrImageDPI() {
		return qrImageDPI;
	}
	public void setQrImageDPI(int qrImageDPI) {
		this.qrImageDPI = qrImageDPI;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("textPositionX", textPositionX)
				.add("textPositionY", textPositionY)
				.add("barcodePositionX", barcodePositionX)
				.add("barcodePositionY", barcodePositionY)
				.add("pageWidth", pageWidth)
				.add("pageHeight", pageHeight)
				.add("qrImageDPI", qrImageDPI)
				.add("fontSize", fontSize).toString();
	}
	public double getFontSize() {
		return fontSize;
	}
	public void setFontSize(double fontSize) {
		this.fontSize = fontSize;
	}
}
