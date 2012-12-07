package net.eatsense.representation;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import net.eatsense.domain.Document;
import net.eatsense.domain.embedded.DocumentStatus;


public class DocumentDTO {
	
	private Long id;
	@NotNull
	private String name;
	@NotNull
	private String type;
	@NotNull
	private String entity;
	private DocumentStatus status;
	private Date createDate;
	private String url;
	private List<Long> ids;
	
	public DocumentDTO() {
	}
	
	public DocumentDTO(Document doc) {
		if(doc != null) {
			id = doc.getId();
			name = doc.getName();
			type = doc.getType();
			entity = doc.getEntity();
			status = doc.getStatus();
			url = doc.getUrl().toString();
			//don't set ids since the are only relevant upon creation of a new document
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}
	
}
