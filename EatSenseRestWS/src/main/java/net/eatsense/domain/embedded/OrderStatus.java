package net.eatsense.domain.embedded;

import java.util.EnumMap;
import java.util.EnumSet;

public enum OrderStatus {
	CART,
	PLACED,
	CANCELED,
	RECEIVED,
	INPROCESS,
	COMPLETE;
	
	private static final EnumMap<OrderStatus, EnumSet<OrderStatus>> allowed;
	
	static {
		allowed = new EnumMap<OrderStatus, EnumSet<OrderStatus>>(OrderStatus.class);
		allowed.put(CANCELED, null);
		allowed.put(CART, EnumSet.of(CART, PLACED));
		allowed.put(PLACED, EnumSet.of(PLACED, RECEIVED,CANCELED));
		allowed.put(RECEIVED, EnumSet.of(CANCELED));		
	}
	
	public boolean isTransitionAllowed( OrderStatus newStatus) {
		EnumSet<OrderStatus> newStatusSet = allowed.get(this);
		return newStatusSet==null ? false : newStatusSet.contains(newStatus);					
	}
}
