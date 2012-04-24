package net.eatsense.domain;

import java.util.List;

import javax.persistence.Embedded;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import net.eatsense.domain.embedded.ChoiceOverridePrice;
import net.eatsense.domain.embedded.ProductOption;

import org.apache.bval.constraints.NotEmpty;

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
	@NotNull
	@NotEmpty
	String text;
	
	/**
	 * Only used if overridePrice is different to NONE.
	 */
	Float price;

	/**
	 * Determines how to calculate the price of the options.
	 */
	ChoiceOverridePrice overridePrice = ChoiceOverridePrice.NONE;

	@Parent
	@NotNull
	Key<Business> business;

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
	List<ProductOption> options;

	/**
	 * Number of options that are free with choice. 
	 */
	int includedChoices;
	
	Key<Choice> parentChoice;

	public Key<Choice> getParentChoice() {
		return parentChoice;
	}

	public void setParentChoice(Key<Choice> parentChoice) {
		this.parentChoice = parentChoice;
	}

	public Key<Business> getBusiness() {
		return business;
	}

	public int getIncludedChoices() {
		return includedChoices;
	}

	@Transient
	public Key<Choice> getKey() {
		return new Key<Choice>(getProduct(), Choice.class, super.getId());
	}

	public int getMaxOccurence() {
		return maxOccurence;
	}

	public int getMinOccurence() {
		return minOccurence;
	}

	public List<ProductOption> getOptions() {
		return options;
	}

	public ChoiceOverridePrice getOverridePrice() {
		return overridePrice;
	}

	public Float getPrice() {
		return price;
	}
	
	public Key<Product> getProduct() {
		return product;
	}

	public String getText() {
		return text;
	}
	
	public void setBusiness(Key<Business> business) {
		this.business = business;
	}
	
	public void setIncludedChoices(int includedChoices) {
		this.includedChoices = includedChoices;
	}
	
	public void setMaxOccurence(int maxOccurence) {
		this.maxOccurence = maxOccurence;
	}

	public void setMinOccurence(int minOccurence) {
		this.minOccurence = minOccurence;
	}

	public void setOptions(List<ProductOption> availableChoices) {
		this.options = availableChoices;
	}
		
	public void setOverridePrice(ChoiceOverridePrice overridePrice) {
		this.overridePrice = overridePrice;
	}
	
	public void setPrice(Float price) {
		this.price = price;
	}
	
	public void setProduct(Key<Product> product) {
		this.product = product;
	}
	
	public void setText(String text) {
		this.text = text;
	}
}
