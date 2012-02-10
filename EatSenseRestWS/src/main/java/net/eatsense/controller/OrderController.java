package net.eatsense.controller;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.Product;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.OrderRepository;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;

public class OrderController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private OrderRepository orderRepo;
	private ChoiceRepository choiceRepo;
	
	@Inject
	private Validator validator;
	
	
	@Inject
	public OrderController(OrderRepository orderRepo,
			ChoiceRepository choiceRepo) {
		super();
		this.orderRepo = orderRepo;
		this.choiceRepo = choiceRepo;
	}
	
	public void setValidator(Validator validator) {
        this.validator = validator;
    }


	public Key<Order> placeOrder(Key<CheckIn> checkIn, Key<Product> product, int amount, List<OrderChoice> choices) {
		Key<Order> orderKey = null;
		Order order = new Order();
		order.setAmount(amount);
		order.setCheckIn(checkIn);
		order.setProduct(product);
		order.setTimeOfPlacement(new Date());
		
		Set<ConstraintViolation<Order>> violations = validator.validate(order);
		
		if(violations.isEmpty()) {
			orderKey = orderRepo.saveOrUpdate(order);
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Order validation failed:\n");
			//build an error message containing all violation messages
			for (ConstraintViolation<Order> violation : violations) {
				sb.append(violation.getPropertyPath()).append(" ").append(violation.getMessage()).append("\n");
			}
			String message = sb.toString();
			logger.error(message);
			throw new IllegalArgumentException(message);
		}
		if(orderKey == null ) {
			logger.error("Error saving order for product: " +product.toString());
			
		}
		for (OrderChoice orderChoice : choices) {
			orderChoice.setOrder(orderKey);
		}
			
		
		
		return orderKey;
	}
}
