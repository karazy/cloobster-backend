package net.eatsense.domain;

import java.net.URL;
import java.util.Date;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import net.eatsense.domain.embedded.DocumentStatus;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;

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
	
	private URL url;
	
	private String[] ids;

	@Transient
	public Key<Document> getKey() {
		return new Key<Document>(getBusiness(), Document.class, super.getId());
	}

	public Key<Business> getBusiness() {
		return business;
	}

	public void setBusiness(Key<Business> business) {
		this.business = business;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public DocumentStatus getStatus() {
		return status;
	}

	public void setStatus(DocumentStatus status) {
		this.status = status;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String[] getIds() {
		return ids;
	}

	public void setIds(String[] ids) {
		this.ids = ids;
	}
	
}
