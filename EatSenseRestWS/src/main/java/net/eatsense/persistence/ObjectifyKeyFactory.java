package net.eatsense.persistence;

import com.google.inject.Singleton;
import com.googlecode.objectify.Key;

@Singleton
public class ObjectifyKeyFactory {
	
	public <T> Key<T> create(Class<? extends T> kindClass, long id) {
		return Key.create(kindClass, id);
	}
	
	public <T> Key<T> create(Class<? extends T> kindClass, String name) {
		return Key.create(kindClass, name);
	}
	
	public <T> Key<T> create(Key<?> parent, Class<? extends T> kindClass, long id) {
		return Key.create(parent, kindClass, id);
	}
	
	public <T> Key<T> create(Key<?> parent, Class<? extends T> kindClass, String name) {
		return Key.create(parent, kindClass, name);
	}
	
}
