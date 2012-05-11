package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.Product;
import net.eatsense.domain.Request;
import net.eatsense.domain.Request.RequestType;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.ChoiceOverridePrice;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.domain.embedded.ProductOption;
import net.eatsense.event.NewBillEvent;
import net.eatsense.event.UpdateBillEvent;
import net.eatsense.exceptions.BillFailureException;
import net.eatsense.persistence.BillRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.representation.BillDTO;
import net.eatsense.representation.Transformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

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
	private RequestRepository requestRepo;
	private Transformer transform;
	private EventBus eventBus;
	
	@Inject
	public BillController(RequestRepository rr, OrderRepository orderRepo,
			OrderChoiceRepository orderChoiceRepo,
			ProductRepository productRepo, CheckInRepository checkInRepo,
			BillRepository billRepo, Transformer transformer, EventBus eventBus) {
		super();
		this.eventBus = eventBus;
		this.transform = transformer;
		this.requestRepo = rr;
		this.orderRepo = orderRepo;
		this.productRepo = productRepo;
		this.orderChoiceRepo = orderChoiceRepo;
		this.checkInRepo = checkInRepo;
		this.billRepo = billRepo;
	}
	
	/**
	 * Retrieve a bill for the given checkin.
	 * 
	 * @param business
	 * @param checkInId entity id
	 * @return bill DTO or null if not found 
	 */
	public BillDTO getBillForCheckIn(Business business, long checkInId) {
		checkNotNull(business, "business cannot be null");
		checkNotNull(business.getId(), "business id cannot be null");
		checkArgument(checkInId != 0 , "checkInId cannot be zero");
		Bill bill = orderRepo.getOfy().query(Bill.class).ancestor(business).filter("checkIn", CheckIn.getKey(checkInId)).get();
		
		return transform.billToDto(bill);
	}
	
	/**
	 * Update a bill to cleared status.
	 * 
	 * @param business
	 * @param bill
	 * @param billData
	 * @return updated bill DTO
	 */
	public BillDTO updateBill(final Business business, Bill bill , BillDTO billData) {
		checkNotNull(business, "Bill cannot be updated, business is null");
		checkNotNull(business.getId(), "Bill cannot be updated, id for business is null");
		checkNotNull(bill, "Bill cannot be updated, bill is null");
		checkNotNull(bill.getId(), "Bill cannot be updated, id for bill is null");
		checkNotNull(bill.getCheckIn(), "Bill cannot be updated, checkin for bill is null");
		checkNotNull(billData, "Bill cannot be updated, billdata is null");
		checkArgument(!bill.isCleared(), "Bill cannot be updated, bill already cleared");
		checkArgument(billData.isCleared(), "Bill cannot be updated, cleared must be set to true");
		
		CheckIn checkIn = checkInRepo.getByKey(bill.getCheckIn());
		List<Order> orders = orderRepo.query().ancestor(business).filter("checkIn", bill.getCheckIn()).list();
		Float billTotal= 0f;
		
		if(orders.isEmpty())
			throw new BillFailureException("Bill cannot be updated, no orders found.");
		
		for (Iterator<Order> iterator = orders.iterator(); iterator.hasNext();) {
			Order order = iterator.next();
			if(order.getStatus() == OrderStatus.PLACED) {
				throw new BillFailureException("Bill cannot be updated, unconfirmed orders available.");
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
		billRepo.saveOrUpdate(bill);
		
		billData = transform.billToDto(bill);

		// ...update the status of the checkIn in the datastore ...
		checkIn.setStatus(CheckInStatus.COMPLETE);
		checkIn.setArchived(true);
		checkInRepo.saveOrUpdate(checkIn);
		// Get all pending requests sorted by oldest first.
		List<Request> requests = requestRepo.ofy().query(Request.class).filter("spot",checkIn.getSpot()).order("-receivedTime").list();

		for (Iterator<Request> iterator = requests.iterator(); iterator.hasNext();) {
			Request request = iterator.next();
			if(request.getType() == RequestType.BILL && request.getObjectId().equals(bill.getId())) {
				requestRepo.delete(request);
				iterator.remove();
			}
		}
		UpdateBillEvent updateEvent = new UpdateBillEvent(business, bill, checkIn);
		
		// Save the status of the next request in line, if there is one.
		if( !requests.isEmpty()) {
			updateEvent.setNewSpotStatus(requests.get(0).getStatus());
		}
		
		// Post update event.
		eventBus.post(updateEvent);
		
		return billData;
	}
	
	/**
	 * Create a new bill and save it in the datastore with the given paymentmethod.
	 * 
	 * @param business
	 * @param checkIn
	 * @param billData
	 * @return bill DTO saved
	 */
	public BillDTO createBill(final Business business, CheckIn checkIn, BillDTO billData) {
		// Check preconditions.
		checkNotNull(business, "business is null");
		checkNotNull(business.getId(), "id for business is null");
		checkNotNull(checkIn, "checkin is null");
		checkNotNull(checkIn.getId(), "id for checkin is null");
		checkNotNull(checkIn.getSpot(), "spot for checkin is null");
		checkNotNull(billData, "billData is null");
		checkNotNull(billData.getPaymentMethod(), "billData must have a payment method");
		checkNotNull(business.getPaymentMethods(), "business must have at least one payment method");
		checkArgument(!business.getPaymentMethods().isEmpty(), "business must have at least one payment method");
		checkArgument(business.getPaymentMethods().contains(billData.getPaymentMethod()),
				"no matching payment method in business for %s",
				billData.getPaymentMethod());
		checkArgument(checkIn.getStatus() == CheckInStatus.CHECKEDIN ||	checkIn.getStatus() == CheckInStatus.ORDER_PLACED,
				"invalid checkin status %s", checkIn.getStatus());
		checkArgument(checkIn.getBusiness().getId() == business.getId(),
				"checkin is not at the same business to which the request was sent: id=%s", checkIn.getBusiness().getId());
		
		List<Order> orders = orderRepo.getOfy().query(Order.class).ancestor(business).filter("checkIn", checkIn.getKey()).list();
		
		Long billId = null;
		for (Iterator<Order> iterator = orders.iterator(); iterator.hasNext();) {
			Order order = iterator.next();
			if(order.getBill() != null) {
				billId = order.getBill().getId();
				iterator.remove();
			}
			else {
				if (order.getStatus() == OrderStatus.CANCELED
						|| order.getStatus() == OrderStatus.CART
						|| order.getStatus() == OrderStatus.COMPLETE)
					iterator.remove();
			}
		}
		if(orders.isEmpty()) {
			logger.info("Retrieved request to create bill, but no orders to bill where found. Returning last known bill id");
			billData.setId(billId);
		}
		else {
			Bill bill = new Bill();
			bill.setPaymentMethod(billData.getPaymentMethod());
			bill.setBusiness(business.getKey());
			bill.setCheckIn(checkIn.getKey());
			bill.setCreationTime(new Date());
			bill.setCleared(false);
			billRepo.saveOrUpdate(bill);
		
			billData = transform.billToDto(bill);
			
			Request request = new Request();
			request.setBusiness(business.getKey());
			request.setCheckIn(checkIn.getKey());
			request.setSpot(checkIn.getSpot());
			request.setType(RequestType.BILL);
			request.setReceivedTime(new Date());
			request.setStatus(CheckInStatus.PAYMENT_REQUEST.toString());
			request.setObjectId(bill.getId());
			requestRepo.saveOrUpdate(request);
			
			if(checkIn.getStatus() != CheckInStatus.PAYMENT_REQUEST) {
				// Update the status of the checkIn
				checkIn.setStatus(CheckInStatus.PAYMENT_REQUEST);
				checkInRepo.saveOrUpdate(checkIn);				
			}
			NewBillEvent newEvent = new NewBillEvent(business, bill, checkIn);
			
			Key<Request> oldestRequest = requestRepo.query().filter("spot",checkIn.getSpot()).order("-receivedTime").getKey();
			
			// If we have no older request in the database ...
			if( oldestRequest == null || oldestRequest.getId() == request.getId() ) {
				newEvent.setNewSpotStatus(request.getStatus());
			}
			
			eventBus.post(newEvent);
		}
		return billData;
	}

	/**
	 * Calculate the total price of the order with selected choices.
	 * 
	 * @param order
	 * @return total price of the order
	 */
	public Float calculateTotalPrice(Order order) {
		checkNotNull(order, "order is null");
		checkNotNull(order.getId(), "id for order is null");
		checkNotNull(order.getProduct(), "product for oder is null");
		
		Float total = 0f;
		
		List<OrderChoice> choices = orderChoiceRepo.getByParent(order.getKey());
		if(choices != null && !choices.isEmpty() ) {
			for (OrderChoice orderChoice : choices) {
				Float choicePrice = calculateTotalPrice(orderChoice );
				total += choicePrice;
			}
		}
		
		Product product;
		try {
			product = productRepo.getByKey(order.getProduct());
		} catch (NotFoundException e) {
			throw new IllegalArgumentException("Product " + order.getProduct() + " not found for order with id: " + order.getId() ,e);
		}
		
		total += product.getPrice();
		
		return total * order.getAmount();
	}

	/**
	 * Calculate the total price of the selected options of this choice.
	 * 
	 * @param orderChoice
	 * @return
	 */
	private Float calculateTotalPrice(OrderChoice orderChoice) {
		checkNotNull(orderChoice, "orderchoice is null");
		checkNotNull(orderChoice.getChoice(), "choice is null for orderChoice with id %s",orderChoice.getId());
		checkNotNull(orderChoice.getChoice().getOptions(), "options are null for choice with id %s",orderChoice.getChoice().getId());
		checkArgument(!orderChoice.getChoice().getOptions().isEmpty(), "options are empty for choice with id %s", orderChoice.getChoice().getId());
		
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

	/**
	 * Retrieve bill from
	 * 
	 * @param business
	 * @param billId
	 * @return
	 */
	public Bill getBill(Business business, long billId) {
		checkNotNull(business, "business is null");
		checkArgument(billId != 0, "billid must be different from 0");
		
		try {
			return billRepo.getById(business.getKey(), billId);
		} catch (NotFoundException e) {
			logger.error("Unable to get bill, unknown billId.", e);
			return null;
		}
	}
}
