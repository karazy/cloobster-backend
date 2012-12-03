package net.eatsense.persistence;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.eatsense.domain.Business;
import net.eatsense.domain.GenericEntity;
import net.eatsense.domain.TranslatedEntity;
import net.eatsense.domain.TrashEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author Frederik Reifschneider, Nils Weiher
 * 
 * @param <T>
 */
public class GenericRepository<T extends GenericEntity<T>> extends DAOBase{
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	protected Class<T> clazz;
	
	static {
		ObjectifyService.register(TrashEntry.class);
	}

	public GenericRepository(Class<T> clazz) {
		this.clazz = clazz;
		//OPTIMIZE This part is still not the best way to handle the entity registration.
		try {
			ObjectifyService.register(clazz);
		} catch (IllegalArgumentException e) {
			// We already registered the entity, okay to skip this.
		}
	}
	
	/**
	 * Create a new instance of an entity.
	 * 
	 * @return new instance of class of T
	 */
	public T newEntity() {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException("Unable to create new "+clazz.getSimpleName(),e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to create new "+clazz.getSimpleName(),e);
		}
	}

	/**
	 * Saves or update given object.
	 * @param obj
	 * 		Object to save.
	 * @return
	 * 		Generated/existing key
	 */
	public Key<T> saveOrUpdate(T obj) {
		logger.info("{}", obj);
		Key<T> key = ofy().put(obj);
		obj.setDirty(false);
		return key;
	}
	
	/**
	 * Saves or update given object.
	 * @param obj
	 * 		List containing objects to save.
	 * @return
	 * 		Map of generated/existing keys and entities
	 */
	public Map<Key<T>, T> saveOrUpdate(Iterable<T> obj) {
		logger.info("{}", obj);
		
		return ofy().put(obj);
	}

	/**
	 * Delete object
	 * @param obj
	 * 		Object to delete.
	 */
	public void delete(T obj) {
		logger.info("{}", obj);
		ofy().delete(obj);
	}
	
	/**
	 * Delete object by key
	 * @param obj
	 * 		Key of object to delete.
	 */
	public void delete(Key<T> obj) {
		logger.info("{}", obj);
		ofy().delete(obj);
	}
	
	/**
	 * Delete object by keys
	 * @param keyIterable
	 * 		Keys of objects to delete.
	 */
	public void delete(Iterable<Key<T>> keyIterable) {
		logger.info("{}", keyIterable);
		ofy().delete(keyIterable);
	}
	
	/**
	 * Shortcut for getting an entity and saving as trash.
	 * 
	 * @param entityKey
	 * @param loginResponsible
	 * @return The entry saved for trash.
	 */
	public TrashEntry trashEntity(Key<T> entityKey, String loginResponsible) {
		return trashEntity(ofy().get(entityKey), loginResponsible);
	}
	
	/**
	 * Save the Entity as trash, creates a TrashEntry for this entity, for later deletion.
	 * 
	 * @param entity
	 * @param loginResponsible
	 * @return The TrashEntry for this entity.
	 */
	public TrashEntry trashEntity(T entity, String loginResponsible) {
		Key<T> key = entity.getKey();
		logger.info("{}", key);
		TrashEntry trashEntry = new TrashEntry(key, Key.getKind(clazz), new Date(), loginResponsible);
		entity.setTrash(true);
		ofy().put(entity);
		ofy().put(trashEntry);
		return trashEntry;
	}
	
	/**
	 * Save the Entities as trash, creates a TrashEntry for this entity, for later deletion.
	 * 
	 * @param entities
	 * @param loginResponsible
	 * @return TrashEntries for the entities
	 */
	public List<TrashEntry> trashEntities(Iterable<T> entities, String loginResponsible) {
		List<TrashEntry> trashEntries = new ArrayList<TrashEntry>();
		List<Object> entitiesToSave = new ArrayList<Object>();
		if(entities == null || !entities.iterator().hasNext()) {
			return trashEntries;
		}

		for (T entity : entities) {
			TrashEntry trashEntry = new TrashEntry(entity.getKey(), Key.getKind(clazz), new Date(), loginResponsible);
			entity.setTrash(true);
			
			trashEntries.add(trashEntry);
			
			// Add to our save list
			entitiesToSave.add(trashEntry);
			entitiesToSave.add(entity);
		}
		
		logger.info("kind={}, number={}", Key.getKind(clazz), trashEntries.size());
		
		ofy().put(entitiesToSave);
		
		return trashEntries;
	}
		
