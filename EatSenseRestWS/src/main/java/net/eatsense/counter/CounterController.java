package net.eatsense.counter;

import java.util.Date;

import net.eatsense.counter.Counter.PeriodType;
import net.eatsense.event.CustomerRequestEvent;
import net.eatsense.event.NewCheckInEvent;
import net.eatsense.event.NewCustomerRequestEvent;
import net.eatsense.event.NewFeedbackEvent;
import net.eatsense.event.PlaceAllOrdersEvent;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Handles all counting, listens to all events we are interested in and updates corresponding counters.
 * 
 * @author Nils Weiher
 *
 */
public class CounterController {
	
	private final CounterService counterService;

	@Inject
	public CounterController(CounterService counterService) {
		this.counterService = counterService;
	}
	
	@Subscribe
	public void countCheckIn(NewCheckInEvent event) {
		counterService.loadAndIncrementCounter("checkins", PeriodType.DAY, new Date(), event.getBusiness().getId(), event.getCheckIn().getArea().getId(), 1);
	}
	
	@Subscribe
	public void countPlacedOrders(PlaceAllOrdersEvent event) {
		counterService.loadAndIncrementCounter("orders-placed", PeriodType.DAY,
						new Date(), event.getCheckIn().getBusiness().getId(),
						event.getCheckIn().getArea().getId(),
						event.getEntityCount());
	}
	
	@Subscribe
	public void countCustomerRequests(NewCustomerRequestEvent event) {
		counterService.loadAndIncrementCounter("customer-requests", PeriodType.DAY, new Date(), event.getBusiness().getId(), event.getRequest().getArea().getId(), 1);
	}
	
	@Subscribe
	public void countFeedback(NewFeedbackEvent event) {
		counterService.loadAndIncrementCounter("feedback", PeriodType.DAY, new Date(), event.getCheckIn().getBusiness().getId(), event.getCheckIn().getArea().getId(), 1);
	}
}
