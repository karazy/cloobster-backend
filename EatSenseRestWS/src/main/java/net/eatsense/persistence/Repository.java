package net.eatsense.persistence;

import com.google.inject.Inject;
import com.vercer.engine.persist.ObjectDatastore;

/**
 * Generic repository. Acts as intermediate layer between datastore and domain objects.
 * Similar to a DAO.
 * 
 * @author freifschneider
 *
 * @param <T>
 */
public abstract class Repository<T> {
	
	private ObjectDatastore datastore;
	
	@Inject
	public Repository(ObjectDatastore datastore) {
		this.datastore = datastore;
	}
	
	public void save(T obj) {
		datastore.store(obj);
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
