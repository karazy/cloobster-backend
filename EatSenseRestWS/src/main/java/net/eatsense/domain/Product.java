package net.eatsense.domain;

import java.util.List;

import javax.persistence.Transient;

import com.google.common.base.Objects;
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
	private Integer order;
	
	/**
	 * The menu listing this product belongs to.
	 */
	private Key<Menu> menu;
	
	@Parent
	private Key<Business> business;
	
	@Unindexed
	private List<Key<Choice>> choices;
	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		if(!Objects.equal(this.name, name)) {
			this.setDirty(true);
			this.name = name;
		}
	}


	public String getShortDesc() {
		return shortDesc;
	}


	public void setShortDesc(String shortDesc) {
		if(!Objects.equal(this.shortDesc, shortDesc)) {
			this.setDirty(true);
			this.shortDesc = shortDesc;
		}
	}


	public String getLongDesc() {
		return longDesc;
	}


	public void setLongDesc(String longDesc) {
		if(!Objects.equal(this.longDesc, longDesc)) {
			this.setDirty(true);
			this.longDesc = longDesc;
		}
	}


	public Float getPrice() {
		return price;
	}


	public void setPrice(Float price) {
		if(!Objects.equal(this.price, price)) {
			this.setDirty(true);
			this.price = price;
		}
	}


	public Key<Menu> getMenu() {
		return menu;
	}


	public void setMenu(Key<Menu> menu) {
		if(!Objects.equal(this.menu, menu)) {
			this.setDirty(true);
			this.menu = menu;
		}
	}


	@Transient
	public Key<Product> getKey() {
		return getKey(getBusiness(), super.getId());
	}


	public List<Key<Choice>> getChoices() {
		return choices;
	}


	public void setChoices(List<Key<Choice>> choices) {
		if(!Objects.equal(this.choices, choices)) {
			this.setDirty(true);
			this.choices = choices;
		}
	}


	public Key<Business> getBusiness() {
		return business;
	}


	public void setBusiness(Key<Business> business) {
		this.business = business;
	}
	
	public static Key<Product> getKey(Key<Business> parentKey, long id) {
		return new Key<Product>(parentKey, Product.class, id);
	}


	public Integer getOrder() {
		return order;
	}


	public void setOrder(Integer order) {
		if(!Objects.equal(this.order, order)) {
			this.setDirty(true);
			this.order = order;
		}
	}
	
}
