package net.eatsense.controller;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.eatsense.domain.Bill;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.CheckInStatus;
import net.eatsense.domain.ChoiceOverridePrice;
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.PaymentMethod;
import net.eatsense.domain.Product;
import net.eatsense.domain.ProductOption;
import net.eatsense.domain.Request;
import net.eatsense.domain.Request.RequestType;
import net.eatsense.domain.Restaurant;
import net.eatsense.persistence.BillRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.representation.BillDTO;
import net.eatsense.representation.cockpit.SpotStatusDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;

/**
 * Handles creation of bills and price calculations.
 * 
 * @author Nils Weiher
 *
 */
public class BillController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private OrderRepository orderRepo;
	private ProductRepository productRepo;
	private OrderChoiceRepository orderChoiceRepo;
	private BillRepository billRepo;
	
	private CheckInRepository checkInRepo;

	private RestaurantRepository restaurantRepo;

	private RequestRepository requestRepo;
	private ChannelController channelCtrl;
	
	
	@Inject
	public BillController(RequestRepository rr, OrderRepository orderRepo,
			OrderChoiceRepository orderChoiceRepo, ProductRepository productRepo, RestaurantRepository restaurantRepo, CheckInRepository checkInRepo,  BillRepository billRepo, ChannelController cctrl) {
		super();
		this.channelCtrl = cctrl;
		this.requestRepo = rr;
		this.orderRepo = orderRepo;
		this.productRepo = productRepo;
		this.orderChoiceRepo = orderChoiceRepo;
		this.checkInRepo = checkInRepo;
		this.restaurantRepo = restaurantRepo;
		this.billRepo = billRepo;
	}
	
	public BillDTO createBill(Long restaurantId, String checkInId, BillDTO billData) {
		CheckIn checkIn = checkInRepo.getByProperty("userId", checkInId);
		if(checkIn == null) {
			throw new IllegalArgumentException("Bill cannot be created, checkin not found!");
		}
		if(checkIn.getStatus() != CheckInStatus.CHECKEDIN && checkIn.getStatus() != CheckInStatus.ORDER_PLACED) {
			throw new IllegalArgumentException("Bill cannot be created, payment already requested or not checked in");
		}
		
		// Check if the restaurant exists.
		Restaurant restaurant = restaurantRepo.getById(restaurantId);
		if(restaurant == null) {
			logger.error("Bill cannot be created, restaurant id unknown" + restaurantId);
		}
		if(restaurant.getId() != checkIn.getRestaurant().getId()) {
			logger.error("Bill cannot be created, checkin is not at the same restaurant to which the request was sent: id="+checkIn.getRestaurant().getId());
			return null;
		}
		
		List<Order> orders = orderRepo.getOfy().query(Order.class).ancestor(restaurant).filter("checkIn", checkIn.getKey()).list();
		
		Long billId = null;
		for (Iterator<Order> iterator = orders.iterator(); iterator.hasNext();) {
			Order order = iterator.next();
			if(order.getBill() != null) {
				billId = order.getBill().getId();
				iterator.remove();
			}
				
		}
		if(orders.isEmpty()) {
			logger.info("Retrieved request to create bill, but no orders to bill where found. Returning last known bill id");
			billData.setId(billId);
		}
		else {
			Bill bill = saveBill(restaurant, checkIn, orders, billData.getPaymentMethod());	
			if(bill == null) {
				throw new RuntimeException("Bill cannot be created, error saving data");
			}
			billData.setId(bill.getId());
			billData.setTime(bill.getCreationTime());
			billData.setTotal(bill.getTotal());
			
			Request request = new Request();
			request.setRestaurant(restaurant.getKey());
			request.setCheckIn(checkIn.getKey());
			request.setSpot(checkIn.getSpot());
			request.setType(RequestType.BILL);
			request.setReceivedTime(new Date());
			request.setStatus(CheckInStatus.PAYMENT_REQUEST.toString());
			request.setObjectId(bill.getId());
			
			requestRepo.saveOrUpdate(request);
			
			
			checkIn.setStatus(CheckInStatus.PAYMENT_REQUEST);
			
			Key<Request> oldestRequest = requestRepo.ofy().query(Request.class).filter("spot",checkIn.getSpot()).order("-receivedTime").getKey();
			
			// If we have an older request in the database ...
			if( oldestRequest == null || oldestRequest.getId() == request.getId() ) {
				// Send message to notify clients over their channel
				
				SpotStatusDTO spotData = new SpotStatusDTO();
				spotData.setId(checkIn.getSpot().getId());
				spotData.setStatus(request.getStatus());
				
				try {
					channelCtrl.sendMessageToAllClients(restaurantId, "spot", "update", spotData);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}		
		}
		return billData;
	}
	
	
	private Bill saveBill(Restaurant restaurant, CheckIn checkIn,
			List<Order> orders, PaymentMethod paymentMethod) {
		
		if(restaurant.getPaymentMethods() == null || restaurant.getPaymentMethods().isEmpty()) {
			throw new RuntimeException("Bill cannot be created, restaurant has no payment methods saved");
		}
		Bill bill = new Bill();
		
		for (PaymentMethod payment : restaurant.getPaymentMethods()) {
			if(payment.getName().equals(paymentMethod.getName()) )
				bill.setPaymentMethod(paymentMethod);
		}
		if(bill.getPaymentMethod() == null )
			throw new RuntimeException("Bill cannot be created, no matching payment method found for: " + paymentMethod.getName());
				
		bill.setRestaurant(restaurant.getKey());
		bill.setCheckIn(checkIn.getKey());
		bill.setCreationTime(new Date());
		
		Float billTotal= 0f;
		
		for (Order order : orders) {
			billTotal += calculateTotalPrice(order);
		}
		
		bill.setTotal(billTotal);
		
		Key<Bill> billKey = billRepo.saveOrUpdate(bill);
		if(billKey == null)
			return null;
		else {
			for (Order order : orders) {
				order.setBill(billKey);
			}
			orderRepo.saveOrUpdate(orders);
			
		}
			
		
		return bill;
	}

	public Float calculateTotalPrice(Order order) {
		Float total = 0f;
		
		List<OrderChoice> choices = orderChoiceRepo.getByParent(order.getKey());
		if(choices != null && !choices.isEmpty() ) {
			for (OrderChoice orderChoice : choices) {
				Float choicePrice = calculateTotalPrice(orderChoice );
				total += choicePrice;
			}
		}		
		
		Product product = productRepo.getByKey(order.getProduct());
		if(product == null ) {
			throw new RuntimeException("Product " + order.getProduct() + " not found for order with id: " + order.getId() );
		}
		
		total += product.getPrice();
		
		return total;
	}

	private Float calculateTotalPrice(OrderChoice orderChoice) {
		if( orderChoice.getChoice() == null)
			throw new IllegalArgumentException("no saved choice for orderChoice with id: " + orderChoice.getId());
		
		if(orderChoice.getChoice().getOptions() == null || orderChoice.getChoice().getOptions().isEmpty())
			throw new IllegalArgumentException("no saved options for choice with id: " + orderChoice.getChoice().getId());
		
		int selected = 0;
		Float total = 0f;
		for (ProductOption option : orderChoice.getChoice().getOptions()) {
			if(option.getSelected() != null && option.getSelected()) {
				selected++;
				if(selected > orderChoice.getChoice().getIncludedChoices()) {
					if(orderChoice.getChoice().getOverridePrice() == ChoiceOverridePrice.OVERRIDE_SINGLE_PRICE)
						total += orderChoice.getChoice().getPrice();
					else
						total += option.getPrice();
				}
			}
		}
		
		if(selected > 0 && orderChoice.getChoice().getOverridePrice() == ChoiceOverridePrice.OVERRIDE_FIXED_SUM)
			return orderChoice.getChoice().getPrice();
		
		return total;
	}
}
