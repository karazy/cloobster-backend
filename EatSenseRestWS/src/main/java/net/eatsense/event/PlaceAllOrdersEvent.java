package net.eatsense.event;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;

public class PlaceAllOrdersEvent extends MultiUpdateEvent {
	
	private final List<Order> orders;
	private Optional<Business> optBusiness;

	public PlaceAllOrdersEvent(CheckIn checkIn, int entityCount, Collection<Order> orders) {
		super(checkIn, entityCount);
		this.optBusiness = Optional.absent();
		this.orders = ImmutableList.copyOf(orders);
	}

	public List<Order> getOrders() {
		return orders;
	}

	public Optional<Business> getOptBusiness() {
		return optBusiness;
	}

	public void setOptBusiness(Optional<Business> optBusiness) {
		this.optBusiness = optBusiness;
	}
}
