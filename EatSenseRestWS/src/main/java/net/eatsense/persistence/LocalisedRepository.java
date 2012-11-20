package net.eatsense.persistence;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.eatsense.annotations.Translate;
import net.eatsense.domain.GenericEntity;
import net.eatsense.domain.TranslatedEntity;
import net.eatsense.domain.embedded.TranslatedField;
import net.eatsense.domain.translation.InfoPageT;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.exceptions.ServiceException;

import org.apache.commons.beanutils.BeanUtils;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;

/**
 * Base class for repositories, which want to deal with translated entities.
 * 
 * @author Nils Weiher
 *
 * @param <T> The domain object the repository handles.
 * @param <U>
 */
public  class LocalisedRepository<T extends GenericEntity<T>, U extends TranslatedEntity<T>> extends GenericRepository<T> {

	private Class<U> translationClass;

	public LocalisedRepository(Class<T> clazz, Class<U> tClazz) {		
		super(clazz);
		this.translationClass = tClazz;
		try {
			ObjectifyService.register(tClazz);
		} catch (IllegalArgumentException e) {
			// We already registered the entity, okay to skip this.
		}
	}
	
	public T get(Key<T> entityKey, Locale locale) {
		checkNotNull(entityKey, "entityKey was null");
		checkNotNull(locale, "locale was null");
		checkArgument(!locale.getLanguage().isEmpty(), "locale must have valid language code");
		
		Key<U> translationKey = Key.create(entityKey, translationClass, locale.getLanguage());
		@SuppressWarnings("unchecked")
		Map<Key<Object>, Object> resultMap = ofy().get(entityKey, translationKey);
		
		@SuppressWarnings("unchecked")
		T entity = (T) resultMap.get(entityKey);
		
		if(entity == null) {
			throw new NotFoundException("No entity found for key: " + entityKey.toString());
		}
		
		U transEntity = (U) resultMap.get(translationKey);
		
		if(transEntity != null) {
			transEntity.applyTranslation(entity);
		}
		
		return entity;
	}
	
	public <V> List<T> getByParent(Key<V> parentKey, Locale locale) {
		checkNotNull(locale, "locale was null");
		checkArgument(!locale.getLanguage().isEmpty(), "locale must have valid language code");
		
		logger.info("kind={}, parent={}, lang={}", new Object[]{clazz ,parentKey,locale.getLanguage()});

		return loadAndApplyTranslations(ofy().query(clazz).ancestor(parentKey), locale);
	}
	
	public Key<U> createTranslationKey(Key<T> originalKey, Locale locale) {
		return Key.create(originalKey, translationClass, locale.getLanguage());
	}
	
	public List<T> loadAndApplyTranslations(Query<T> entityQuery, Locale locale) {
				
		List<Key<T>> entityKeys = entityQuery.listKeys();
		List<Key<?>> allKeys = new ArrayList<Key<?>>();
		List<Key<U>> translationKeys = new ArrayList<Key<U>>();
		List<T> entities = new ArrayList<T>();
		
		for (Key<T> entityKey : entityKeys) {
			allKeys.add(entityKey);
			Key<U> translationKey = createTranslationKey(entityKey, locale);
			translationKeys.add(translationKey);
			allKeys.add(translationKey);
		}
		
		Map<Key<Object>, Object> allEntities = ofy().get(allKeys);
		Iterator<Key<U>> translationKeysIter = translationKeys.iterator();
		
		for (Iterator<Key<T>> iterator = entityKeys.iterator(); iterator.hasNext();) {
			Key<T> entityKey = iterator.next();
			Key<U> transKey = translationKeysIter.next();
			
			@SuppressWarnings("unchecked")
			T entity = (T) allEntities.get(entityKey);
			
			@SuppressWarnings("unchecked")
			U transEntity = (U) allEntities.get(transKey);
			
			if(transEntity != null) {
				transEntity.applyTranslation(entity);
			}
			
			entities.add(entity);
		}
				
		return entities;
	}
	
	/**
	 * Save an entity and a translation of the entity in the specified language.
	 * The saved data is the same for the translation and the original entity.
	 * 
	 * @param entity the entity to write
	 * @param locale 
	 * @return
	 */
	public Key<T> saveOrUpdate(T entity, Optional<Locale> locale) {
		checkNotNull(entity, "entity was null");
		checkNotNull(locale, "locale was null");
		checkArgument( locale.isPresent() && !locale.get().getLanguage().isEmpty(), "locale must have valid language code");
		
		logger.info("{}({}), locale={}", new Object[]{Key.getKind(clazz), entity.getId(), locale});
		
		Key<T> entityKey = ofy().put(entity);
		
		if(locale.isPresent()) {
			saveOrUpdateTranslation(entity, locale.get());
		}
		
		return entityKey;
	}
	
	private U createAndFill(T entity, Locale locale) {
		U translationEntity;
		try {
			translationEntity = translationClass.newInstance();
		} catch (Exception e) {
			logger.error("Error instantiating translation entity", e);
			throw new ServiceException("Internal error while creating translation model", e);
		}
		
		translationEntity.setLang(locale.getLanguage());
		translationEntity.setParent(entity.getKey());
		translationEntity.setFieldsFromEntity(entity);
		
		return translationEntity;
	}
	
	/**
	 * Save an entity translation for a specified language.
	 * 
	 * @param entity the entity that contains the translation
	 * @param locale language of the entity to save
	 */
	public U saveOrUpdateTranslation(T entity, Locale locale) {
		checkNotNull(entity, "entity was null");
		checkArgument(!locale.getLanguage().isEmpty(), "locale must have valid language code");
		
		logger.info("{}({}), locale={}", new Object[]{Key.getKind(clazz), entity.getId(), locale});
		
		U translationEntity = createAndFill(entity, locale);
		
		ofy().put(translationEntity);
		
		return translationEntity;
	}
	
	public void deleteWithTranslation(Key<T> entityKey) {
		checkNotNull(entityKey, "entityKey was null");
		
		logger.info("key={}",entityKey);
						
		ofy().delete(ofy().query(translationClass).ancestor(entityKey).fetchKeys());
		ofy().delete(entityKey);
	}
}
