package net.eatsense.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.eatsense.domain.Account;
import net.eatsense.domain.Bill;
import net.eatsense.domain.Choice;
import net.eatsense.domain.Menu;
import net.eatsense.domain.NicknameAdjective;
import net.eatsense.domain.NicknameNoun;
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.Product;
import net.eatsense.domain.Request;
import net.eatsense.domain.Spot;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Objectify;
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
		//Register classes with Objectify
		ObjectifyService.register(Request.class);
		ObjectifyService.register(Account.class);
		ObjectifyService.register(Business.class);
		ObjectifyService.register(Spot.class);
		ObjectifyService.register(Menu.class);
		ObjectifyService.register(Product.class);
		ObjectifyService.register(CheckIn.class);
		ObjectifyService.register(Choice.class);
		ObjectifyService.register(Order.class);
		ObjectifyService.register(Bill.class);
		ObjectifyService.register(OrderChoice.class);
		ObjectifyService.register(NicknameAdjective.class);
		ObjectifyService.register(NicknameNoun.class);
		
	}

	protected ObjectifyService datastore;

	@Inject
	public GenericRepository() {
//		 this.clazz = (Class<T>) ((ParameterizedType)
//				 getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		//this.datastore = datastore;
	}

	/**
	 * Saves or update given object.
	 * @param obj
	 * 		Object to save.
	 * @return
	 * 		Generated/existing key
	 */
	public Key<T> saveOrUpdate(T obj) {
		logger.info("saveOrUpdate {} ", obj);
		
		return ofy().put(obj);
	}
	
	/**
	 * Saves or update given object.
	 * @param obj
	 * 		List containing objects to save.
	 * @return
	 * 		Map of generated/existing keys and entities
	 */
	public Map<Key<T>, T> saveOrUpdate(Iterable<T> obj) {
		logger.info("saveOrUpdate {} ", obj);
		
		return ofy().put(obj);
	}

	/**
	 * Delete object
	 * @param obj
	 * 		Object to delete.
	 */
	public void delete(T obj) {
		logger.info("delete {} ", obj);
		ofy().delete(obj);
	}
	
	/**
	 * Delete object by key
	 * @param obj
	 * 		Key of object to delete.
	 */
	public void delete(Key<T> obj) {
		logger.info("delete {} ", obj);
		ofy().delete(obj);
	}

	/**
	 * Finds an object by id. ONLY WORKING WITH OBJECTS WITH NO PARENT ANNOTATION
	 * @param id
	 * 		Id of entity to find.
	 * @return
	 * 		Found entity.
	 * @throws NotFoundException if no entity with the given id was found
	 */
	public T getById(long id) throws NotFoundException {
		logger.info("findByKey {} ", id);
		
		return ofy().get(clazz, id);
	}

	/**
	 * Gets entity by id and owner. Use this for objects with @Parent annotation.
	 * 
	 * @param owner
	 * 		parent of entity
	 * @param id
	 * 		Id of entity to load
	 * @return
	 * 		Found entity
	 * @throws NotFoundException if no entity with the given id and parent was found
	 */
	public <V> T getById(Key<V> owner, long id) throws NotFoundException {
		logger.info("getByKey {} ", id); 
		return ofy().get(new Key<T>(owner, clazz, id));
	}
	
	/**
	 * Gets entity by it's key.
	 * 
	 * @param key
	 * 		Key object of entity to load
	 * @return
	 * 		Found entity
	 */
	public <V> T getByKey(Key<T> key) throws NotFoundException {
		logger.info("getByKey {} ", key); 
		return ofy().get(key);
	}
	
	/**
	 * Gets a list of entities by a list of keys.
	 * 
	 * @param id
	 * 		List of ids of entities to load
	 * @return
	 * 		List of entities
	 */
	public Collection<T> getByKeys(List<Key<T>> keys) throws NotFoundException {
		
		return ofy().get(keys).values();
	}
	
	/**
	 * Gets a map of entities by a list of keys.
	 * 
	 * @param id
	 * 		List of ids of entities to load
	 * @return
	 * 		Map of entities, mapping given key objects to the entity.
	 */
	public Map<Key<T>, T> getByKeysAsMap(List<Key<T>> keys) throws NotFoundException {
		
		return ofy().get(keys);
	}

	/**
	 * Returns children of a parent entity.
	 * Performs an ancestor query.
	 * 
	 * @param parentKey
	 * 			Key of parent. Doesn't have to be the direct parent.
	 * @return
	 * 		List with children of given parent
	 */
	public <V> List<T> getByParent( Key<V> parentKey) {
		logger.info("getChildren for {} ", parentKey);
		return ofy().query(clazz).ancestor(parentKey).list();
	}
	
	/**
	 * Returns children of a parent entity.
	 * Performs an ancestor query.
	 * 
	 * @param parent
	 * 			parent entity. Doesn't have to be the direct parent.
	 * @return
	 * 		List with children of given parent
	 */
	public <V> List<T> getByParent( V parent) {
		logger.info("getChildren for {} ", parent);
		return ofy().query(clazz).ancestor(parent).list();
	}

	/**
	 * Gets all entities of type T.
	 * @return
	 * 		Collection of entities of type T
	 */
	public Collection<T> getAll() {
		logger.info("getAll entities of type {} ", clazz);
		Collection<T> list = ofy().query(clazz).list();
		return list;
	}
	
	/**
	 * Gets all key of entities of type T.
	 * @return
	 * 		List of keys of type T
	 */
	public List<Key<T>> getAllKeys() {
		logger.info("getAllKeys of type {} ", clazz);
		List<Key<T>> list = ofy().query(clazz).listKeys();
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
	 * @return T matching Object or NULL if no match found
	 */
	public T getByProperty(String propName, Object propValue)
	{
		Query<T> q = ofy().query(clazz);

		q.filter(propName, propValue);

		return q.get();
	}
	
	/**
	 * Convenience method to get all objects matching a single property
	 * 
	 * 
	 * @param propName
	 * 
	 * @param propValue
	 * 
	 * @return List<T> of matching objects
	 */
	public List<T> getListByProperty(String propName, Object propValue)
	{
		Query<T> q = ofy().query(clazz);

		q.filter(propName, propValue);

		return q.list();

	}
	
	/**
	 * Convenience method to get all object keys matching a single property
	 * 
	 * 
	 * @param propName
	 * 
	 * @param propValue
	 * 
	 * @return List<T> of matching objects
	 */
	public List<Key<T>> getKeysByProperty(String propName, Object propValue)
	{
		Query<T> q = ofy().query(clazz);

		q.filter(propName, propValue);

		return q.listKeys();

	}
	
	/**
	 * Convenience method to get all objects matching a single property.
	 * orderby is the property to order the list f.e. "age" for ascending order by the "age" property 
	 * 		"-age" for descending. 
	 * 
	 * @param propName
	 * 
	 * @param propValue
	 * 
	 * @param orderBy name of a property by which to order.
	 * 
	 * @return List<T> of matching objects
	 */
	public List<T> getListByPropertyOrdered(String propName, Object propValue, String orderBy)
	{
		Query<T> q = ofy().query(clazz).filter(propName, propValue).order(orderBy);

		return q.list();

	}
	
	/**
	 * Count entities matching a filter.
	 * 
	 * @param propFilter
	 * @param propValue
	 * @return Count of matching entities
	 */
	public int countByProperty(String propFilter, Object propValue) {
		return ofy().query(clazz).filter(propFilter, propValue).count();
	}
	
	/**
	 * Return a typesafe objectify query object.
	 * 
	 * @return
	 */
	public Query<T> query() {
		return ofy().query(clazz);
	}
		
	/**
	 * Returns the {@link Objectify} object to directly query datastore. 
	 * @return
	 */
	public Objectify getOfy() {
		return ofy();
	}

}
