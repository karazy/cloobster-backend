package net.eatsense.domain;

import java.util.Date;

import com.googlecode.objectify.Key;

public class TrashEntry extends GenericEntity {
	private Key<?> entityKey;
	private Date deletionDate;
	private Date markedForDeletionDate;
	private String loginResponsible;
	
	/**
	 * Empty constructor for Objectify.
	 */
	public TrashEntry() {
		super();
	}

	public TrashEntry(Key<?> entityKey, Date markedForDeletionDate,String loginResponsible) {
		super();
		this.entityKey = entityKey;
		this.markedForDeletionDate = markedForDeletionDate;
		this.loginResponsible = loginResponsible;
	}
	
	public Key<?> getEntityKey() {
		return entityKey;
	}
	public void setEntityKey(Key<? extends GenericEntity> entityKey) {
		this.entityKey = entityKey;
	}
	public Date getDeletionDate() {
		return deletionDate;
	}
	public void setDeletionDate(Date deletionDate) {
		this.deletionDate = deletionDate;
	}
	public String getLoginResponsible() {
		return loginResponsible;
	}
	public void setLoginResponsible(String loginResponsible) {
		this.loginResponsible = loginResponsible;
	}

	public Date getMarkedForDeletionDate() {
		return markedForDeletionDate;
	}

	public void setMarkedForDeletionDate(Date markedForDeletionDate) {
		this.markedForDeletionDate = markedForDeletionDate;
	}
	
}
