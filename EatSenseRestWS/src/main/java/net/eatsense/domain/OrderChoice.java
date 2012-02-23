package net.eatsense.domain;

import javax.persistence.Embedded;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;

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
	Choice choice;
		
	public Key<Order> getOrder() {
		return order;
	}

	public void setOrder(Key<Order> order) {
		this.order = order;
	}

	public Choice getChoice() {
		return choice;
	}

	public void setChoice(Choice originalChoice) {
		this.choice = originalChoice;
	}
}
