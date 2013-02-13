package net.eatsense.counter;

import java.util.Date;

import net.eatsense.counter.Counter.PeriodType;
import net.eatsense.event.NewCheckInEvent;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

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
}
