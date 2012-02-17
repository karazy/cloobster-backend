package net.eatsense.domain;

import java.util.List;

import javax.persistence.Embedded;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

/**
 * Represents a multiple or single choice attached to an order, like toppings for a salad or sauce for a burger.
 * 
 * @author Nils Weiher
 *
 */
public class OrderChoice extends GenericEntity {
	
	@Transient
	public Key<OrderChoice> getKey() {
		return new Key<OrderChoice>(getOrder(), OrderChoice.class, super.getId());
	}
	
	@Parent
	@NotNull
	Key<Order> order;
	
	@Embedded
	@NotNull
	@Valid
	Choice originalChoice;
		
	public Key<Order> getOrder() {
		return order;
	}

	public void setOrder(Key<Order> order) {
		this.order = order;
	}

	public Choice getOriginalChoice() {
		return originalChoice;
	}

	public void setOriginalChoice(Choice originalChoice) {
		this.originalChoice = originalChoice;
	}

	public List<ProductOption> getSelectedOptions() {
		return selectedOptions;
	}

	public void setSelectedOptions(List<ProductOption> selectedOptions) {
		this.selectedOptions = selectedOptions;
	}

	/**
	 * Options the customer selected for this product order.
	 */
	@Unindexed
	@Embedded
	@Valid
	List<ProductOption> selectedOptions;
	
}
