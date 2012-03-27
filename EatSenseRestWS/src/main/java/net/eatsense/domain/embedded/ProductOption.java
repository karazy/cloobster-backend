package net.eatsense.domain.embedded;

import javax.validation.constraints.NotNull;

import org.apache.bval.constraints.NotEmpty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductOption {
	@NotNull
	@NotEmpty
	String name;
	
	float price;
	
	Boolean selected;
	
	public Boolean getSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}
	
	public ProductOption(String name, float price) {
		super();
		this.name = name;
		this.price = price;
	}

	public ProductOption() {
		super();
	}
	
}
