package net.eatsense.domain;

import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import net.eatsense.domain.embedded.DocumentStatus;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.common.base.Objects;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

public class Document extends GenericEntity<Document>{
	
	@Parent
	@NotNull
	private Key<Business> business;
	
	private String name;
	
	private String type;
	
	private String entity;
	
	private DocumentStatus status;
	
	@NotNull
	private Date createDate;
	
	private String representation;
	
	@Unindexed
	private List<Long> entityIds;
	
	@Unindexed
	private List<String> entityNames;
	
	private BlobKey blobKey;

	@Transient
	public Key<Document> getKey() {
		return new Key<Document>(getBusiness(), Document.class, super.getId());
	}

	public Key<Business> getBusiness() {
		return business;
	}

	public void setBusiness(Key<Business> business) {
		if(!Objects.equal(this.business, business)) {
			this.setDirty(true);
			this.business = business;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(!Objects.equal(this.name, name)) {
			this.setDirty(true);
			this.name = name;
		}
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		if(!Objects.equal(this.type, type)) {
			this.setDirty(true);
			this.type = type;
		}
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		if(!Objects.equal(this.entity, entity)) {
			this.setDirty(true);
			this.entity = entity;
		}
	}

	public DocumentStatus getStatus() {
		return status;
	}

	public void setStatus(DocumentStatus status) {
		if(!Objects.equal(this.status, status)) {
			this.setDirty(true);
			this.status = status;
		}
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		if(!Objects.equal(this.createDate, createDate)) {
			this.setDirty(true);
			this.createDate = createDate;
		}
	}

	public String getRepresentation() {
		return representation;
	}

	public void setRepresentation(String representation) {
		if(!Objects.equal(this.representation, representation)) {
			this.setDirty(true);
			this.representation = representation;
		}
	}

	public BlobKey getBlobKey() {
		return blobKey;
	}

	public void setBlobKey(BlobKey blobKey) {
		if(!Objects.equal(this.blobKey, blobKey)) {
			this.setDirty(true);
			this.blobKey = blobKey;
		}
	}

	public List<Long> getEntityIds() {
		return entityIds;
	}

	public void setEntityIds(List<Long> entityIds) {
		if(!Objects.equal(this.entityIds, entityIds)) {
			this.setDirty(true);
			this.entityIds = entityIds;
		}
	}

	public List<String> getEntityNames() {
		return entityNames;
	}

	public void setEntityNames(List<String> entityNames) {
		this.entityNames = entityNames;
	}	
}
