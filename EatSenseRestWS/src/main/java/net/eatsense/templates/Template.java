package net.eatsense.templates;

import java.util.List;

import javax.persistence.Id;

public class Template {
	@Id
	String id;
	String templateText;
	String description;

	List<String> substitutionDesc;
	
	public Template() {
		super();
	}

	/**
	 * @param id Unique identifier of this Template. 
	 */
	public Template(String id) {
		this();
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTemplateText() {
		return templateText;
	}


	public void setTemplateText(String templateText) {
		this.templateText = templateText;
	}


	public List<String> getSubstitutionDesc() {
		return substitutionDesc;
	}

	public void setSubstitutionDesc(List<String> substitutionDesc) {
		this.substitutionDesc = substitutionDesc;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