	/**
	 * @param trashEntryKey
	 * @return
	 */
	public T restoreTrashedEntity(Key<TrashEntry> trashEntryKey) {
		checkNotNull(trashEntryKey, "trashEntryKey was null");
		TrashEntry trashEntry = ofy().get(trashEntryKey);
		checkArgument(trashEntry.getEntityKey().getKind().equals(Key.getKind(clazz)), "Trashed entity not of correct type");
		
		T entity = ofy().get((Key<T>)trashEntry.getEntityKey());
		entity.setTrash(false);
		
		ofy().put(entity);
		
		ofy().delete(trashEntryKey);
		
		return entity;
	}

	
	/**
	 * @return All TrashEntry items in the store.
	 */
	public List<TrashEntry> getAllTrash() {
		return ofy().query(TrashEntry.class).list();
	}
	
	/**
	 * @param trashEntries
	 */
	private List<TrashEntry> deleteTrash(Iterable<TrashEntry> trashEntries) {
		List<Key<?>> keysToDelete = new ArrayList<Key<?>>();
		List<TrashEntry> trashList = new ArrayList<TrashEntry>();
		
		for (TrashEntry trashEntry : trashEntries) {
			keysToDelete.add(trashEntry.getEntityKey());
			trashEntry.setDeletionDate(new Date());
			
			trashList.add(trashEntry);
		}
		ofy().put(trashList);
		
		ofy().delete(keysToDelete);
		
		return trashList;
	}
	
	/**
	 * Permanently delete entities in the trash.
	 * 
	 * @return List of TrashEntries for the deleted Entities.
	 */
	public List<TrashEntry> deleteAllTrash() {
		return deleteTrash(ofy().query(TrashEntry.class));
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
		logger.info("{} id:{}",clazz, id);
		
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
		logger.info("{} id:{}",clazz, id); 
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
		logger.info("{}", key); 
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
		logger.info("{}", keys); 
		return ofy().get(keys).values();
	}
	
