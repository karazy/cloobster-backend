package net.eatsense.persistence;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import net.eatsense.annotations.Translate;
import net.eatsense.domain.GenericEntity;
import net.eatsense.domain.TranslationEntity;
import net.eatsense.domain.embedded.TranslatedField;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.exceptions.ServiceException;

import org.apache.commons.beanutils.BeanUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.googlecode.objectify.Key;

/**
 * Base class for repositories, which want to deal with translated entities.
 * 
 * @author Nils Weiher
 *
 * @param <T> The domain object the repository handles.
 * @param <U>
 */
public  class LocalisedRepository<T extends GenericEntity<T>> extends GenericRepository<T> {
	
	private final static Multimap<String, String> translatedFields = ArrayListMultimap.create();
	
	public LocalisedRepository(Class<T> clazz) {		
		super(clazz);
		String entityName = clazz.getSimpleName();
		
		if(!translatedFields.containsKey(entityName)) {
			Field[] fields = clazz.getDeclaredFields();

			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if(field.isAnnotationPresent(Translate.class)) {
					
					translatedFields.put(entityName, field.getName());
				}
			}
		}		
	}
	
	public T get(Key<T> entityKey, Locale locale) {
		checkNotNull(entityKey, "entityKey was null");
		checkArgument(locale.getLanguage().isEmpty(), "locale must have valid language code");
		
		Key<TranslationEntity> translationKey = Key.create(entityKey, TranslationEntity.class, locale.getLanguage());
		@SuppressWarnings("unchecked")
		Map<Key<Object>, Object> resultMap = ofy().get(entityKey, translationKey);
		
		@SuppressWarnings("unchecked")
		T entity = (T) resultMap.get(entityKey);
		
		if(entity == null) {
			throw new NotFoundException("No entity found for key: " + entityKey.toString());
		}
		
		TranslationEntity transEntity = (TranslationEntity) resultMap.get(translationKey);
		
		if(transEntity != null) {
			try {
				BeanUtils.populate(entity, transEntity.getFieldsMap());
			} catch (Exception e) {
				logger.error("Could not apply translation", e);
				throw new ServiceException("Unable to apply translation to fields",e);
			}
		}
		
		return entity;
	}
	
	public Collection<String> getTranslatedFieldNames() {
		return translatedFields.get(clazz.getSimpleName());
	}
	
	public void saveOrUpdateTranslation(T entity, Locale locale) {
		checkNotNull(entity, "entity was null");
		checkArgument(!locale.getLanguage().isEmpty(), "locale must have valid language code");
		
		TranslationEntity translationEntity = new TranslationEntity(locale.getLanguage(), entity.getKey());
		ArrayList<TranslatedField> translation = new ArrayList<TranslatedField>(); 
		for (String fieldName : translatedFields.get(clazz.getSimpleName())) {
			String fieldValue;
			try {
				fieldValue = BeanUtils.getSimpleProperty(entity, fieldName);
			} catch (Exception e) {
				logger.error("Could not read field from entity", e);
				throw new ServiceException("Unable to save translation",e);
			}
			
			translation.add(new TranslatedField(fieldName, fieldValue));
		}
		
		translationEntity.setFields(translation);
		
		
		ofy().put(translationEntity);
	}
}
