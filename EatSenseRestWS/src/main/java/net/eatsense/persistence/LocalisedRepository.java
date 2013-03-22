package net.eatsense.persistence;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import net.eatsense.domain.GenericEntity;
import net.eatsense.domain.TranslatedEntity;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.exceptions.ServiceException;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
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

	public static class EntityWithTranlations<V extends GenericEntity<V>, W extends TranslatedEntity<V>> {
		public EntityWithTranlations(V entity, Map<Locale, W> translations) {
			super();
			this.entity = entity;
			this.translations = translations;
		}
		
		private V entity;
		private Map<Locale, W> translations;
		
		public Map<Locale, W> getTranslations() {
			return translations;
		}
		public void setTranslations(Map<Locale, W> translations) {
			this.translations = translations;
		}
		public V getEntity() {
			return entity;
		}
		public void setEntity(V entity) {
			this.entity = entity;
		}
	}

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
		
		@SuppressWarnings("unchecked")
		U transEntity = (U) resultMap.get(translationKey);
		
		if(transEntity != null) {
			transEntity.applyTranslation(entity);
		}
		
		return entity;
	}
	
	public EntityWithTranlations<T,U> getWithTranslations(Key<T> entityKey, List<Locale> locales) {
		checkNotNull(entityKey, "entityKey was null");
		
		List<Key<?>> entityKeys = new ArrayList<Key<?>>();
		
		for (Locale locale : locales) {
			entityKeys.add(Key.create(entityKey, translationClass, locale.getLanguage()));
		}
		
		if(entityKeys.isEmpty()) {
			return new EntityWithTranlations<T, U>(ofy().get(entityKey), null);
		}
		else {
			entityKeys.add(entityKey);
			Map<Key<Object>, Object> resultMap = ofy().get(entityKeys);
			
			@SuppressWarnings("unchecked")
			T entity = (T) resultMap.get(entityKey);
			Map<Locale, U> translations = Maps.newHashMap();
			
			for ( Key<?> translationKeys  : entityKeys) {
				if(translationKeys.getName() == null) {
					// Skip the original entity
					continue;
				}
				else {
					U translatedEntity = (U) resultMap.get(translationKeys);
					if(translatedEntity == null) {
						try {
							translatedEntity = translationClass.newInstance();
						} catch (Exception e) {
							logger.error("Error instantiating translation class", e);
							throw new ServiceException("Internal error while creating translation model", e);
						}
					}
					
					translations.put(new Locale(translationKeys.getName()), translatedEntity);
				}
			}
			
			return new EntityWithTranlations<T, U>(entity, translations);
		}
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
