package net.eatsense.persistence;

import net.eatsense.domain.Barcode;

import com.google.inject.Inject;
import com.googlecode.objectify.ObjectifyService;

public class BarcodeRepository extends Repository<Barcode> {

	@Inject
	public BarcodeRepository(ObjectifyService datastore) {
		super(datastore);
	}

}
