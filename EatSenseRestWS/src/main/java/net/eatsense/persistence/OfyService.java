package net.eatsense.persistence;

import net.eatsense.domain.Subscription;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

public class OfyService {
	static {
		ObjectifyService.register(Subscription.class);
	}
	
	public Objectify ofy() {
		return ObjectifyService.begin();
	}
	
	public Objectify ofyTrans() {
		return ObjectifyService.beginTransaction();
	}
	
	public ObjectifyFactory factory() {
		return ObjectifyService.factory();
	}
}
