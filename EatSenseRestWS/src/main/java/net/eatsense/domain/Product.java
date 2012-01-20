package net.eatsense.domain;

import java.util.List;

import javax.persistence.Transient;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

public class Product extends GenericEntity {
	private String name;
	private String shortDesc;
	/**
	 * Detailed description of this product.
	 */
	private String longDesc;
	private Float price;
	
	/**
	 * The menu listing this product belongs to.
	 */
	@Parent
	private Key<Menu> menu;
	
	@Unindexed
	private List<Key<Choice>> choices;
	
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


	public Key<Menu> getMenu() {
		return menu;
	}


	public void setMenu(Key<Menu> menu) {
		this.menu = menu;
	}


	@Transient
	public Key<Product> getKey() {
		return new Key<Product>(getMenu(), Product.class, super.getId());
	}


	public List<Key<Choice>> getChoices() {
		return choices;
	}


	public void setChoices(List<Key<Choice>> choices) {
		this.choices = choices;
	}
	
}
