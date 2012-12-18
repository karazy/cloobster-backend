package net.eatsense.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import com.google.common.base.Objects;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Parent;

@Cached
public class Product extends GenericEntity<Product> {
	private String name;
	private String shortDesc;
	/**
	 * Detailed description of this product.
	 */
	private String longDesc;
	
	
	/**
	 * Saved as currency minor amount (cent)
	 */
	private long price;
	private Integer order;
	
	/**
	 * The menu listing this product belongs to.
	 */
	private Key<Menu> menu;
	
	@Parent
	private Key<Location> business;
	
	private List<Key<Choice>> choices;
	
	private boolean active = false;
	
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


	public long getPrice() {
		return price;
	}


	public void setPrice(long price) {
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


	public Key<Location> getBusiness() {
		return business;
	}


	public void setBusiness(Key<Location> business) {
		this.business = business;
	}
	
	public static Key<Product> getKey(Key<Location> parentKey, long id) {
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
	
	/**
	 * Add a Key for a Choice entity to the list of choices.
	 * 
	 * @param choice
	 * @return true
	 */
	public boolean addChoice(Key<Choice> choice) {
		checkNotNull(choice, "choice key was null");
		
		if(choices == null)
			choices = new ArrayList<Key<Choice>>();
		boolean result = false;
		
		if(!choices.contains(choice)) {
			result = choices.add(choice);
			if(result) {
				this.setDirty(true);
			}
		}
		
		return result;
	}


	public boolean isActive() {
		return active;
	}


	public void setActive(boolean active) {
		if(!Objects.equal(this.active, active)) {
			this.setDirty(true);
			this.active = active;
		}
	}
}
