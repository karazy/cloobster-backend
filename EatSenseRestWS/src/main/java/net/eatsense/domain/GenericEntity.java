package net.eatsense.domain;

import javax.persistence.Id;
import javax.persistence.Transient;

import com.googlecode.objectify.Key;

/**
 * Base class for all entities.
 * Provides some common fields.
 * 
 * @author Frederik Reifschneider
 *
 */
public abstract class GenericEntity<T> {
	
	/**
	 * Unique identifier for this entity.
	 */
	@Id
	private Long id;
	
	@Transient
	private boolean dirty = false; 
	
	private boolean trash = false;
	
	@Transient
	public boolean isDirty() {
		return dirty;
	}
	
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public boolean isTrash() {
		return trash;
	}

	public void setTrash(boolean trash) {
		this.trash = trash;
	}
	
	public abstract Key<T> getKey();
}
