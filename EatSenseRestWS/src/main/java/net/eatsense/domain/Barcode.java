package net.eatsense.domain;

import javax.persistence.Transient;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;


public class Barcode extends GenericEntity{
	
	@Parent
	private Key<Area> area;
	
	private String barcode;
	
	/**
	 * A human readable identifier for the spot where the barcode is located.
	 * E.g. Table 4, Lounge etc.
	 */
	private String spot;
	
	private byte[] barcodeData;


	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String code) {
		this.barcode = code;
	}	

	public Key<Area> getArea() {
		return area;
	}

	public void setArea(Key<Area> area) {
		this.area = area;
	}

	public byte[] getBarcodeData() {
		return barcodeData;
	}

	public void setBarcodeData(byte[] barcodeData) {
		this.barcodeData = barcodeData;
	}
	
	@Transient
	public Key<Barcode> getKey() {
	   return new Key<Barcode>(Barcode.class, getId());
	}

	public String getSpot() {
		return spot;
	}

	public void setSpot(String spot) {
		this.spot = spot;
	}
	
	

}
