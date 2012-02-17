package net.eatsense.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Choice;
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.Product;
import net.eatsense.domain.ProductOption;
import net.eatsense.domain.Restaurant;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.GenericRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.representation.ChoiceDTO;
import net.eatsense.representation.OrderDTO;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;

public class OrderController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private OrderRepository orderRepo;
	private ProductRepository productRepo;
	private OrderChoiceRepository orderChoiceRepo;
	
	@Inject
	private Validator validator;

	private CheckInRepository checkInRepo;

	private RestaurantRepository restaurantRepo;

	private ChoiceRepository choiceRepo;
	
	
	@Inject
	public OrderController(OrderRepository orderRepo,
			OrderChoiceRepository orderChoiceRepo, ProductRepository productRepo, RestaurantRepository restaurantRepo, CheckInRepository checkInRepo, ChoiceRepository choiceRepo) {
		super();
		this.orderRepo = orderRepo;
		this.productRepo = productRepo;
		this.orderChoiceRepo = orderChoiceRepo;
		this.choiceRepo = choiceRepo;
		this.checkInRepo = checkInRepo;
		this.restaurantRepo = restaurantRepo;
	}
	
	public void setValidator(Validator validator) {
        this.validator = validator;
    }
	
	public Long placeOrder(Long restaurantId, String checkInId, OrderDTO order) {
		Long orderId = null;
		
		CheckIn checkIn = checkInRepo.getByProperty("userId", checkInId);
		if(checkIn == null) {
			logger.error("Order cannot be placed, checkin not found!");
			return null;
		}
		// Check if the restaurant exists.
		Restaurant restaurant = restaurantRepo.findByKey(restaurantId);
		if(restaurant == null) {
			logger.error("Order cannot be placed, restaurant id unknown" + restaurantId);
		}
		if(restaurant.getId() != checkIn.getRestaurant().getId()) {
			logger.error("Order cannot be placed, checkin is not at the same restaurant to which the order was sent: id="+checkIn.getRestaurant().getId());
			return null;
		}
		
		// Check if the product to be ordered exists
		Product product = productRepo.getByKey(checkIn.getRestaurant(), order.getProduct().getId());
		if(product == null) {
			logger.error("Order cannot be placed, productId unknown: "+ order.getProduct().getId());
			return null;
		}
		Key<Product> productKey = product.getKey();
		
		List<OrderChoice> choices = null;
		if(order.getProduct().getChoices() != null) {
			choices = new ArrayList<OrderChoice>();
			
			for (ChoiceDTO choiceDto : order.getProduct().getChoices()) {
				OrderChoice choice = new OrderChoice();
				
				if(choiceDto.getOptions() != null ) {
					List<ProductOption> selected = new ArrayList<ProductOption>();
					for (ProductOption productOption : choiceDto.getOptions()) {
						if(productOption.getSelected() != null && productOption.getSelected())
							selected.add(productOption);
					}
					choice.setSelectedOptions(selected);
				}
				Choice originalChoice = choiceRepo.getByKey(checkIn.getRestaurant(), choiceDto.getId());
				if(originalChoice == null) {
					logger.error("Order cannot be placed, unknown choice id="+choiceDto.getId());
					return null;
				}
				choice.setOriginalChoice(originalChoice);
				
				// Validate choice selection
				if(choice.getSelectedOptions().size() < originalChoice.getMinOccurence() ) {
					logger.error("Order cannot be placed, minOccurence of "+ originalChoice.getMinOccurence() + " not satisfied. selected="+choice.getSelectedOptions().size());
					return null;
				}
				
				if(choice.getSelectedOptions().size() > originalChoice.getMaxOccurence() ) {
					logger.error("Order cannot be placed, maxOccurence of "+ originalChoice.getMaxOccurence() + " not satisfied. selected="+choice.getSelectedOptions().size());
					return null;
				}
					
				choices.add(choice);				
			}
		}
		

		Key<Order> orderKey = saveOrder(restaurant.getKey(), checkIn.getKey(), productKey, order.getAmount(), choices, order.getComment());		
		if(orderKey != null) {
			// order successfully saved
			orderId = orderKey.getId();
		}
		
		return orderId;
	}
	
	public OrderDTO getOrder(Long restaurantId, Long orderId) {
		OrderDTO order = new OrderDTO();
		
		//TODO extract order data from datastore
		
		return order;
	}
	
	public Key<Order> saveOrder(Key<Restaurant> restaurant, Key<CheckIn> checkIn, Key<Product> product, int amount, List<OrderChoice> choices, String comment) {
		Key<Order> orderKey = null;
		Order order = new Order();
		order.setAmount(amount);
		order.setRestaurant(restaurant);
		order.setCheckIn(checkIn);
		order.setComment(comment);
		order.setProduct(product);
		order.setOrderTime(new Date());
		
		// validate order object
		Set<ConstraintViolation<Order>> violations = validator.validate(order);
		
		if(violations.isEmpty()) {
			orderKey = orderRepo.saveOrUpdate(order);
		}
		else { /// handle validation errors ...
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
			return null;
		}
		
		if(choices != null) {
			for (OrderChoice orderChoice : choices) {
				// set parent key
				orderChoice.setOrder(orderKey);
				
				// validate choices
				Set<ConstraintViolation<OrderChoice>> choiceViolations = validator.validate(orderChoice);
				
				if(choiceViolations.isEmpty()) {
					if ( orderChoiceRepo.saveOrUpdate(orderChoice) == null ) {
						logger.error("Error saving choices for order: " +orderKey.toString());
						return null;
					}
				}
				else { // handle validation errors ...
					StringBuilder sb = new StringBuilder();
					sb.append("Choice validation failed:\n");
					
					//build an error message containing all violation messages
					for (ConstraintViolation<OrderChoice> violation : choiceViolations) {
						sb.append(violation.getPropertyPath()).append(" ").append(violation.getMessage()).append("\n");
					}
					String message = sb.toString();
					logger.error(message);
					throw new IllegalArgumentException(message);
				}	
			}
		}
			
		
		
		return orderKey;
	}
}
