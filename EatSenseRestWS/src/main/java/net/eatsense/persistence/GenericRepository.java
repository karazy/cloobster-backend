package net.eatsense.persistence;

import java.util.Collection;
import java.util.List;

import net.eatsense.domain.Area;
import net.eatsense.domain.Barcode;
import net.eatsense.domain.Restaurant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public abstract class GenericRepository<T> {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	static {
		ObjectifyService.register(Restaurant.class);
		ObjectifyService.register(Area.class);
		ObjectifyService.register(Barcode.class);
	}

	protected ObjectifyService datastore;

	@Inject
	public GenericRepository(ObjectifyService datastore) {
		this.datastore = datastore;
	}

	public Key<T> saveOrUpdate(T obj) {
		logger.info("saveOrUpdate {} ", obj);
		Objectify oiy = ObjectifyService.begin();
		return oiy.put(obj);
	}

	public void delete(T obj) {
		logger.info("delete {} ", obj);
		Objectify oiy = ObjectifyService.begin();
		oiy.delete(obj);
	}

	public T findByKey(long id, Class<T> clazz) {
		logger.info("findByKey {} ", id);
		Objectify oiy = ObjectifyService.begin();
		Key<T> key = new Key<T>(clazz, id);
		return oiy.find(key);
	}

	public <V> T getByKey(Key<V> owner, Class<T> clazz, long id) {
		logger.info("getByKey {} ", id);
		Objectify oiy = ObjectifyService.begin();
		return oiy.get(new Key<T>(owner, clazz, id));
	}

	public <V> List<V> getChildren(Class<V> clazz, Key<T> parentKey) {
		logger.info("getChildren for {} ", parentKey);
		Objectify oiy = ObjectifyService.begin();
		return oiy.query(clazz).ancestor(parentKey).list();
	}

	public Collection<T> getAll(Class<T> clazz) {
		logger.info("getAll entities of type {} ", clazz);
		Objectify oiy = ObjectifyService.begin();
		Collection<T> list = oiy.query(clazz).list();
		return list;
	}

}
