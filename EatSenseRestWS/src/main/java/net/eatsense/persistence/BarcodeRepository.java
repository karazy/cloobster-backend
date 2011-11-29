package net.eatsense.persistence;

import net.eatsense.domain.Barcode;

public class BarcodeRepository extends GenericRepository<Barcode> {
	
	public BarcodeRepository() {
		super();
		super.clazz = Barcode.class;
	}

}
