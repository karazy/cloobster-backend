package net.eatsense.representation;

import javax.validation.constraints.NotNull;

import org.apache.bval.constraints.NotEmpty;

import net.eatsense.domain.Business;

public class BusinessDTO {
	@NotNull
	@NotEmpty
	String name;
	@NotNull
	@NotEmpty
	String description;
	Long id;

	@NotNull
	@NotEmpty
	private String currency;
	
	public BusinessDTO() {
		super();
	}
	
	public BusinessDTO(Business business) {
		if(business == null)
			return;
		this.name = business.getName();
		this.description = business.getDescription();
		this.currency = business.getCurrency();
		this.id = business.getId();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
}