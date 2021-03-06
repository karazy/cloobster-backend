package net.eatsense.domain;

import java.util.Date;

import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.googlecode.objectify.Key;

public class TrashEntry extends GenericEntity<TrashEntry> {
	private Key<?> entityKey;
	private String kind;
	private Date deletionDate;
	private Date markedForDeletionDate;
	private String loginResponsible;
	
	/**
	 * Empty constructor for Objectify.
	 */
	public TrashEntry() {
		super();
	}

	public TrashEntry(Key<?> entityKey, String kind, Date markedForDeletionDate,String loginResponsible) {
		super();
		this.kind = kind;
		this.entityKey = entityKey;
		this.markedForDeletionDate = markedForDeletionDate;
		this.loginResponsible = loginResponsible;
	}
	
	public Long getEntityId() {
		return entityKey != null? entityKey.getId(): null;
	}
	
	@JsonIgnore
	public Key<?> getEntityKey() {
		return entityKey;
	}
	@JsonIgnore
	public void setEntityKey(Key<?> entityKey) {
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

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	@Override
	@Transient
	@JsonIgnore
	public Key<TrashEntry> getKey() {
		return new Key<TrashEntry>(TrashEntry.class, getId());
	}
	
}
