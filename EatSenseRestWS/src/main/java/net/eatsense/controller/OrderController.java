package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Choice;
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.Product;
import net.eatsense.domain.Request;
import net.eatsense.domain.Request.RequestType;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.domain.embedded.ProductOption;
import net.eatsense.event.UpdateCartEvent;
import net.eatsense.event.UpdateOrderEvent;
import net.eatsense.exceptions.OrderFailureException;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.representation.OrderCartDTO;
import net.eatsense.representation.ChoiceDTO;
import net.eatsense.representation.OrderDTO;
import net.eatsense.representation.Transformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;

/**
 * Manages order creation, update and retrieval.
 * 
 * @author Nils Weiher
 *
 */
public class OrderController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private OrderRepository orderRepo;
	private ProductRepository productRepo;
	private OrderChoiceRepository orderChoiceRepo;
	private Validator validator;
	private CheckInRepository checkInRepo;
	private ChoiceRepository choiceRepo;
	private Transformer transform;
	private RequestRepository requestRepo;

	private EventBus eventBus;
	
	
	@Inject
	public OrderController(OrderRepository orderRepo,
			OrderChoiceRepository orderChoiceRepo,
			ProductRepository productRepo, BusinessRepository businessRepo,
			CheckInRepository checkInRepo, ChoiceRepository choiceRepo,
			RequestRepository rr, Transformer trans, Validator validator,
			EventBus eventBus) {
		super();
		this.eventBus = eventBus;
		this.validator = validator;
		this.requestRepo = rr;
		this.orderRepo = orderRepo;
		this.productRepo = productRepo;
		this.orderChoiceRepo = orderChoiceRepo;
		this.choiceRepo = choiceRepo;
		this.checkInRepo = checkInRepo;
		this.transform = trans;
	}

	/**
	 * Delete the specified order from the datastore.
	 * 
	 * @param business
	 * @param order
	 * @param checkIn 
	 */
	public void deleteOrder(Business business, Order order, CheckIn checkIn) {
		if(order == null) {
			throw new IllegalArgumentException("Unable to delete order, order is null");
		}
		if(checkIn == null)
			throw new IllegalArgumentException("Unable to delete order, checkin is null");
		if(order.getCheckIn().getId() != checkIn.getId())
			throw new OrderFailureException("Unable to delete order, checkIn does not own the order");
		if(order.getStatus() != OrderStatus.CART) {
			throw new OrderFailureException("Unable to delete order, order already PLACED.");
		}
		orderRepo.ofy().delete(orderRepo.ofy().query(OrderChoice.class).ancestor(order).listKeys());
		orderRepo.ofy().delete(order);
	}
	
	/**
	 * Delete all orders in the cart for the checkin.
	 * 
	 * @param checkIn
	 */
	public void deleteCartOrders(CheckIn checkIn) {
		checkNotNull(checkIn, "checkIn was null");
		checkNotNull(checkIn.getId(), "checkIn id was null");
		checkNotNull(checkIn.getBusiness(), "checkIn business was null");
		Objectify ofy = orderRepo.ofy();

		List<Key<Order>> orderKeys = ofy.query(Order.class).ancestor(checkIn.getBusiness())
				.filter("checkIn", checkIn.getKey())
				.filter("status", OrderStatus.CART.toString()).listKeys();
		
		List<Key<?>> keysToDelete = new ArrayList<Key<?>>();
		for (Key<Order> orderKey : orderKeys) {
			keysToDelete.addAll(orderChoiceRepo.query().ancestor(orderKey).listKeys());
		}
		
		keysToDelete.addAll(orderKeys);
		
		ofy.delete(keysToDelete);
	}
	
	/**
	 * @param checkIn
	 */
	public void updateCartOrdersToPlaced(CheckIn checkIn) {
		checkNotNull(checkIn, "checkIn was null");
		checkNotNull(checkIn.getId(), "checkIn id was null");
		checkNotNull(checkIn.getBusiness(), "checkIn business was null");
		checkArgument(checkIn.getStatus() == CheckInStatus.CHECKEDIN ||
				checkIn.getStatus() == CheckInStatus.ORDER_PLACED, "" );
		
		List<Order> orders = orderRepo.query().ancestor(checkIn.getBusiness())
				.filter("checkIn", checkIn.getKey())
				.filter("status", OrderStatus.CART.toString()).list();
		
		if(orders.isEmpty()) {
			logger.info("No orders in cart, returning.");
			return;
		}
		
		Key<Request> oldestRequest = requestRepo.query().filter("spot",checkIn.getSpot()).order("-receivedTime").getKey();
		// If we have no older request in the database ...
		UpdateCartEvent updateEvent = new UpdateCartEvent(checkIn);
		if( oldestRequest == null ) {
			updateEvent.setNewSpotStatus(OrderStatus.PLACED.toString());
		}
		
		List<Request> requests = new ArrayList<Request>();
		for (Order order : orders) {
			order.setStatus(OrderStatus.PLACED);
			
			Request request = new Request();
			request.setCheckIn(checkIn.getKey());
			request.setBusiness(checkIn.getBusiness());
			request.setObjectId(order.getId());
			request.setType(RequestType.ORDER);
			request.setReceivedTime(new Date());
			request.setSpot(checkIn.getSpot());
			request.setStatus(CheckInStatus.ORDER_PLACED.toString());
			
			requests.add(request);
		}
		orderRepo.saveOrUpdate(orders);
		requestRepo.saveOrUpdate(requests);
	
		// Update the checkin status, if there is none set.
		if(checkIn.getStatus() == CheckInStatus.CHECKEDIN) {
			checkIn.setStatus(CheckInStatus.ORDER_PLACED);
			checkInRepo.saveOrUpdate(checkIn);
			
			updateEvent.setNewCheckInStatus(CheckInStatus.ORDER_PLACED.toString());
		}
		
		eventBus.post(updateEvent);
	}
	
	/**
	 * <p>
	 * Updates a specific order (identified by orderId)<br>
	 * from a given checkIn (identified by checkInId)<br>
	 * of a specific business (identified by businessId)<br>
	 * with new data contained in orderData.
	 * </p>
	 * 
	 * @param business
	 * @param order
	 * @param orderData
	 * @param checkIn
	 * @return the updated OrderDTO
	 */
	public OrderDTO updateOrder(Business business, Order order, OrderDTO orderData, CheckIn checkIn) {
		//
		// Check preconditions and retrieve entities.
		//
		if(business == null) {
			throw new IllegalArgumentException("Order cannot be updated, business is null");
		}
		if(checkIn == null) {
			throw new IllegalArgumentException("Order cannot be updated, checkin is null");
		}
		if(order == null) {
			throw new IllegalArgumentException("Order cannot be updated, order is null.");
		}
		// Check if the order belongs to the specified checkin.
		if(! checkIn.getId().equals(order.getCheckIn().getId()) ) {
			throw new IllegalArgumentException("Order cannot be updated, checkIn does not own the order.");
		}
		// Check that the checkin is allowed to update the order.
		if(checkIn.getStatus() != CheckInStatus.CHECKEDIN && checkIn.getStatus() != CheckInStatus.ORDER_PLACED) {
			throw new OrderFailureException("Order cannot be updated, payment already requested or not checked in");
		}
		// Check that we only save allowed status updates.
		if(order.getStatus() == OrderStatus.PLACED || !order.getStatus().isTransitionAllowed(orderData.getStatus())) {
			throw new OrderFailureException("Order cannot be updated, already PLACED");
		}
		// save the previous status for reference
		OrderStatus oldStatus = order.getStatus();
		// update order object from submitted data
		order.setStatus(orderData.getStatus());
		order.setComment(orderData.getComment());
		order.setAmount(orderData.getAmount());
		
		orderData.setCheckInId(checkIn.getId());
		// Retrieve saved choices from the store
		List<OrderChoice> savedChoices = orderChoiceRepo.getByParent(order.getKey());
		if(orderData.getProduct().getChoices() != null ) {
			// iterate over all choices ... 
			for( ChoiceDTO choiceData : orderData.getProduct().getChoices()) {
				for( OrderChoice savedChoice : savedChoices ) {
					// ... and compare ids.
					if(choiceData.getId().equals(savedChoice.getChoice().getId())) {
						// Save all made choices for the option.
						HashSet<ProductOption> optionSet = new HashSet<ProductOption>(savedChoice.getChoice().getOptions());
						// Check if any of the options were changed
						if ( ! optionSet.containsAll(choiceData.getOptions()) ) {
							logger.info("Saving updated options for choice: {}", choiceData.getText() );
							// Remove old choices.
							savedChoice.getChoice().getOptions().clear();
							// Add the new choices to the data.
							savedChoice.getChoice().getOptions().addAll(choiceData.getOptions());
							
							orderChoiceRepo.saveOrUpdate(savedChoice);
						}
					}
				}
			}
		}
		
		//Validate the order entity.		
		Set<ConstraintViolation<Order>> violations = validator.validate(order);
		//Check that we have no violations.
		if(violations.isEmpty()) {
			// save order
			orderRepo.saveOrUpdate(order);
			
			// only create a new request if the order status was updated
			if(order.getStatus() != oldStatus) {
				UpdateOrderEvent updateEvent = new UpdateOrderEvent(business, order, checkIn);
				updateEvent.setOrderData(orderData);
				// Update the checkin status, if there is none set.
				if(checkIn.getStatus() == CheckInStatus.CHECKEDIN) {
					checkIn.setStatus(CheckInStatus.ORDER_PLACED);
					checkInRepo.saveOrUpdate(checkIn);
					
					updateEvent.setNewCheckInStatus(CheckInStatus.ORDER_PLACED.toString());
				}
				
				Request request = new Request();
				request.setCheckIn(checkIn.getKey());
				request.setBusiness(business.getKey());
				request.setObjectId(order.getId());
				request.setType(RequestType.ORDER);
				request.setReceivedTime(new Date());
				request.setSpot(checkIn.getSpot());
				request.setStatus(CheckInStatus.ORDER_PLACED.toString());
				requestRepo.saveOrUpdate(request);
				
				Key<Request> oldestRequest = requestRepo.ofy().query(Request.class).filter("spot",checkIn.getSpot()).order("-receivedTime").getKey();
				// If we have no older request in the database or the new request is the oldest...
				if( oldestRequest == null || oldestRequest.getId() == request.getId() ) {
					updateEvent.setNewSpotStatus(request.getStatus());
				}

				eventBus.post(updateEvent);
			}
		}
		else {
			// build validation error messages
			String message = "Validation errors:\n";
			for (ConstraintViolation<Order> constraintViolation : violations) {
				message += constraintViolation.getPropertyPath() + " " + constraintViolation.getMessage() + "\n";
				
			}
			throw new OrderFailureException(message);
		}

		return orderData;
	}
	
	/**
	 * Get the data for a specified order of a business.
	 * 
	 * @param business
	 * @param orderId
	 * @return
	 */
	public OrderDTO getOrderAsDTO(Business business, Long orderId) {
		return transform.orderToDto( getOrder(business, orderId) );
	}
	
	/**
	 * Create data transfer object from order entity.
	 * 
	 * @param order
	 * @return order data transfer object
	 */
	public OrderDTO toDto( Order order ) {
		return transform.orderToDto( order );
	}

	/**
	 * Get the order entity for a given orderId of a business.
	 * 
	 * @param business
	 * @param orderId
	 * @return the Order entity, if existing
	 */
	public Order getOrder(Business business, Long orderId) {
		if(business == null) {
			logger.error("Unable to get order, business is null (orderId={})", orderId);
			return null;
		}
			
		try {
			return orderRepo.getById(business.getKey(), orderId);
		} catch (com.googlecode.objectify.NotFoundException e) {
			logger.error("Unable to get order, order or business id unknown", e);
			return null;
		}
	}
	
	/**
	 * Get all order data 
	 * 
	 * @param business
	 * @param checkIn
	 * @param status
	 * @return
	 */
	public Collection<OrderDTO> getOrdersAsDto(Business business, CheckIn checkIn, String status) {
		return transform.ordersToDto(getOrders( business, checkIn, status));
	}
	
	/**
	 * Get orders saved for the given checkin and filter by status if set.
	 * 
	 * @param business
	 * @param checkIn
	 * @param status 
	 * @return
	 */
	public List<Order> getOrders(Business business, CheckIn checkIn, String status) {
		Query<Order> query = orderRepo.getOfy().query(Order.class).ancestor(business);
		if(checkIn != null) {
			// Filter by checkin if set.
			query = query.filter("checkIn", checkIn.getKey()); 
		}
		if(status != null && !status.isEmpty()) {
			// Filter by status if set.
			query = query.filter("status", status.toUpperCase());
		}

		return query.list();
	}
	
	public Collection<OrderDTO> getOrdersBySpotAsDto(Business business, Long spotId, Long checkInId) {
		return transform.ordersToDto(getOrdersBySpot( business, spotId, checkInId));
	}
	
	/**
	 * Return all orders not in the cart for the given spot.
	 * 
	 * @param business
	 * @param spotId
	 * @param checkInId filter by checkin if not null
	 * @return list of the orders found
	 */
	public List<Order> getOrdersBySpot(Business business, Long spotId, Long checkInId) {
		Query<Order> query = orderRepo.getOfy().query(Order.class).ancestor(business)
				.filter("status !=", OrderStatus.CART.toString());
		
		if(checkInId != null) {
			query = query.filter("checkIn", CheckIn.getKey(checkInId));
		}

		return query.list();
	}
	
	/**
	 * Create a new Order entity with status CART and save in the datastore.
	 * 
	 * @param business
	 * @param checkIn
	 * @param orderData
	 * @return id of the order
	 */
	public Long placeOrderInCart(Business business, CheckIn checkIn, OrderDTO orderData) {
		if(business == null) {
			throw new IllegalArgumentException("Order cannot be placed, business is null");
		}
		if(checkIn == null) {
			throw new IllegalArgumentException("Order cannot be placed, checkin is null");
		}
		
		if(checkIn.getStatus() != CheckInStatus.CHECKEDIN && checkIn.getStatus() != CheckInStatus.ORDER_PLACED) {
			throw new OrderFailureException("Order cannot be placed, payment already requested or not checked in");
		}
		
		if( orderData.getStatus() != OrderStatus.CART ) {
			throw new OrderFailureException("Order cannot be placed, unexpected order status: "+orderData.getStatus());
		}

		// Check that the order will be placed at the correct business.
		if(business.getId() != checkIn.getBusiness().getId()) {
			throw new IllegalArgumentException("Order cannot be placed, checkin is not at the same business to which the order was sent: id="+checkIn.getBusiness().getId());
		}
		
		// Check if the product to be ordered exists	
		Product product;
		try {
			product = productRepo.getById(checkIn.getBusiness(), orderData.getProduct().getId());
		} catch (com.googlecode.objectify.NotFoundException e) {
			throw new IllegalArgumentException("Order cannot be placed, productId unknown",e);
		}
		Key<Product> productKey = product.getKey();
		Long orderId = null;
		List<OrderChoice> choices = null;
		if(orderData.getProduct().getChoices() != null) {
			choices = new ArrayList<OrderChoice>();
			ArrayList<ChoiceDTO> childChoiceDtos = new ArrayList<ChoiceDTO>();
			
			HashMap<Long, ChoiceDTO> activeChoiceMap = new HashMap<Long, ChoiceDTO>();
			for (ChoiceDTO choiceDto : orderData.getProduct().getChoices()) {
				int selected = 0;
				//TODO check for parent in the originalchoice
				if(choiceDto.getParent() == null) {
					OrderChoice choice = new OrderChoice();
					
					Choice originalChoice;
					try {
						originalChoice = choiceRepo.getById(checkIn.getBusiness(), choiceDto.getId());
					} catch (com.googlecode.objectify.NotFoundException e) {
						throw new IllegalArgumentException("Order cannot be placed, unknown choice id", e);
					}
					if(choiceDto.getOptions() != null ) {
						selected = checkOptions(choiceDto, originalChoice);
						
						originalChoice.setOptions(new ArrayList<ProductOption>(choiceDto.getOptions()));
					}
					
					choice.setChoice(originalChoice);
										
					choices.add(choice);
				}
				else {
					selected = countSelected(choiceDto);
					childChoiceDtos.add(choiceDto);
				}
				
				if(!activeChoiceMap.containsKey(choiceDto.getId())) {
					if(selected > 0)
						activeChoiceMap.put(choiceDto.getId(), choiceDto);
				}
			}
			
			for (ChoiceDTO choiceDto : childChoiceDtos) {
				Choice originalChoice;
				try {
					originalChoice = choiceRepo.getById(checkIn.getBusiness(), choiceDto.getId());
				} catch (com.googlecode.objectify.NotFoundException e) {
					throw new IllegalArgumentException("Order cannot be placed, unknown choice id", e);
				}
				// Check that the parent choice has been selected.
				if(activeChoiceMap.containsKey(choiceDto.getParent())) {
					if(choiceDto.getOptions() != null ) {
						checkOptions(choiceDto, originalChoice);
						
						originalChoice.setOptions(new ArrayList<ProductOption>(choiceDto.getOptions()));
					}
				}
				OrderChoice choice = new OrderChoice();
				
				
				
				
				choice.setChoice(originalChoice);
									
				choices.add(choice);
			}
		}
		

		Key<Order> orderKey = createAndSaveOrder(business.getKey(), checkIn.getKey(), productKey, orderData.getAmount(), choices, orderData.getComment());		
		if(orderKey != null) {
			// order successfully saved
			orderId = orderKey.getId();
		}
		
		return orderId;
	}

	private int checkOptions(ChoiceDTO choiceDto, Choice originalChoice) throws IllegalArgumentException {
		int selected = countSelected(choiceDto);
		// Validate choice selection
		if(selected < originalChoice.getMinOccurence() ) {
			throw new IllegalArgumentException("Order cannot be placed, minOccurence of "+ originalChoice.getMinOccurence() + " not satisfied. selected="+ selected);
		}
		
		if(originalChoice.getMaxOccurence() > 0 && selected > originalChoice.getMaxOccurence() ) {
			throw new IllegalArgumentException("Order cannot be placed, maxOccurence of "+ originalChoice.getMaxOccurence() + " not satisfied. selected="+ selected);
		}
		
		return selected;
	}

	private int countSelected(ChoiceDTO choiceDto) {
		checkNotNull(choiceDto, "choiceDto was null");
		int selected = 0;
		for (ProductOption productOption : choiceDto.getOptions()) {
			if(productOption.getSelected() != null && productOption.getSelected())
				selected++;
		}
		return selected;
	}

	/**
	 * Create a new Order entity with the given data and save it in the datastore;
	 * 
	 * @param business
	 * @param checkIn
	 * @param product
	 * @param amount
	 * @param choices
	 * @param comment
	 * @return key of the new entity
	 */
	public Key<Order> createAndSaveOrder(Key<Business> business, Key<CheckIn> checkIn, Key<Product> product, int amount, List<OrderChoice> choices, String comment) {
		Key<Order> orderKey = null;
		Order order = new Order();
		order.setAmount(amount);
		order.setBusiness(business);
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
		
		if(choices != null) {
			for (OrderChoice orderChoice : choices) {
				// set parent key
				orderChoice.setOrder(orderKey);
				
				// validate choices
				Set<ConstraintViolation<OrderChoice>> choiceViolations = validator.validate(orderChoice);
				
				if(choiceViolations.isEmpty()) {
					if ( orderChoiceRepo.saveOrUpdate(orderChoice) == null ) {
						throw new RuntimeException("Error saving choices for order: " +orderKey.toString());
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
					throw new IllegalArgumentException(message);
				}	
			}
		}
		
		return orderKey;
	}

	/**
	 * Update method for orders called by the backend.
	 * 
	 * @param business
	 * @param order
	 * @param orderData new data update
	 * @return updated Data
	 */
	public OrderDTO updateOrderForBusiness(Business business, Order order, OrderDTO orderData) {
		if(order == null) {
			throw new IllegalArgumentException("Order cannot be updated, order is null.");
		}
	
		CheckIn checkIn = checkInRepo.getByKey(order.getCheckIn());
		orderData.setCheckInId(checkIn.getId());
		orderData.setOrderTime(order.getOrderTime());
		
		if(!order.getStatus().isTransitionAllowed(orderData.getStatus())) {
			throw new IllegalArgumentException(String.format("Order cannot be updated, change from %s to %s forbidden.",
					order.getStatus(), orderData.getStatus() ));
		}
		
		OrderStatus oldStatus = order.getStatus();
		// update order object from submitted data
		order.setStatus(orderData.getStatus());
		
		Set<ConstraintViolation<Order>> violations = validator.validate(order);
		if(violations.isEmpty()) {
			// save order
			orderRepo.saveOrUpdate(order);
			UpdateOrderEvent updateOrderEvent = new UpdateOrderEvent(business, order, checkIn); 
			
			// Check that we actually had a placed request.
			if(oldStatus == OrderStatus.PLACED && order.getStatus() != oldStatus) {
				// Get all pending requests sorted by oldest first.
				List<Request> requests = requestRepo.ofy().query(Request.class).filter("spot",checkIn.getSpot()).order("-receivedTime").list();
				
				String newCheckInStatus = null;
				// Save the current assumed spot status for reference.
				String oldSpotStatus = requests.get(0).getStatus();
				
				for (Iterator<Request> iterator = requests.iterator(); iterator.hasNext();) {
					Request request = iterator.next();
					if( request.getType() == RequestType.ORDER &&  request.getObjectId().equals(order.getId())) {
						requestRepo.delete(request);
						iterator.remove();
					}
					// Look for other order requests for the current checkin as long as we have no new status or there are no more requests ...
					else if( newCheckInStatus == null && request.getType() == RequestType.ORDER && request.getCheckIn().getId() == checkIn.getId()) {
						// ... set the status to the new found one.
						newCheckInStatus = request.getStatus();
					}
				}
				
				// No other requests for the current checkin were found ...
				if(newCheckInStatus == null) {
					// ... set the status back to CHECKEDIN.
					newCheckInStatus = CheckInStatus.CHECKEDIN.toString();
				}
				
				String newSpotStatus;
				// Save the status of the next request in line, if there is one.
				if( !requests.isEmpty()) {
					newSpotStatus = requests.get(0).getStatus();
				}
				else
					newSpotStatus = CheckInStatus.CHECKEDIN.toString();
				
				// If the spot status needs to be updated ...
				if(!oldSpotStatus.equals(newSpotStatus)) {
					updateOrderEvent.setNewSpotStatus(newSpotStatus);
				}	
				
				// If the payment hasnt already been requested and the checkin status has changed ...  
				if(!checkIn.getStatus().equals(CheckInStatus.PAYMENT_REQUEST) && !checkIn.getStatus().equals(newCheckInStatus) ) {
					// ...update the status of the checkIn in the datastore ...
					checkIn.setStatus(CheckInStatus.valueOf(newCheckInStatus));
					checkInRepo.saveOrUpdate(checkIn);
					
					updateOrderEvent.setNewCheckInStatus(newCheckInStatus);
					
				}
			}
			// if the order changed send a update message
			if(oldStatus != order.getStatus()) {
				updateOrderEvent.setOrderData(orderData);
				eventBus.post(updateOrderEvent);
			}
		}
		else {
			// build validation error messages
			String message = "Validation errors:\n";
			for (ConstraintViolation<Order> constraintViolation : violations) {
				message += constraintViolation.getPropertyPath() + " " + constraintViolation.getMessage() + "\n";
				
			}
			throw new IllegalArgumentException(message);
		}

		return orderData; //transform.orderToDto( order );
	}
}
