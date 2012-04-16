package net.eatsense.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

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
import net.eatsense.domain.Business;
import net.eatsense.exceptions.OrderFailureException;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.BusinessRepository;
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
	private BusinessRepository businessRepo;
	private ChoiceRepository choiceRepo;
	private Transformer transform;
	private RequestRepository requestRepo;
	private ChannelController channelCtrl;
	
	
	@Inject
	public OrderController(OrderRepository orderRepo,
			OrderChoiceRepository orderChoiceRepo, ProductRepository productRepo, BusinessRepository businessRepo, CheckInRepository checkInRepo, ChoiceRepository choiceRepo, RequestRepository rr,Transformer trans, ChannelController channelCtrl, Validator validator) {
		super();
		this.validator = validator;
		this.channelCtrl = channelCtrl;
		this.requestRepo = rr;
		this.orderRepo = orderRepo;
		this.productRepo = productRepo;
		this.orderChoiceRepo = orderChoiceRepo;
		this.choiceRepo = choiceRepo;
		this.checkInRepo = checkInRepo;
		this.businessRepo = businessRepo;
		this.transform = trans;
	}

	/**
	 * Delete the specified order from the datastore.
	 * 
	 * @param businessId
	 * @param orderId
	 * @param checkInUid 
	 */
	public void deleteOrder(Long businessId, Long orderId, String checkInUid) {
		Key<Order> orderKey = new Key<Order>(new Key<Business>(Business.class, businessId), Order.class, orderId);
		Order order;
		try {
			order = orderRepo.getByKey(orderKey);
		} catch (com.googlecode.objectify.NotFoundException e) {
			logger.error("Failed to delete order", e);
		}
		CheckIn checkIn = checkInRepo.getByProperty("userId", checkInUid);
		
		orderRepo.ofy().delete(orderRepo.ofy().query(OrderChoice.class).ancestor(orderKey).listKeys());
		orderRepo.ofy().delete(orderKey);
	}
	
	/**
	 * <p>
	 * Updates a specific order (identified by orderId)<br>
	 * from a given checkIn (identified by checkInId)<br>
	 * of a specific business (identified by businessId)<br>
	 * with new data contained in orderData.
	 * </p>
	 * 
	 * @param businessId
	 * @param orderId
	 * @param orderData
	 * @param checkInUid
	 * @return the updated OrderDTO
	 */
	public OrderDTO updateOrder(Long businessId, Long orderId, OrderDTO orderData, String checkInUid) {
		//
		// Check preconditions and retrieve entities.
		//
		// Retrieve the order from the store.
		Order order;
		try {
			order = getOrder(businessId, orderId);
		} catch (com.googlecode.objectify.NotFoundException e) {
			throw new IllegalArgumentException("Order cannot be updated, unknown business or order id given.");
		}
		// Retrieve the checkin from the store.
		CheckIn checkIn = checkInRepo.getByProperty("userId", checkInUid);
		if(checkIn == null) {
			throw new IllegalArgumentException("Order cannot be updated, invalid checkInUid given");
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
			
			orderData = transform.orderToDto( order );
			// only create a new request if the order status was updated
			if(order.getStatus() != oldStatus) {
				// Update the checkin status, if there is none set.
				if(checkIn.getStatus() == CheckInStatus.CHECKEDIN) {
					checkIn.setStatus(CheckInStatus.ORDER_PLACED);
					checkInRepo.saveOrUpdate(checkIn);
				}
				
				Request request = new Request();
				request.setCheckIn(checkIn.getKey());
				request.setBusiness(Business.getKey(businessId));
				request.setObjectId(orderId);
				request.setType(RequestType.ORDER);
				request.setReceivedTime(new Date());
				request.setSpot(checkIn.getSpot());
				request.setStatus(CheckInStatus.ORDER_PLACED.toString());

				requestRepo.saveOrUpdate(request);
				
				ArrayList<MessageDTO> messages = new ArrayList<MessageDTO>();
				
				messages.add(new MessageDTO("order", "update", orderData));
				
				Key<Request> oldestRequest = requestRepo.ofy().query(Request.class).filter("spot",checkIn.getSpot()).order("-receivedTime").getKey();
				// If we have no older request in the database or the new request is the oldest...
				if( oldestRequest == null || oldestRequest.getId() == request.getId() ) {
					// Send message to notify cockpit clients over their channel
					messages.add(new MessageDTO("checkin", "update", transform.toStatusDto(checkIn)));
					
					SpotStatusDTO spotData = new SpotStatusDTO();
					spotData.setId(checkIn.getSpot().getId());
					spotData.setStatus(request.getStatus());
					messages.add(new MessageDTO("spot","update",spotData));
					
				}

				channelCtrl.sendMessagesToAllCockpitClients(businessId, messages);
				
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
	 * @param businessId
	 * @param orderId
	 * @return
	 */
	public OrderDTO getOrderAsDTO(Long businessId, Long orderId) {
		Order order = getOrder(businessId, orderId);
				
		return transform.orderToDto( order );
	}

	/**
	 * Get the order entity for a given orderId of a business.
	 * 
	 * @param businessId
	 * @param orderId
	 * @return the Order entity, if existing
	 */
	public Order getOrder(Long businessId, Long orderId) {
		return orderRepo.getById(Business.getKey(businessId), orderId);
	}
	
	/**
	 * Get all order data 
	 * 
	 * @param businessId
	 * @param checkInId
	 * @param status
	 * @return
	 */
	public Collection<OrderDTO> getOrdersAsDto(Long businessId, String checkInId, String status) {
		return transform.ordersToDto(getOrders( businessId, checkInId, status));
	}
	
	/**
	 * Get orders saved for the given checkin and filter by status if set.
	 * 
	 * @param businessId
	 * @param checkInId
	 * @param status 
	 * @return
	 */
	public List<Order> getOrders(Long businessId, String checkInId, String status) {
		// Check if the business exists.
		Business business = businessRepo.getById(businessId);
		if(business == null) {
			logger.error("Order cannot be retrieved, business id unknown: " + businessId);
			throw new NotFoundException("Orders cannot be retrieved, business id unknown: " + businessId);
		}
		
		CheckIn checkIn = checkInRepo.getByProperty("userId", checkInId);
		if(checkIn == null) {
			logger.error("Orders cannot be retrieved, checkin not found.");
			throw new NotFoundException("Orders cannot be retrieved, checkin not found.");
		}
		
		
		Query<Order> query = orderRepo.getOfy().query(Order.class).ancestor(business).filter("checkIn", checkIn.getKey());
		if(status != null && !status.isEmpty()) {
			query = query.filter("status", status.toUpperCase());
		}

		return query.list();
	}
	
	public Collection<OrderDTO> getOrdersBySpotAsDto(Long businessId, Long spotId, Long checkInId) {
		return transform.ordersToDto(getOrdersBySpot( businessId, spotId, checkInId));
	}
	
	/**
	 * Return all orders not in the cart for the given spot.
	 * 
	 * @param businessId
	 * @param spotId
	 * @param checkInId
	 * @return list of the orders found
	 */
	public List<Order> getOrdersBySpot(Long businessId, Long spotId, Long checkInId) {
		// Check if the business exists.
		Business business = businessRepo.getById(businessId);
		if(business == null) {
			logger.error("Order cannot be retrieved, business id unknown: " + businessId);
			throw new NotFoundException("Orders cannot be retrieved, business id unknown: " + businessId);
		}
		
		Query<Order> query = orderRepo.getOfy().query(Order.class).ancestor(business).filter("status !=", OrderStatus.CART.toString());
		
		if(checkInId != null) {
			query = query.filter("checkIn", CheckIn.getKey(checkInId));
		}

		return query.list();
	}
	
	/**
	 * Create a new Order entity with status CART and save in the datastore.
	 * 
	 * @param businessId
	 * @param checkInUid
	 * @param orderData
	 * @return id of the order
	 */
	public Long placeOrderInCart(Long businessId, String checkInUid, OrderDTO orderData) {
		Long orderId = null;
		
		CheckIn checkIn = checkInRepo.getByProperty("userId", checkInUid);
		if(checkIn == null) {
			throw new IllegalArgumentException("Order cannot be placed, checkin not found!");
		}
		if(checkIn.getStatus() != CheckInStatus.CHECKEDIN && checkIn.getStatus() != CheckInStatus.ORDER_PLACED) {
			throw new OrderFailureException("Order cannot be placed, payment already requested or not checked in");
		}
		
		if( orderData.getStatus() != OrderStatus.CART ) {
			throw new OrderFailureException("Order cannot be placed, unexpected order status: "+orderData.getStatus());
		}
		
		// Check if the business exists.
		Business business;
		try {
			business = businessRepo.getById(businessId);
		} catch (com.googlecode.objectify.NotFoundException e) {
			throw new IllegalArgumentException("Order cannot be placed, business id unknown", e);
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
		
		List<OrderChoice> choices = null;
		if(orderData.getProduct().getChoices() != null) {
			choices = new ArrayList<OrderChoice>();
			
			for (ChoiceDTO choiceDto : orderData.getProduct().getChoices()) {
				OrderChoice choice = new OrderChoice();
				int selected = 0;
				Choice originalChoice = choiceRepo.getById(checkIn.getBusiness(), choiceDto.getId());
				
				if(originalChoice == null) {
					throw new IllegalArgumentException("Order cannot be placed, unknown choice id="+choiceDto.getId());
				}
				if(choiceDto.getOptions() != null ) {
					
					for (ProductOption productOption : choiceDto.getOptions()) {
						if(productOption.getSelected() != null && productOption.getSelected())
							selected++;
					}
					// Validate choice selection
					if(selected < originalChoice.getMinOccurence() ) {
						throw new IllegalArgumentException("Order cannot be placed, minOccurence of "+ originalChoice.getMinOccurence() + " not satisfied. selected="+ selected);
					}
					
					if(originalChoice.getMaxOccurence() > 0 && selected > originalChoice.getMaxOccurence() ) {
						throw new IllegalArgumentException("Order cannot be placed, maxOccurence of "+ originalChoice.getMaxOccurence() + " not satisfied. selected="+ selected);
					}

					originalChoice.setOptions(new ArrayList<ProductOption>(choiceDto.getOptions()));
				}
				
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
		
		if(orderKey == null ) {
			throw new RuntimeException("Error saving order for product: " +product.toString());
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
	 * @param businessId
	 * @param orderId
	 * @param orderData new data update
	 * @return updated Data
	 */
	public OrderDTO updateOrderForBusiness(Long businessId, Long orderId, OrderDTO orderData) {
		Order order = getOrder(businessId, orderId);
		if(order == null) {
			throw new IllegalArgumentException("Order cannot be updated, unknown business or order id given.");
		}
	
		CheckIn checkIn = checkInRepo.getByKey(order.getCheckIn());
		
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
			if( orderRepo.saveOrUpdate(order) == null )
				throw new RuntimeException("order could not be updated, id: " + orderId);
			
			ArrayList<MessageDTO> messages = new ArrayList<MessageDTO>();
			
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
					// Add a message with updated spot status to the package.
					SpotStatusDTO spotData = new SpotStatusDTO();
					spotData.setId(checkIn.getSpot().getId());
					spotData.setStatus(newSpotStatus);
					messages.add(new MessageDTO("spot","update",spotData));
				}	
				
				// If the payment hasnt already been requested and the checkin status has changed ...  
				if(!checkIn.getStatus().equals(CheckInStatus.PAYMENT_REQUEST) && !checkIn.getStatus().equals(newCheckInStatus) ) {
					// ...update the status of the checkIn in the datastore ...
					checkIn.setStatus(CheckInStatus.valueOf(newCheckInStatus));
					checkInRepo.saveOrUpdate(checkIn);
					
					// ... and add a message with updated checkin status to the package.
					messages.add(new MessageDTO("checkin","update",transform.toStatusDto(checkIn)));
				}
			}
			// if the order changed send a update message
			if(oldStatus != order.getStatus()) {
				// Add a message with updated order status to the message package.
				messages.add(new MessageDTO("order","update",orderData));
				
				// If we cancel the order, let the checkedin customer know.
				if(order.getStatus() == OrderStatus.CANCELED)
					channelCtrl.sendMessage(checkIn.getChannelId(), "order", "update", orderData);
			}
			// Send messages if there are any.
			if(!messages.isEmpty()) {
				try {
					channelCtrl.sendMessagesToAllCockpitClients(businessId, messages);
				} catch (Exception e) {
					logger.error("error while sending messages", e);
				}
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