	/**
	 * Gets a list of entities by a list of Ids.
	 * 
	 * @param id
	 * 		List of ids of entities to load
	 * @return
	 * 		List of entities
	 */
	public Collection<T> getByIds(List<Long> ids) throws NotFoundException {
		logger.info("{}", ids);
		
		return ofy().get(clazz, ids).values();
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
		logger.info("{}", keys);
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
		logger.info("{}, parent: {}",clazz, parentKey);
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
		logger.info("{}, parent: {}",clazz, parent);
		return ofy().query(clazz).ancestor(parent).list();
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
	public <V> List<T> getByParentOrdered( V parent, String orderBy) {
		logger.info("{}, parent: {}, orderBy: {}", new Object[]{ clazz, parent, orderBy} );
		return ofy().query(clazz).ancestor(parent).order(orderBy).list();
	}

	/**
	 * Gets all entities of type T.
	 * @return
	 * 		Collection of entities of type T
	 */
	public Collection<T> getAll() {
		logger.info("{}", clazz);
		Collection<T> list = ofy().query(clazz).list();
		return list;
	}
	
	/**
	 * Gets all key of entities of type T.
	 * @return
	 * 		List of keys of type T
	 */
	public List<Key<T>> getAllKeys() {
		logger.info("{}", clazz);
		List<Key<T>> list = ofy().query(clazz).listKeys();
		return list;
	}

	/**
	 * Convenience method to get the first object matching a single property
	 * 
	 * 
	 * @param propName
	 * 
	 * @param propValue
	 * 
	 * @return T matching Object or <code>null</code> if no match found
	 */
	public T getByProperty(String propName, Object propValue)
	{
		logger.info("{}, property: {}", clazz, propName);
		Query<T> q = ofy().query(clazz);

		q.filter(propName, propValue);

		return q.get();
	}
	
	/**
	 * Convenience method to get the Key of the first object matching a single property.
	 * 
	 * 
	 * @param propName
	 * 
	 * @param propValue
	 * 
	 * @return Key of matching Object or <code>null</code> if no match found
	 */
	public Key<T> getKeyByProperty(String propName, Object propValue)
	{
		logger.info("{}, property: {}", clazz, propName);
		Query<T> q = ofy().query(clazz);

		q.filter(propName, propValue);

		return q.getKey();
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
		logger.info("{}, property: {}", clazz, propName);
		Query<T> q = ofy().query(clazz);

		q.filter(propName, propValue);

		return q.list();

	}
	
	/**
	 * Convenience method to get all objects matching a single property and Parent
	 * @param <V> Type of the parent entity.
	 * 
	 * @param parent
	 * @param propName
	 * 
	 * @param propValue
	 * 
	 * @return List<T> of matching objects
	 */
	public <V extends GenericEntity<V>> List<T> getListByParentAndProperty(Key<V> parent, String propName, Object propValue)
	{
		logger.info("{}, property: {}", clazz, propName);
		Query<T> q = ofy().query(clazz).ancestor(parent);

		q.filter(propName, propValue);

		return q.list();

	}
	
	/**
	 * Convenience method to get all objects matching a single property and Parent
	 * @param <V> Type of the parent entity.
	 * 
	 * @param parent
	 * @param propName
	 * 
	 * @param propValue
	 * 
	 * @return List<Key<T>> of matching entity keys.
	 */
	public <V extends GenericEntity<V>> List<Key<T>> getKeysByParentAndProperty(Key<V> parent, String propName, Object propValue)
	{
		logger.info("{}, property: {}", clazz, propName);
		Query<T> q = ofy().query(clazz).ancestor(parent);

		q.filter(propName, propValue);

		return q.listKeys();
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
		logger.info("{}, property: {}", clazz, propName);
		Query<T> q = ofy().query(clazz);

		q.filter(propName, propValue);

		return q.listKeys();

	}
	
	/**
	 * Convenience method to get all object keys matching a single property
	 * @param <V>
	 * 
	 * @param parentKey
	 * @return List<Key<T>> of matching entity keys
	 */
	public <V> List<Key<T>> getKeysByParent(Key<? extends GenericEntity<V>> parentKey)
	{
		logger.info("{}, parent: {}", clazz, parentKey);
		Query<T> q = ofy().query(clazz);

		q.ancestor(parentKey);

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
		logger.info("{} property: {}, orderBy: {}", new Object[]{ clazz, propName, orderBy});
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
		logger.info("{}, property: {}", clazz, propFilter);
		return ofy().query(clazz).filter(propFilter, propValue).count();
	}
	
	/**
	 * Return a typesafe objectify query object.
	 * 
	 * @return
	 */
	public Query<T> query() {
		logger.info("entity: {}", clazz);
		return ofy().query(clazz);
	}
		
	/**
	 * Returns the {@link Objectify} object to directly query datastore. 
	 * @return
	 */
	public Objectify getOfy() {
		return ofy();
	}

	/**
	 * Create a typesafe wrapper for the datastore key object.
	 * @param <V>
	 * 
	 * @param parent parent {@link Key}
	 * @param id numerical identifier
	 * @return {@link Key}
	 */
	public <V> Key<T> getKey(Key<? extends GenericEntity<V>> parent, long id) {
		return new Key<T>(parent , clazz, id);
	}
	
	/**
	 * Create a typesafe wrapper for the datastore key object.
	 * 
	 * @param id numerical identifier
	 * @return {@link Key} of type T
	 */
	public Key<T> getKey(long id) {
		return new Key<T>( clazz, id);
	}
	
	/**
	 * Create a typesafe wrapper for the datastore key object.
	 * 
	 * @param id numerical identifier
	 * @return {@link Key} of type T
	 */
	public Key<T> getKey(T obj) {
		return obj.getKey();
	}
}
