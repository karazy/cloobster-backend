package net.eatsense.domain;

import com.vercer.engine.persist.annotation.Key;

public class Barcode {
	
	@Key
	private Long id;
	
	private String code;
	
	private byte[] barcode;

}
