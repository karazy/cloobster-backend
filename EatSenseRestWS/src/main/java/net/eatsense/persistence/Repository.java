package net.eatsense.persistence;

import java.util.List;

import net.eatsense.domain.Area;
import net.eatsense.domain.Barcode;
import net.eatsense.domain.Restaurant;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * Generic repository. Acts as intermediate layer between datastore and domain
 * objects. Similar to a DAO.
 * 
 * @author freifschneider
 * 
 * @param <T>
 */
public abstract class Repository<T> {

	static {
		ObjectifyService.register(Restaurant.class);
		ObjectifyService.register(Area.class);
		ObjectifyService.register(Barcode.class);
	}

	protected ObjectifyService datastore;

	@Inject
	public Repository(ObjectifyService datastore) {
		this.datastore = datastore;
	}

	public Key<T> saveOrUpdate(T obj) {
		Objectify oiy = ObjectifyService.begin();
		return oiy.put(obj);
	}

//	public void update(T obj) {
//		Objectify oiy = ObjectifyService.begin();
//		oiy.
//		datastore.update(obj);
//	}

	public void delete(T obj) {
		Objectify oiy = ObjectifyService.begin();
		oiy.delete(obj);
	}

	public T findByKey(long id, Class<T> clazz) {
		Objectify oiy = ObjectifyService.begin();
		Key<T> key = new Key<T>(clazz, id);
		return oiy.find(key);
	}
	
	public <V> T getByKey(Key<V> owner, Class<T> clazz, long id ) {
		Objectify oiy = ObjectifyService.begin();
		return oiy.get(new Key<T>(owner, clazz, id));
	}
	
	
	public <V> List<V> getChildren(Class<V> clazz, Key<T> parentKey) {
		Objectify oiy = ObjectifyService.begin();
		return oiy.query(clazz).ancestor(parentKey).list();
	}

}
