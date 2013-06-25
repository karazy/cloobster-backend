package net.eatsense.dataimport.giata;

import com.googlecode.objectify.Key;

import net.eatsense.domain.GenericEntity;

public class GiataImportInfo extends GenericEntity<GiataImportInfo> {

	@Override
	public Key<GiataImportInfo> getKey() {
		return Key.create(GiataImportInfo.class, getId());
	}
}
