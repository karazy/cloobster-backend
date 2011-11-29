package net.eatsense.persistence;

import java.util.Collection;
import java.util.List;

import net.eatsense.domain.Area;
import net.eatsense.domain.Barcode;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Restaurant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;
import com.googlecode.objectify.util.DAOBase;

/**
 * Generic repository. Acts as intermediate layer between datastore and domain
 * objects. Similar to a DAO.
 * 
 * @author freifschneider
 * 
 * @param <T>
 */
public class GenericRepository<T> extends DAOBase{

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	protected Class<T> clazz;

	static {
		ObjectifyService.register(Restaurant.class);
		ObjectifyService.register(Area.class);
		ObjectifyService.register(Barcode.class);
		ObjectifyService.register(CheckIn.class);
	}

	protected ObjectifyService datastore;

	@Inject
	public GenericRepository() {
//		 this.clazz = (Class<T>) ((ParameterizedType)
//				 getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		//this.datastore = datastore;
	}

	public Key<T> saveOrUpdate(T obj) {
		logger.info("saveOrUpdate {} ", obj);
//		Objectify ofy() = ObjectifyService.begin();
		return ofy().put(obj);
	}

	public void delete(T obj) {
		logger.info("delete {} ", obj);
//		Objectify ofy() = ObjectifyService.begin();
		ofy().delete(obj);
	}

	public T findByKey(long id) {
		logger.info("findByKey {} ", id);
//		Objectify ofy() = ObjectifyService.begin();
		Key<T> key = new Key<T>(clazz, id);
		return ofy().find(key);
	}

	public <V> T getByKey(Key<V> owner, long id) {
		logger.info("getByKey {} ", id);
//		Objectify ofy() = ObjectifyService.begin();
		return ofy().get(new Key<T>(owner, clazz, id));
	}

	public <V> List<V> getChildren(Class<V> clazz, Key<T> parentKey) {
		logger.info("getChildren for {} ", parentKey);
//		Objectify ofy() = ObjectifyService.begin();
		return ofy().query(clazz).ancestor(parentKey).list();
	}

	public Collection<T> getAll() {
		logger.info("getAll entities of type {} ", clazz);
//		Objectify ofy() = ObjectifyService.begin();
		Collection<T> list = ofy().query(clazz).list();
		return list;
	}

	/**
	 * Convenience method to get all objects matching a single property
	 * 
	 * 
	 * @param propName
	 * 
	 * @param propValue
	 * 
	 * @return T matching Object
	 */
	public T getByProperty(String propName, Object propValue)
	{
		Query<T> q = ofy().query(clazz);

		q.filter(propName, propValue);

		return q.get();

	}

}
