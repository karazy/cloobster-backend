package net.eatsense.persistence;

import net.eatsense.counter.Counter;
import net.eatsense.domain.Channel;
import net.eatsense.domain.Subscription;

import com.google.inject.Inject;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

public class OfyService {
	private final ObjectifyKeyFactory keyFactory;

	public static void registerEntities() {
		ObjectifyService.register(Subscription.class);
		ObjectifyService.register(Channel.class);
		ObjectifyService.register(Counter.class);
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
