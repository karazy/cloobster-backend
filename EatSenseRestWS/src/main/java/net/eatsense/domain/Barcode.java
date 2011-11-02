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
	
	private String code;
	
	private byte[] barcode;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}	

	public Key<Area> getArea() {
		return area;
	}

	public void setArea(Key<Area> area) {
		this.area = area;
	}

	public byte[] getBarcode() {
		return barcode;
	}

	public void setBarcode(byte[] barcode) {
		this.barcode = barcode;
	}
	
	@Transient
	public Key<Barcode> getKey() {
	   return new Key<Barcode>(Barcode.class, id);
	}

}
