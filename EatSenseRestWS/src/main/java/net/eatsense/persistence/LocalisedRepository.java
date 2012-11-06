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
public abstract class LocalisedRepository<T extends GenericEntity<T>, U extends TranslationEntity<T>> extends GenericRepository<T> {

	public LocalisedRepository(Class<T> clazz, Class<U> clazzT) {
		super(clazz);
		try {
			ObjectifyService.register(clazzT);
		} catch (IllegalArgumentException e) {
			// We already registered the entity, okay to skip this.
		}
	}
	
	public abstract T applyLocalisation(T entity, U translationEntity);
}
