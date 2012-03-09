package net.eatsense.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.CheckInStatus;
import net.eatsense.domain.Choice;
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.OrderStatus;
import net.eatsense.domain.Product;
import net.eatsense.domain.ProductOption;
import net.eatsense.domain.Restaurant;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.representation.ChoiceDTO;
import net.eatsense.representation.OrderDTO;
import net.eatsense.representation.Transformer;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Query;
import com.sun.jersey.api.NotFoundException;

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

	private Transformer transform;
	
	
	@Inject
	public OrderController(OrderRepository orderRepo,
			OrderChoiceRepository orderChoiceRepo, ProductRepository productRepo, RestaurantRepository restaurantRepo, CheckInRepository checkInRepo, ChoiceRepository choiceRepo, Transformer trans) {
		super();
		this.orderRepo = orderRepo;
		this.productRepo = productRepo;
		this.orderChoiceRepo = orderChoiceRepo;
		this.choiceRepo = choiceRepo;
		this.checkInRepo = checkInRepo;
		this.restaurantRepo = restaurantRepo;
		this.transform = trans;
	}
	
	public void setValidator(Validator validator) {
        this.validator = validator;
    }
	

	public void deleteOrder(Long restaurantId, Long orderId) {
		orderRepo.ofy().delete(new Key<Order>(new Key<Restaurant>(Restaurant.class, restaurantId), Order.class, orderId));
	}
	
	public OrderDTO updateOrder(Long restaurantId, Long orderId, OrderDTO orderData) {
		Order order = getOrder(restaurantId, orderId);
		
		
		order.setStatus(orderData.getStatus());
		order.setComment(orderData.getComment());
		order.setAmount(orderData.getAmount());
		Set<ConstraintViolation<Order>> violations = validator.validate(order);
		if(violations.isEmpty()) {
			// save order
			if( orderRepo.saveOrUpdate(order) == null )
				throw new RuntimeException("order could not be updated, id: " + orderId);
		}
		else {
			// build validation error messages
			String message = "";
			for (ConstraintViolation<Order> constraintViolation : violations) {
				message += constraintViolation.getPropertyPath() + " " + constraintViolation.getMessage() + "\n";
				
			}
			throw new RuntimeException("Validation errors:\n"+message);
		}		
		
		return transform.orderToDto( order );
	}
	
	public OrderDTO getOrderAsDTO(Long restaurantId, Long orderId) {
		Order order = getOrder(restaurantId, orderId);
				
		return transform.orderToDto( order );
	}

	public Order getOrder(Long restaurantId, Long orderId) {
		// Check if the restaurant exists.
		Restaurant restaurant = restaurantRepo.findByKey(restaurantId);
		if(restaurant == null) {
			logger.error("Order cannot be retrieved, restaurant id unknown: " + restaurantId);
			throw new NotFoundException("Order cannot be retrieved, restaurant id unknown: " + restaurantId);
		}
		
		Order order = orderRepo.getById(restaurant.getKey(), orderId);
		if( order == null) {
			logger.error("Order cannot be retrieved, order id unknown: " + orderId);
			throw new NotFoundException("Order cannot be retrieved, order id unknown: " + orderId);
		}
		return order;
	}
	
	public Collection<OrderDTO> getOrdersAsDTO(Long restaurantId, String checkInId, String status) {
		return transform.ordersToDto(getOrders( restaurantId, checkInId, status));
	}
	
	/**
	 * Get orders saved for the given checkin and filter by status if set.
	 * 
	 * @param restaurantId
	 * @param checkInId
	 * @param status 
	 * @return
	 */
	public List<Order> getOrders(Long restaurantId, String checkInId, String status) {
		// Check if the restaurant exists.
		Restaurant restaurant = restaurantRepo.findByKey(restaurantId);
		if(restaurant == null) {
			logger.error("Order cannot be retrieved, restaurant id unknown: " + restaurantId);
			throw new NotFoundException("Orders cannot be retrieved, restaurant id unknown: " + restaurantId);
		}
		
		CheckIn checkIn = checkInRepo.getByProperty("userId", checkInId);
		if(checkIn == null) {
			logger.error("Orders cannot be retrieved, checkin not found.");
			throw new NotFoundException("Orders cannot be retrieved, checkin not found.");
		}
		
		
		Query<Order> query = orderRepo.getOfy().query(Order.class).ancestor(restaurant).filter("checkIn", checkIn.getKey());
		if(status != null && !status.isEmpty()) {
			query = query.filter("status", status.toUpperCase());
		}
		List<Order> orders = query.list();
		
		return orders;
	}
	
	public Long placeOrder(Long restaurantId, String checkInId, OrderDTO order) {
		Long orderId = null;
		
		CheckIn checkIn = checkInRepo.getByProperty("userId", checkInId);
		if(checkIn == null) {
			logger.error("Order cannot be placed, checkin not found!");
			return null;
		}
		if(checkIn.getStatus() != CheckInStatus.CHECKEDIN) {
			logger.error("Order cannot be placed, payment already requested or not checked in");
			return null;
		}
		
		if( order.getStatus() != OrderStatus.CART ) {
			logger.error("Order cannot be placed, unexpected order status: "+order.getStatus());
			return null;
		}
		// Check if the restaurant exists.
		Restaurant restaurant = restaurantRepo.findByKey(restaurantId);
		if(restaurant == null) {
			logger.error("Order cannot be placed, restaurant id unknown" + restaurantId);
			return null;
		}
		if(restaurant.getId() != checkIn.getRestaurant().getId()) {
			logger.error("Order cannot be placed, checkin is not at the same restaurant to which the order was sent: id="+checkIn.getRestaurant().getId());
			return null;
		}
		
		// Check if the product to be ordered exists
		Product product = productRepo.getById(checkIn.getRestaurant(), order.getProduct().getId());
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
				int selected = 0;
				Choice originalChoice = choiceRepo.getById(checkIn.getRestaurant(), choiceDto.getId());
				
				if(originalChoice == null) {
					logger.error("Order cannot be placed, unknown choice id="+choiceDto.getId());
					return null;
				}
				if(choiceDto.getOptions() != null ) {
					
					for (ProductOption productOption : choiceDto.getOptions()) {
						if(productOption.getSelected() != null && productOption.getSelected())
							selected++;
					}
					// Validate choice selection
					if(selected < originalChoice.getMinOccurence() ) {
						logger.error("Order cannot be placed, minOccurence of "+ originalChoice.getMinOccurence() + " not satisfied. selected="+ selected);
						return null;
					}
					
					if(originalChoice.getMaxOccurence() > 0 && selected > originalChoice.getMaxOccurence() ) {
						logger.error("Order cannot be placed, maxOccurence of "+ originalChoice.getMaxOccurence() + " not satisfied. selected="+ selected);
						return null;
					}

					originalChoice.setOptions(new ArrayList<ProductOption>(choiceDto.getOptions()));
				}
				
				choice.setChoice(originalChoice);
				
					
				choices.add(choice);				
			}
		}
		

		Key<Order> orderKey = createOrder(restaurant.getKey(), checkIn.getKey(), productKey, order.getAmount(), choices, order.getComment());		
		if(orderKey != null) {
			// order successfully saved
			orderId = orderKey.getId();
			checkIn.setStatus(CheckInStatus.ORDER_PLACED);
			checkInRepo.saveOrUpdate(checkIn);
		}
		
		
		return orderId;
	}

	

	public Key<Order> createOrder(Key<Restaurant> restaurant, Key<CheckIn> checkIn, Key<Product> product, int amount, List<OrderChoice> choices, String comment) {
		Key<Order> orderKey = null;
		Order order = new Order();
		order.setAmount(amount);
		order.setRestaurant(restaurant);
		order.setCheckIn(checkIn);
		order.setComment(comment);
		order.setStatus(OrderStatus.CART);
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
