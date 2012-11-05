package net.eatsense.persistence;

import net.eatsense.domain.GenericEntity;

/**
 * Base class for repositories, which want to deal with translated entities.
 * 
 * @author Nils Weiher
 *
 * @param <T> The domain object the repository handles.
 */
public class LocalisedRepository<T extends GenericEntity<T>> extends GenericRepository<T> {

	public LocalisedRepository(Class<T> clazz) {
		super(clazz);
		// TODO Auto-generated constructor stub
	}
}
