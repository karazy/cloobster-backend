package net.eatsense.domain;

import com.vercer.engine.persist.annotation.Key;

public class Barcode {
	
	@Key
	private Long id;
	
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

	public byte[] getBarcode() {
		return barcode;
	}

	public void setBarcode(byte[] barcode) {
		this.barcode = barcode;
	}
	
	

}
