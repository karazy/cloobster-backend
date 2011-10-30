package net.eatsense.persistence;

import com.google.appengine.api.datastore.Key;
import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;


/**
 * Generic repository. Acts as intermediate layer between datastore and domain objects.
 * Similar to a DAO.
 * 
 * @author freifschneider
 *
 * @param <T>
 */
public abstract class Repository<T> {
	
	protected ObjectDatastore datastore;
	
	@Inject
	public Repository(ObjectDatastore datastore) {
		this.datastore = datastore;
	}
	
	public Key save(T obj) {		
		return datastore.store().instance(obj).now();
	}
	
	public void update(T obj) {
		datastore.update(obj);
	}
	
	public void delete(T obj) {
		datastore.delete(obj);
	}
	
	public T findByKey(long key, Class<T> clazz) {
		return datastore.load(clazz, key);
	}
	
	
	
	

}
