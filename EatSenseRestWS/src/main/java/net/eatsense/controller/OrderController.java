package net.eatsense.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.CheckInStatus;
import net.eatsense.domain.Choice;
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.OrderStatus;
import net.eatsense.domain.Product;
import net.eatsense.domain.ProductOption;
import net.eatsense.domain.Request;
import net.eatsense.domain.Request.RequestType;
import net.eatsense.domain.Restaurant;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.representation.ChoiceDTO;
import net.eatsense.representation.OrderDTO;
import net.eatsense.representation.Transformer;
import net.eatsense.representation.cockpit.MessageDTO;
import net.eatsense.representation.cockpit.SpotStatusDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Query;
import com.sun.jersey.api.NotFoundException;

public class OrderController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private OrderRepository orderRepo;
	private ProductRepository productRepo;
	private OrderChoiceRepository orderChoiceRepo;
	private Validator validator;
	private CheckInRepository checkInRepo;
	private RestaurantRepository restaurantRepo;
	private ChoiceRepository choiceRepo;
	private Transformer transform;
	private RequestRepository requestRepo;
	private ChannelController channelCtrl;
	
	
	@Inject
	public OrderController(OrderRepository orderRepo,
			OrderChoiceRepository orderChoiceRepo, ProductRepository productRepo, RestaurantRepository restaurantRepo, CheckInRepository checkInRepo, ChoiceRepository choiceRepo, RequestRepository rr,Transformer trans, ChannelController channelCtrl, Validator validator) {
		super();
		this.validator = validator;
		this.channelCtrl = channelCtrl;
		this.requestRepo = rr;
		this.orderRepo = orderRepo;
		this.productRepo = productRepo;
		this.orderChoiceRepo = orderChoiceRepo;
		this.choiceRepo = choiceRepo;
		this.checkInRepo = checkInRepo;
		this.restaurantRepo = restaurantRepo;
		this.transform = trans;
	}

	/**
	 * Delete the specified order from the datastore.
	 * 
	 * @param restaurantId
	 * @param orderId
	 */
	public void deleteOrder(Long restaurantId, Long orderId) {
		orderRepo.ofy().delete(new Key<Order>(new Key<Restaurant>(Restaurant.class, restaurantId), Order.class, orderId));
	}
	
	/**
	 * <p>
	 * Updates a specific order (identified by orderId)<br>
	 * from a given checkIn (identified by checkInId)<br>
	 * of a specific restaurant (identified by restaurantId)<br>
	 * with new data contained in orderData.
	 * </p>
	 * 
	 * @param restaurantId
	 * @param orderId
	 * @param orderData
	 * @param checkInId
	 * @return the updated OrderDTO
	 */
	public OrderDTO updateOrder(Long restaurantId, Long orderId, OrderDTO orderData, String checkInId) {
		Order order = getOrder(restaurantId, orderId);
		
		CheckIn checkIn = checkInRepo.getByProperty("userId", checkInId);
		if(checkIn == null) {
			logger.error("Order cannot be updated, checkin not found!");
			return null;
		}
		if(checkIn.getStatus() != CheckInStatus.CHECKEDIN && checkIn.getStatus() != CheckInStatus.ORDER_PLACED) {
			logger.error("Order cannot be updated, payment already requested or not checked in");
			return null;
		}
		if(order.getStatus() != OrderStatus.CART) {
			logger.error("Order cannot be updated, order already placed or completed.");
			return null;
		}
		
		// update order object from submitted data
		order.setStatus(orderData.getStatus());
		order.setComment(orderData.getComment());
		order.setAmount(orderData.getAmount());
		
		Set<ConstraintViolation<Order>> violations = validator.validate(order);
		
		if(violations.isEmpty()) {
			// save order
			if( orderRepo.saveOrUpdate(order) == null )
				throw new RuntimeException("order could not be updated, id: " + orderId);
			orderData = transform.orderToDto( order );
			// only create a new request if the order status was updated to be placed
			if(order.getStatus() == OrderStatus.PLACED) {
				
				checkIn.setStatus(CheckInStatus.ORDER_PLACED);
				checkInRepo.saveOrUpdate(checkIn);
								
				Request request = new Request();
				request.setCheckIn(checkIn.getKey());
				request.setRestaurant(Restaurant.getKey(restaurantId));
				request.setObjectId(orderId);
				request.setType(RequestType.ORDER);
				request.setReceivedTime(new Date());
				request.setSpot(checkIn.getSpot());
				request.setStatus(CheckInStatus.ORDER_PLACED.toString());

				requestRepo.saveOrUpdate(request);
				
				ArrayList<MessageDTO> messages = new ArrayList<MessageDTO>();
				
				messages.add(new MessageDTO("order", "update", orderData));
				
				Key<Request> oldestRequest = requestRepo.ofy().query(Request.class).filter("spot",checkIn.getSpot()).order("-receivedTime").getKey();
				// If we have an older request in the database ...
				if( oldestRequest == null || oldestRequest.getId() == request.getId() ) {
					// Send message to notify clients over their channel
					
					messages.add(new MessageDTO("checkin", "update", transform.toStatusDto(checkIn)));
					
					SpotStatusDTO spotData = new SpotStatusDTO();
					spotData.setId(checkIn.getSpot().getId());
					spotData.setStatus(request.getStatus());
					messages.add(new MessageDTO("spot","update",spotData));
					
				}
				try {
					channelCtrl.sendMessagesToAllClients(restaurantId, messages);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			// build validation error messages
			String message = "";
			for (ConstraintViolation<Order> constraintViolation : violations) {
				message += constraintViolation.getPropertyPath() + " " + constraintViolation.getMessage() + "\n";
				
			}
			throw new RuntimeException("Validation errors:\n"+message);
		}

		return orderData;
	}
	
	/**
	 * Get the data for a specified order of a restaurant.
	 * 
	 * @param restaurantId
	 * @param orderId
	 * @return
	 */
	public OrderDTO getOrderAsDTO(Long restaurantId, Long orderId) {
		Order order = getOrder(restaurantId, orderId);
				
		return transform.orderToDto( order );
	}

	/**
	 * Get the order entity for a given orderId of a restaurant.
	 * 
	 * @param restaurantId
	 * @param orderId
	 * @return the Order entity, if existing
	 */
	public Order getOrder(Long restaurantId, Long orderId) {
		// Check if the restaurant exists.
		Restaurant restaurant = restaurantRepo.getById(restaurantId);
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
	
	/**
	 * Get all order data 
	 * 
	 * @param restaurantId
	 * @param checkInId
	 * @param status
	 * @return
	 */
	public Collection<OrderDTO> getOrdersAsDto(Long restaurantId, String checkInId, String status) {
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
		Restaurant restaurant = restaurantRepo.getById(restaurantId);
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
	
	public Collection<OrderDTO> getOrdersBySpotAsDto(Long businessId, Long spotId, Long checkInId) {
		return transform.ordersToDto(getOrdersBySpot( businessId, spotId, checkInId));
	}
	
	/**
	 * Return all orders not in the cart for the given spot.
	 * 
	 * @param restaurantId
	 * @param spotId
	 * @param checkInId
	 * @return list of the orders found
	 */
	public List<Order> getOrdersBySpot(Long restaurantId, Long spotId, Long checkInId) {
		// Check if the restaurant exists.
		Restaurant restaurant = restaurantRepo.getById(restaurantId);
		if(restaurant == null) {
			logger.error("Order cannot be retrieved, restaurant id unknown: " + restaurantId);
			throw new NotFoundException("Orders cannot be retrieved, restaurant id unknown: " + restaurantId);
		}
		
		Query<Order> query = orderRepo.getOfy().query(Order.class).ancestor(restaurant).filter("status !=", OrderStatus.CART.toString());
		
		if(checkInId != null) {
			query = query.filter("checkIn", CheckIn.getKey(checkInId));
		}
		
		List<Order> orders = query.list();
		
		Collections.sort(orders, new Comparator<Order>(){
	           public int compare (Order m1, Order m2){
	               return m1.getOrderTime().compareTo(m2.getOrderTime());
	           }
	       });
		
		return orders;
	}
	
	/**
	 * Create a new Order entity with status CART and save in the datastore.
	 * 
	 * @param restaurantId
	 * @param checkInId
	 * @param order
	 * @return id of the order
	 */
	public Long placeOrder(Long restaurantId, String checkInId, OrderDTO order) {
		Long orderId = null;
		
		CheckIn checkIn = checkInRepo.getByProperty("userId", checkInId);
		if(checkIn == null) {
			logger.error("Order cannot be placed, checkin not found!");
			return null;
		}
		if(checkIn.getStatus() != CheckInStatus.CHECKEDIN && checkIn.getStatus() != CheckInStatus.ORDER_PLACED) {
			logger.error("Order cannot be placed, payment already requested or not checked in");
			return null;
		}
		
		if( order.getStatus() != OrderStatus.CART ) {
			logger.error("Order cannot be placed, unexpected order status: "+order.getStatus());
			return null;
		}
		
		// Check if the restaurant exists.
		Restaurant restaurant = restaurantRepo.getById(restaurantId);
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
		

		Key<Order> orderKey = createAndSaveOrder(restaurant.getKey(), checkIn.getKey(), productKey, order.getAmount(), choices, order.getComment());		
		if(orderKey != null) {
			// order successfully saved
			orderId = orderKey.getId();
		}
		
		return orderId;
	}

	

	/**
	 * Create a new Order entity with the given data and save it in the datastore;
	 * 
	 * @param restaurant
	 * @param checkIn
	 * @param product
	 * @param amount
	 * @param choices
	 * @param comment
	 * @return key of the new entity
	 */
	public Key<Order> createAndSaveOrder(Key<Restaurant> restaurant, Key<CheckIn> checkIn, Key<Product> product, int amount, List<OrderChoice> choices, String comment) {
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

	/**
	 * Update method for orders called by the backend.
	 * 
	 * @param businessId
	 * @param orderId
	 * @param orderData new data update
	 * @return updated Data
	 */
	public OrderDTO updateOrderForBusiness(Long businessId, Long orderId, OrderDTO orderData) {
		Order order = getOrder(businessId, orderId);
		if(order == null) {
			logger.error("Order cannot be updated, orderId not found!");
			return null;
		}
	
		CheckIn checkIn = checkInRepo.getByKey(order.getCheckIn());
		// Check that status CANCELLED will not be changed.
		if(OrderStatus.CANCELED.equals(order.getStatus())) {
			logger.error("{} already cancelled, cannot update", order.getKey());
			return null;
		}
		
		// Check that the status CART cannot be changed from this request.
		// ( only consumer can place an order from the cart, a business would directy create it as recieved)
		if(OrderStatus.CART.equals(order.getStatus())) {
			logger.error("{} still in cart, cannot update", order.getKey());
			return null;
		}
		// Check that the request doesnt set the status to CART.
		if(OrderStatus.CART.equals(orderData.getStatus())) {
			logger.error("{} update status to CART not allowed", order.getKey());
			return null;
		}
		// Check if the new status is different from the current status.	
		if(orderData.getStatus().equals(order.getStatus())) {
			logger.info("{} status already, set to {}", order.getStatus());
			return orderData;
		}
		// Check that status RECEIVED will not be overwritten with an earlier status, only with CANCELED
		if(OrderStatus.RECEIVED.equals(order.getStatus()) && !orderData.getStatus().equals(OrderStatus.CANCELED)) {
			logger.error("{} status RECEIVED can only be set to CANCELLED, unable to update", order.getKey());
			return null;
		}
		
			
			
		// update order object from submitted data
		order.setStatus(orderData.getStatus());
		//order.setComment(orderData.getComment());
		//order.setAmount(orderData.getAmount());
		
		Set<ConstraintViolation<Order>> violations = validator.validate(order);
		if(violations.isEmpty()) {
			// save order
			if( orderRepo.saveOrUpdate(order) == null )
				throw new RuntimeException("order could not be updated, id: " + orderId);

			// Get all pending requests sorted by oldest first.
			List<Request> requests = requestRepo.ofy().query(Request.class).filter("spot",checkIn.getSpot()).order("-receivedTime").list();
						
			for (Request request : requests) {
				// delete the request for this order
				if( request.getType() == RequestType.ORDER &&  request.getObjectId().equals(order.getId())) {
					requestRepo.delete(request); 
				}
			}
			ArrayList<MessageDTO> messages = new ArrayList<MessageDTO>();
			
			// Add a message with updated order status to the message package.
			messages.add(new MessageDTO("order","update",orderData));
			
			// If we have an older request in the database ...
			if(requests.size() > 0 && requests.get(0).getType() == RequestType.ORDER && requests.get(0).getObjectId().equals(order.getId()) ) {
				
				
//				requestRepo.delete(requests.get(0));
				String newStatus = null;
				// Save the status of the next request in line, if there is one.
				if(requests.size() > 1 ) {
					newStatus = requests.get(1).getStatus();
				} else if(!requests.get(0).getStatus().equals(OrderStatus.PLACED)) {
					//all pending orders are processed for this spot, 
					newStatus = CheckInStatus.CHECKEDIN.toString();
				}
					
				if(!requests.get(0).getStatus().equals(newStatus)) {
					// Update the status of the checkIn in the datastore.
					checkIn.setStatus(CheckInStatus.valueOf(newStatus));
					checkInRepo.saveOrUpdate(checkIn);
					
					// Add a message with updated checkin status to the package.
					messages.add(new MessageDTO("checkin","update",transform.toStatusDto(checkIn)));
					
					// Add a message with updated spot status to the package.
					SpotStatusDTO spotData = new SpotStatusDTO();
					spotData.setId(checkIn.getSpot().getId());
					spotData.setStatus(newStatus);
					messages.add(new MessageDTO("spot","update",spotData));
				}	
			}
			try {
				channelCtrl.sendMessagesToAllClients(businessId, messages);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		else {
			// build validation error messages
			String message = "";
			for (ConstraintViolation<Order> constraintViolation : violations) {
				message += constraintViolation.getPropertyPath() + " " + constraintViolation.getMessage() + "\n";
				
			}
			throw new RuntimeException("Validation errors:\n"+message);
		}

		return orderData; //transform.orderToDto( order );
	}
}
