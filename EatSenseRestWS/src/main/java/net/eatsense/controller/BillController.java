package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.eatsense.domain.Account;
import net.eatsense.domain.Area;
import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.Product;
import net.eatsense.domain.Request;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.embedded.ChoiceOverridePrice;
import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.domain.embedded.ProductOption;
import net.eatsense.event.NewBillEvent;
import net.eatsense.event.UpdateBillEvent;
import net.eatsense.exceptions.BillFailureException;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.BillRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.BillDTO;
import net.eatsense.representation.Transformer;
import net.eatsense.validation.ValidationHelper;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.googlecode.objectify.NotFoundException;

/**
 * Handles creation of bills and price calculations.
 * 
 * @author Nils Weiher
 *
 */
public class BillController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final OrderRepository allOrders;
	private final ProductRepository productRepo;
	private final OrderChoiceRepository orderChoiceRepo;
	private final BillRepository allBills;
	private final CheckInRepository checkInRepo;
	private final RequestRepository requestRepo;
	private final Transformer transform;
	private final EventBus eventBus;
	private final SpotRepository spotRepo;
	private final AccountRepository accountRepo;
	private final ValidationHelper validator;
	private final AreaRepository areaRepo;
	
	@Inject
	public BillController(RequestRepository rr, OrderRepository orderRepo,
			OrderChoiceRepository orderChoiceRepo,
			ProductRepository productRepo, CheckInRepository checkInRepo,
			BillRepository billRepo, Transformer transformer, EventBus eventBus, SpotRepository spotRepo, AccountRepository accountRepo, ValidationHelper validator, AreaRepository areaRepo) {
		super();
		this.accountRepo = accountRepo;
		this.spotRepo = spotRepo;
		this.eventBus = eventBus;
		this.transform = transformer;
		this.requestRepo = rr;
		this.allOrders = orderRepo;
		this.productRepo = productRepo;
		this.orderChoiceRepo = orderChoiceRepo;
		this.checkInRepo = checkInRepo;
		this.allBills = billRepo;
		this.validator = validator;
		this.areaRepo = areaRepo;
	}
	
	/**
	 * Retrieve a bill for the given checkin.
	 * 
	 * @param business
	 * @param checkInId entity id
	 * @return bill DTO or null if not found 
	 */
	public Bill getBillForCheckIn(Business business, long checkInId) {
		checkNotNull(business, "business cannot be null");
		checkArgument(checkInId != 0 , "checkInId cannot be zero");
		
		Bill bill = allBills.belongingToCheckInAndLocation(business, checkInId);
		if(bill == null) {
			throw new net.eatsense.exceptions.NotFoundException();
		}
		return bill;
	}
	
	/**
	 * Update a bill to cleared status.
	 * 
	 * @param business
	 * @param bill
	 * @param billData
	 * @return updated Bill entity
	 */
	public Bill updateBill(final Business business, Bill bill , BillDTO billData) {
		checkNotNull(business, "Bill cannot be updated, business is null");
		checkNotNull(bill, "Bill cannot be updated, bill is null");
		checkNotNull(billData, "Bill cannot be updated, billdata is null");
		
		if(!billData.isCleared()) {
			logger.error("Bill cannot be updated, cleared must be set to true.");
			throw new ValidationException("Bill cannot be updated, cleared must be set to true.");
		}
		
		if(bill.isCleared()) {
			logger.error("Bill cannot be updated, bill already cleared.");
			throw new BillFailureException("Bill cannot be updated, bill already cleared.");
		}
		
		Iterable<Order> ordersForCheckIn = allOrders.belongingToLocationAndCheckIn(business, bill.getCheckIn());
		
		if(!ordersForCheckIn.iterator().hasNext()) {
			logger.error("Bill cannot be updated, no orders found.");
			throw new BillFailureException("Bill cannot be updated, no orders found.");			
		}
		
		// Holds all Orders ready to be billed.
		List<Order> ordersToBill = new ArrayList<Order>(); 
		// Currency used for this business.
		CurrencyUnit currencyUnit = CurrencyUnit.of(business.getCurrency());
		Money billTotal = Money.of(currencyUnit, 0);
		
		// Check all orders for the CheckIn. Skip orders, that are already completed, in the cart or cancelled.
		// Throw an exception if there are placed (unconfirmed) orders.
		for (Iterator<Order> iterator = ordersForCheckIn.iterator(); iterator.hasNext();) {
			Order order = iterator.next();
			if(order.getStatus() == OrderStatus.PLACED) {
				throw new BillFailureException("Bill cannot be updated, unconfirmed orders available.");
			}
			else if(order.getStatus().equals(OrderStatus.CANCELED) || order.getStatus().equals(OrderStatus.CART) || order.getStatus() == OrderStatus.COMPLETE ) {
				break;
			}
			else {
				billTotal = billTotal.plus(calculateTotalPrice(order, currencyUnit));
				order.setStatus(OrderStatus.COMPLETE);
				order.setBill(bill.getKey());
				ordersToBill.add(order);
			}
		}
		
		if(ordersToBill.isEmpty()) {
			logger.warn("Bill not updated, no Orders ready to be completed.");
			return bill;
		}
		
		allOrders.saveOrUpdateAsync(ordersToBill);
		
		bill.setTotal(billTotal.getAmountMinorLong());
		bill.setCleared(true);
		
		allBills.saveOrUpdate(bill);
		
		CheckIn checkIn = checkInRepo.getByKey(bill.getCheckIn());

		// ...update the status of the checkIn in the datastore ...
		checkIn.setStatus(CheckInStatus.COMPLETE);
		checkIn.setArchived(true);
		checkInRepo.saveOrUpdate(checkIn);
		
		if(checkIn.getAccount() != null) {
			Account account = null;
			try {
				account = accountRepo.getByKey(checkIn.getAccount());
			} catch (NotFoundException e) {
				logger.warn("Could not find associated Account for CheckIn({}).",checkIn.getId());
			}
			if( account != null && account.getActiveCheckIn() != null && account.getActiveCheckIn().equals(bill.getCheckIn()) ) {
				account.setActiveCheckIn(null);
				accountRepo.saveOrUpdate(account);
			}
		}
		
		// Get all pending requests sorted by oldest first.
		Iterable<Request> requests = requestRepo.belongingToSpotOrderedByReceivedTime(checkIn.getSpot());
		Request nextOldestRequest = null;
		
		for (Iterator<Request> iterator = requests.iterator(); iterator.hasNext();) {
			Request request = iterator.next();
			if(request.getCheckIn().equals(bill.getCheckIn())) {
				requestRepo.delete(request);
			}
			else if(nextOldestRequest == null) {
				nextOldestRequest = request;
			}
		}
		
		UpdateBillEvent updateEvent = new UpdateBillEvent(business, bill, checkIn);
		
		// Save the status of the next request in line, if there is one.
		if( nextOldestRequest != null) {
			updateEvent.setNewSpotStatus(nextOldestRequest.getStatus());
		}
		
		// Post update event.
		eventBus.post(updateEvent);
		
		return bill;
	}
	
	/**
	 * Create a new bill for a checkInId supplied with billData.
	 * 
	 * @param business
	 * @param billData must have checkInId and paymentMethod set.
	 * @return transfer object for the newly created Bill
	 */
	public BillDTO createBillForCheckIn(final Business business, BillDTO billData) {
		checkNotNull(billData, "billData was null");
		
		validator.validate(billData);
		
		CheckIn checkIn = checkInRepo.getById(billData.getCheckInId());
		
		return createBill(business,checkIn, billData, true);
	}
	
	/**
	 * Calls {@link #createBill(Business, CheckIn, BillDTO, boolean)}.
	 * 
	 * @param business
	 * @param checkIn
	 * @param billData
	 * @return
	 */
	public BillDTO createBill(final Business business, CheckIn checkIn, BillDTO billData) {
		return createBill(business, checkIn, billData, false);
	}
	
	/**
	 * Create a new bill and save it in the datastore with the given paymentmethod.
	 * 
	 * @param business
	 * @param checkIn
	 * @param billData
	 * @return bill DTO saved
	 */
	public BillDTO createBill(final Business business, CheckIn checkIn, BillDTO billData, boolean fromBusiness) {
		// Check preconditions.
		checkNotNull(business, "business was null");
		checkNotNull(checkIn, "checkIn was null");
		checkNotNull(billData, "billData was null");
		
		if(billData.getPaymentMethod() == null) {
			logger.error("billData must have a payment method");
			throw new ValidationException("billData must have a payment method");
		}
		
		if(business.getPaymentMethods() == null || business.getPaymentMethods().isEmpty()) {
			logger.error("business must have at least one payment method");
			throw new BillFailureException("business must have at least one payment method");
		}
		
		if(!business.getPaymentMethods().contains(billData.getPaymentMethod())) {
			String message = String.format("No matching payment method in business for %s",
					billData.getPaymentMethod());
			logger.error(message);
			throw new ValidationException(message);
		}
		
		if(checkIn.getStatus() != CheckInStatus.CHECKEDIN && checkIn.getStatus() != CheckInStatus.ORDER_PLACED) {
			String message = String.format("Unable to create Bill, invalid checkin status %s", checkIn.getStatus());
			logger.error(message);
			throw new BillFailureException(message);
		}
		
		if(checkIn.getBusiness().getId() != business.getId()) {
			String message= String.format("checkin is not at the same business to which the request was sent: id=%s",
					checkIn.getBusiness().getId());
			
			logger.error(message);
			throw new ValidationException(message);
		}
		
		if(business.isBasic()) {
			logger.error("Unable to create Bill at Business with basic subscription");
			throw new IllegalAccessException("Unable to create Bill at Business with basic subscription");
		}
		
		Spot spot;
		try {
			spot = spotRepo.getByKey(checkIn.getSpot());
		} catch (NotFoundException e) {
			throw new BillFailureException("Unable to find Spot for CheckIn.");
		}
		
		if(spot.isWelcome()) {
			logger.error("Unable to create Bill for checkin at welcome spot");
			throw new IllegalAccessException("Unable to create Bill for checkin at welcome spot");
		}

		Iterable<Order> ordersIterable = allOrders.belongingToLocationAndCheckIn(business, checkIn.getKey());
		boolean foundOrderToBill = false;
		
		Long billId = null;
		for (Iterator<Order> iterator = ordersIterable.iterator(); iterator.hasNext() && !foundOrderToBill;) {
			Order order = iterator.next();
			if(order.getBill() != null) {
				billId = order.getBill().getId();
			}
			else {
				if ( order.getStatus() == OrderStatus.RECEIVED || order.getStatus() == OrderStatus.PLACED)
					foundOrderToBill = true;
			}
		}
		if(!foundOrderToBill) {
			logger.error("no orders to bill where found.");
			throw new BillFailureException("no orders to bill where found.");
		}
		Area area = areaRepo.getByKey(checkIn.getArea());
		
		Bill bill = allBills.newEntity();
		bill.setPaymentMethod(billData.getPaymentMethod());
		bill.setBusiness(business.getKey());
		bill.setCheckIn(checkIn.getKey());
		bill.setCreationTime(new Date());
		bill.setCleared(false);
		bill.setSpot(checkIn.getSpot());
		bill.setSpotName(spot.getName());
		bill.setArea(checkIn.getArea());
		bill.setAreaName(area.getName());
		
		allBills.saveOrUpdate(bill);
	
		billData = transform.billToDto(bill);
		
		Request request = new Request(checkIn, spot, bill);
		request.setObjectText(bill.getPaymentMethod().getName());
		request.setStatus(CheckInStatus.PAYMENT_REQUEST.toString());
		requestRepo.saveOrUpdate(request);
		
		if(checkIn.getStatus() != CheckInStatus.PAYMENT_REQUEST) {
			// Update the status of the checkIn
			checkIn.setStatus(CheckInStatus.PAYMENT_REQUEST);
			checkInRepo.saveOrUpdate(checkIn);				
		}
		NewBillEvent newEvent = new NewBillEvent(business, bill, checkIn, fromBusiness);
		
		Long oldestRequestId = requestRepo.getIdOfOldestRequestBelongingToSpot(checkIn.getSpot());
		
		// If we have no older request in the database ...
		if( oldestRequestId == null || oldestRequestId.equals(request.getId())) {
			newEvent.setNewSpotStatus(request.getStatus());
		}
		
		eventBus.post(newEvent);
		
		return new BillDTO(bill);
	}

	/**
	 * Calculate the total price of the order with selected choices.
	 * 
	 * @param order
	 * @return total price of the order
	 */
	public Money calculateTotalPrice(Order order, CurrencyUnit currencyUnit) {
		checkNotNull(order, "order is null");
		checkNotNull(order.getId(), "id for order is null");
		checkNotNull(order.getProduct(), "product for oder is null");
		
		Money total = Money.of(currencyUnit, 0);
		
		List<OrderChoice> choices = orderChoiceRepo.getByParent(order.getKey());
		if(choices != null && !choices.isEmpty() ) {
			for (OrderChoice orderChoice : choices) {
				total = total.plus(calculateTotalPrice(orderChoice, currencyUnit));
			}
		}
		
		Product product;
		try {
			product = productRepo.getByKey(order.getProduct());
		} catch (NotFoundException e) {
			throw new IllegalArgumentException("Product " + order.getProduct() + " not found for order with id: " + order.getId() ,e);
		}
		
		total = total.plusMinor(product.getPrice());
		
		return total.multipliedBy( order.getAmount() );
	}

	/**
	 * Calculate the total price of the selected options of this choice.
	 * 
	 * @param orderChoice
	 * @return
	 */
	private Money calculateTotalPrice(OrderChoice orderChoice, CurrencyUnit currencyUnit) {
		checkNotNull(orderChoice, "orderchoice is null");
		checkNotNull(orderChoice.getChoice(), "choice is null for orderChoice with id %s",orderChoice.getId());
		checkNotNull(orderChoice.getChoice().getOptions(), "options are null for choice with id %s",orderChoice.getChoice().getId());
		checkArgument(!orderChoice.getChoice().getOptions().isEmpty(), "options are empty for choice with id %s", orderChoice.getChoice().getId());
		
		int selected = 0;
		Money total = Money.of(currencyUnit, 0);
		for (ProductOption option : orderChoice.getChoice().getOptions()) {
			if(option.getSelected() != null && option.getSelected()) {
				selected++;
				if(selected > orderChoice.getChoice().getIncludedChoices()) {
					if(orderChoice.getChoice().getOverridePrice() == ChoiceOverridePrice.OVERRIDE_SINGLE_PRICE)
						total = total.plusMinor(orderChoice.getChoice().getPrice());
					else
						total = total.plusMinor(option.getPriceMinor());
				}
			}
		}
		
		if(selected > 0 && orderChoice.getChoice().getOverridePrice() == ChoiceOverridePrice.OVERRIDE_FIXED_SUM)
			return Money.ofMinor(currencyUnit, orderChoice.getChoice().getPrice());
		
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
		checkArgument(billId != 0, "billId must be different from 0");
		
		try {
			return allBills.getById(business.getKey(), billId);
		} catch (NotFoundException e) {
			logger.error("Unable to get bill, unknown billId.", e);
			return null;
		}
	}
}
