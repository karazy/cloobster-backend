package net.eatsense.domain;

import javax.persistence.Id;

public abstract class GenericEntity {
	
	@Id
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	

}
