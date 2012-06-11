package net.eatsense.domain;

import javax.persistence.Id;

/**
 * Base class for all entities.
 * Provides some common fields.
 * 
 * @author Frederik Reifschneider
 *
 */
public abstract class GenericEntity{
	
	/**
	 * Unique identifier for this entity.
	 */
	@Id
	private Long id;
	
	private boolean dirty = false; 

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
	
}
