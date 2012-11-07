package net.eatsense.persistence;

import com.googlecode.objectify.ObjectifyService;

import net.eatsense.domain.GenericEntity;
import net.eatsense.domain.TranslationEntity;

/**
 * Base class for repositories, which want to deal with translated entities.
 * 
 * @author Nils Weiher
 *
 * @param <T> The domain object the repository handles.
 * @param <U>
 */
public  class LocalisedRepository<T extends GenericEntity<T>, U extends TranslationEntity<T>> extends GenericRepository<T> {

	public LocalisedRepository(Class<T> clazz) {
		super(clazz);
	}
	
	public T applyLocalisation(T entity, U translationEntity) {
		return entity;
	}
}
