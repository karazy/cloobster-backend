package net.eatsense.restws;

import java.util.Collection;

public interface IEatSenseResource<T> {
	
	public Collection<T> listAll();

}
