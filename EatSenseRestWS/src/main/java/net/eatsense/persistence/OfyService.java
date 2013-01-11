package net.eatsense.persistence;

import net.eatsense.domain.Subscription;

import com.google.inject.Inject;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

public class OfyService {
	private final ObjectifyKeyFactory keyFactory;

	static {
		ObjectifyService.register(Subscription.class);
	}
	
	@Inject
	public OfyService(ObjectifyKeyFactory keyFactory) {
		this.keyFactory = keyFactory;
	}
	
	public Objectify ofy() {
		return ObjectifyService.begin();
	}
	
	public ObjectifyKeyFactory keys() {
		return keyFactory;
	}
	
	public Objectify ofyTrans() {
		return ObjectifyService.beginTransaction();
	}
	
	public ObjectifyFactory factory() {
		return ObjectifyService.factory();
	}
}
