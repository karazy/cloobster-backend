package net.eatsense.persistence;

import net.eatsense.domain.StoreCard;

public class StoreCardRepository extends GenericRepository<StoreCard> {
	
	public StoreCardRepository() {
		super(StoreCard.class);
	}
}
