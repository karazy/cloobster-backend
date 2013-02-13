package net.eatsense.persistence;

import net.eatsense.counter.Counter;
import net.eatsense.domain.Channel;
import net.eatsense.domain.CustomerProfile;
import net.eatsense.domain.Subscription;

import com.google.inject.Inject;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

public class OfyService {
	private final ObjectifyKeyFactory keyFactory;
	
	/**
	 * Try to register entity with Objectify and silently fail if already registered.
	 * 
	 * @param clazz
	 */
	public static void register(Class<?> clazz) {
		try {
			ObjectifyService.register(clazz);
		} catch (IllegalArgumentException e) {
			// We already registered the entity, okay to skip this.
		}
	}

	public static void registerEntities() {
		register(Subscription.class);
		register(Channel.class);
		register(CustomerProfile.class);
		register(Counter.class);
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
