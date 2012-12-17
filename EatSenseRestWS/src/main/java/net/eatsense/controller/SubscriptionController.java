package net.eatsense.controller;

import net.eatsense.domain.Subscription;
import net.eatsense.validation.ValidationHelper;

import com.googlecode.objectify.Objectify;

public class SubscriptionController {
	private final Objectify ofy;
	private final ValidationHelper validator;

	public SubscriptionController(Objectify ofy,  ValidationHelper validator) {
		super();
		
		this.ofy = ofy;
		this.validator = validator;
	}
	
	public Subscription createPackage() {
		return null;
		
	}
}
