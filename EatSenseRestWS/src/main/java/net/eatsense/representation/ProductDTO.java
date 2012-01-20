package net.eatsense.representation;

import java.util.Collection;

public class ProductDTO {
	private String name;
	private String shortDesc;
	/**
	 * Detailed description of this product.
	 */
	private String longDesc;
	private Float price;
	
	private Collection<ChoiceDTO> choices;
	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getShortDesc() {
		return shortDesc;
	}


	public void setShortDesc(String shortDesc) {
		this.shortDesc = shortDesc;
	}


	public String getLongDesc() {
		return longDesc;
	}


	public void setLongDesc(String longDesc) {
		this.longDesc = longDesc;
	}


	public Float getPrice() {
		return price;
	}


	public void setPrice(Float price) {
		this.price = price;
	}


	public Collection<ChoiceDTO> getChoices() {
		return choices;
	}


	public void setChoices(Collection<ChoiceDTO> choices) {
		this.choices = choices;
	}
}
