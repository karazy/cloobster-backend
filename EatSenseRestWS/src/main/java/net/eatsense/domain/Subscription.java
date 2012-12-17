package net.eatsense.domain;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;

public class Subscription {
	@Parent
	Key<Business> business;
	
	@Id
	String name;
	
	
}
