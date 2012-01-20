package net.eatsense.domain;

import java.util.List;

import javax.persistence.Embedded;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

/**
 * Represents a multiple or single choice attached to a product, like toppings for a salad or sauce for a burger.
 * 
 * @author Nils Weiher
 *
 */
public class Choice extends GenericEntity {
	
	/**
	 * Description of the choice to show on the product page.
	 */
	@Unindexed
	String text;
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public ChoiceOverridePrice getOverridePrice() {
		return overridePrice;
	}

	public void setOverridePrice(ChoiceOverridePrice overridePrice) {
		this.overridePrice = overridePrice;
	}

	public Key<Product> getProduct() {
		return product;
	}

	public void setProduct(Key<Product> product) {
		this.product = product;
	}

	public int getMaxOccurence() {
		return maxOccurence;
	}

	public void setMaxOccurence(int maxOccurence) {
		this.maxOccurence = maxOccurence;
	}

	public int getMinOccurence() {
		return minOccurence;
	}

	public void setMinOccurence(int minOccurence) {
		this.minOccurence = minOccurence;
	}

	public List<ProductOption> getAvailableChoices() {
		return availableChoices;
	}

	public void setAvailableChoices(List<ProductOption> availableChoices) {
		this.availableChoices = availableChoices;
	}

	public List<Key<Product>> getAvailableProducts() {
		return availableProducts;
	}

	public void setAvailableProducts(List<Key<Product>> availableProducts) {
		this.availableProducts = availableProducts;
	}

	public int getIncludedChoices() {
		return includedChoices;
	}

	public void setIncludedChoices(int includedChoices) {
		this.includedChoices = includedChoices;
	}

	/**
	 * Only used if overridePrice is different to NONE.
	 */
	float price;
	
	/**
	 * Determines how to calculate the price of the options.
	 */
	ChoiceOverridePrice overridePrice = ChoiceOverridePrice.NONE;
	
	@Parent
	Key<Product> product;
		
	/**
	 * If different from 0, the maximum number of options the customer can select.
	 */
	int maxOccurence;
	
	/**
	 * If different from 0, the minimum number of options the customer should select.
	 */
	int minOccurence;
	
	/**
	 * Contains available options for the customer to order with the product.
	 * ONLY this list OR availableProducts are set.
	 */
	@Unindexed
	@Embedded
	List<ProductOption> availableChoices;
	
	/**
	 * Contains additional product options to order with the original product to order.
	 * ONLY this list OR avaibleChoices are set.
	 */
	@Unindexed
	List<Key<Product>> availableProducts;
	
	/**
	 * Number of options that are free with choice. 
	 */
	int includedChoices;
}
