package net.eatsense.domain;

import javax.persistence.Id;
import javax.persistence.Transient;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;


public class Barcode {
	
	@Id
	private Long id;
	
	@Parent
	private Key<Area> area;
	
	private String barcode;
	
	private byte[] barcodeData;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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
	   return new Key<Barcode>(Barcode.class, id);
	}

}
