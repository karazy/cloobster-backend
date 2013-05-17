package net.eatsense.auth;

import java.util.Set;

import com.google.common.collect.Sets;

public class AwesomeUserAuthorizer {

	private final static Set<String> awesomeList;
	
	static {
		awesomeList = Sets.newHashSet("reifschneider@karazy.net", "weiher@karazy.net");
	}
	
	public boolean isAwesome(String email) {
		return awesomeList.contains(email);
	}
}
