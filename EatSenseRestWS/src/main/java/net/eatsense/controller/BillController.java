package net.eatsense.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.eatsense.domain.Bill;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.Product;
import net.eatsense.domain.Request;
import net.eatsense.domain.Request.RequestType;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.ChoiceOverridePrice;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.domain.embedded.PaymentMethod;
import net.eatsense.domain.embedded.ProductOption;
import net.eatsense.domain.Business;
import net.eatsense.persistence.BillRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.representation.BillDTO;
import net.eatsense.representation.Transformer;
import net.eatsense.representation.cockpit.MessageDTO;
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

	private BusinessRepository businessRepo;

	private RequestRepository requestRepo;
	private ChannelController channelCtrl;
	private Transformer transform;
	
	@Inject
	public BillController(RequestRepository rr, OrderRepository orderRepo,
			OrderChoiceRepository orderChoiceRepo, ProductRepository productRepo, BusinessRepository businessRepo, CheckInRepository checkInRepo,
			BillRepository billRepo, ChannelController cctrl, Transformer transformer) {
		super();
		this.transform = transformer;
		this.channelCtrl = cctrl;
		this.requestRepo = rr;
		this.orderRepo = orderRepo;
		this.productRepo = productRepo;
		this.orderChoiceRepo = orderChoiceRepo;
		this.checkInRepo = checkInRepo;
		this.businessRepo = businessRepo;
		this.billRepo = billRepo;
	}
	
	public BillDTO getBillForCheckIn(Long businessId, Long checkInId) {
		BillDTO billData = new BillDTO();
		Bill bill = orderRepo.getOfy().query(Bill.class).ancestor(Business.getKey(businessId)).filter("checkIn", CheckIn.getKey(checkInId)).get();
		if(bill == null)
			return null;
		
		billData.setId(bill.getId());
		billData.setCheckInId(bill.getCheckIn().getId());
		billData.setCleared(bill.isCleared());
		billData.setPaymentMethod(bill.getPaymentMethod());
		billData.setTotal(bill.getTotal());
		billData.setTime(bill.getCreationTime());
		
		return billData;
	}
	
	public BillDTO updateBill(Long businessId, Long billId , BillDTO billData) {
		// Check if the business exists.
		Business business = businessRepo.getById(businessId);
		if(business == null) {
			logger.error("Bill cannot be updated, business id unknown" + businessId);
			return null;
		}
		
		Bill bill = billRepo.getById(business.getKey(), billId);
		if(bill == null) {
			logger.error("Bill cannot be updated, bill id unknown" + businessId);
			return null;
		}
		
		CheckIn checkIn = checkInRepo.getByKey(bill.getCheckIn());
		if(checkIn == null) {
			logger.error("Bill cannot be updated, check in not found.");
			return null;
		}
		
		List<Order> orders = orderRepo.getOfy().query(Order.class).ancestor(business).filter("checkIn", bill.getCheckIn()).list();
		
		Float billTotal= 0f;
		
		for (Iterator<Order> iterator = orders.iterator(); iterator.hasNext();) {
			Order order = iterator.next();
			if(order.getStatus() == OrderStatus.PLACED) {
				throw new RuntimeException("Unconfirmed orders available.");
			}
			else if(order.getStatus().equals(OrderStatus.CANCELED) || order.getStatus().equals(OrderStatus.CART) || order.getStatus() == OrderStatus.COMPLETE ) {
					iterator.remove();
			}
			else {
				billTotal += calculateTotalPrice(order);
				order.setStatus(OrderStatus.COMPLETE);
				order.setBill(bill.getKey());
			}
		}
		bill.setTotal(billTotal);
		bill.setCleared(billData.isCleared());
		orderRepo.saveOrUpdate(orders);
		if(billRepo.saveOrUpdate(bill)== null) {
			throw new RuntimeException("Bill cannot be saved");
		}

		billData.setTotal(bill.getTotal());
		billData.setCheckInId(bill.getCheckIn().getId());
		billData.setCleared(bill.isCleared());
		billData.setId(bill.getId());
		billData.setTime(bill.getCreationTime());
		billData.setPaymentMethod(bill.getPaymentMethod());

		// ...update the status of the checkIn in the datastore ...
		checkIn.setStatus(CheckInStatus.COMPLETE);
		checkIn.setArchived(true);
		checkInRepo.saveOrUpdate(checkIn);
		
		ArrayList<MessageDTO> messages = new ArrayList<MessageDTO>();

		// ... and add a message with updated checkin status to the package.
		messages.add(new MessageDTO("checkin","delete",transform.toStatusDto(checkIn)));
		
		// Add a message with updated order status to the message package.
		messages.add(new MessageDTO("bill","update",billData));
		
		// Get all pending requests sorted by oldest first.
		List<Request> requests = requestRepo.ofy().query(Request.class).filter("spot",checkIn.getSpot()).order("-receivedTime").list();

		for (Iterator<Request> iterator = requests.iterator(); iterator.hasNext();) {
			Request request = iterator.next();
			if(request.getType() == RequestType.BILL && request.getObjectId().equals(bill.getId())) {
				requestRepo.delete(request);
				iterator.remove();
			}
		}
		
		String newSpotStatus = CheckInStatus.CHECKEDIN.toString();
		// Save the status of the next request in line, if there is one.
		if( !requests.isEmpty()) {
			newSpotStatus = requests.get(0).getStatus();
		}
		// Add a message with updated spot status to the package.
		SpotStatusDTO spotData = new SpotStatusDTO();
		spotData.setId(checkIn.getSpot().getId());
		spotData.setStatus(newSpotStatus);
		spotData.setCheckInCount(checkInRepo.countActiveCheckInsAtSpot(checkIn.getSpot()));
		messages.add(new MessageDTO("spot","update",spotData));
				
		try {
			// Send messages to notify clients over their channel.
			channelCtrl.sendMessagesToAllClients(businessId, messages);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return billData;
	}
	
	public BillDTO createBill(Long businessId, String checkInId, BillDTO billData) {
		CheckIn checkIn = checkInRepo.getByProperty("userId", checkInId);
		if(checkIn == null) {
			throw new IllegalArgumentException("Bill cannot be created, checkin not found!");
		}
		if(checkIn.getStatus() != CheckInStatus.CHECKEDIN && checkIn.getStatus() != CheckInStatus.ORDER_PLACED) {
			throw new IllegalArgumentException("Bill cannot be created, payment already requested or not checked in");
		}
		
		// Check if the business exists.
		Business business = businessRepo.getById(businessId);
		if(business == null) {
			logger.error("Bill cannot be created, business id unknown" + businessId);
			return null;
		}
		if(business.getId() != checkIn.getBusiness().getId()) {
			logger.error("Bill cannot be created, checkin is not at the same business to which the request was sent: id="+checkIn.getBusiness().getId());
			return null;
		}
		
		List<Order> orders = orderRepo.getOfy().query(Order.class).ancestor(business).filter("checkIn", checkIn.getKey()).list();
		
		Long billId = null;
		for (Iterator<Order> iterator = orders.iterator(); iterator.hasNext();) {
			Order order = iterator.next();
			if(order.getBill() != null) {
				billId = order.getBill().getId();
				iterator.remove();
			}
			else {
				if(order.getStatus().equals(OrderStatus.CANCELED) || order.getStatus().equals(OrderStatus.CART) )
					iterator.remove();
			}
		}
		if(orders.isEmpty()) {
			logger.info("Retrieved request to create bill, but no orders to bill where found. Returning last known bill id");
			billData.setId(billId);
		}
		else {
			if(business.getPaymentMethods() == null || business.getPaymentMethods().isEmpty()) {
				throw new RuntimeException("Bill cannot be created, business has no payment methods saved");
			}
			Bill bill = new Bill();
			
			for (PaymentMethod payment : business.getPaymentMethods()) {
				if(payment.getName().equals(billData.getPaymentMethod().getName()) )
					bill.setPaymentMethod(billData.getPaymentMethod());
			}
			if(bill.getPaymentMethod() == null )
				throw new RuntimeException("Bill cannot be created, no matching payment method found for: " + billData.getPaymentMethod().getName());
					
			bill.setBusiness(business.getKey());
			bill.setCheckIn(checkIn.getKey());
			bill.setCreationTime(new Date());
			bill.setCleared(false);

			
			Key<Bill> billKey = billRepo.saveOrUpdate(bill);
			if(billKey == null) {
				throw new RuntimeException("Bill cannot be created, error saving data");
			}
		
			billData.setId(bill.getId());
			billData.setCheckInId(checkIn.getId());
			billData.setTime(bill.getCreationTime());
			
			Request request = new Request();
			request.setBusiness(business.getKey());
			request.setCheckIn(checkIn.getKey());
			request.setSpot(checkIn.getSpot());
			request.setType(RequestType.BILL);
			request.setReceivedTime(new Date());
			request.setStatus(CheckInStatus.PAYMENT_REQUEST.toString());
			request.setObjectId(bill.getId());
			
			requestRepo.saveOrUpdate(request);
			
			
			ArrayList<MessageDTO> messages = new ArrayList<MessageDTO>();
			
			// Add a message with the new bill to the message package.
			messages.add(new MessageDTO("bill","new", billData));
			
			
			Key<Request> oldestRequest = requestRepo.ofy().query(Request.class).filter("spot",checkIn.getSpot()).order("-receivedTime").getKey();
			
			// If we have an older request in the database ...
			if( oldestRequest == null || oldestRequest.getId() == request.getId() || checkIn.getStatus() != CheckInStatus.PAYMENT_REQUEST  ) {
				// Update the status of the checkIn
				checkIn.setStatus(CheckInStatus.PAYMENT_REQUEST);
				checkInRepo.saveOrUpdate(checkIn);
				
				// Add a message with updated checkin status to the package.
				messages.add(new MessageDTO("checkin","update",transform.toStatusDto(checkIn)));
				
				SpotStatusDTO spotData = new SpotStatusDTO();
				spotData.setId(checkIn.getSpot().getId());
				spotData.setStatus(request.getStatus());
				// Add a message with updated spot status to the package.
				messages.add(new MessageDTO("spot", "update", spotData));
			}
			try {
				// Send messages to notify clients over their channel.
				channelCtrl.sendMessagesToAllClients(businessId, messages);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return billData;
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
