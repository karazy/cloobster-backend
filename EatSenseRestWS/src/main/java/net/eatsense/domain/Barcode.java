package net.eatsense.domain;

import com.google.code.twig.annotation.Parent;



public class Barcode {
	
	private Long id;
	
	@Parent
	private Area area;
	
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

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	public byte[] getBarcode() {
		return barcode;
	}

	public void setBarcode(byte[] barcode) {
		this.barcode = barcode;
	}
	
	

}
