package net.eatsense.filter;

import javax.ws.rs.HttpMethod;

import com.google.common.collect.ImmutableSet;

public final class HttpMethods {
	public static final ImmutableSet<String> WRITE_METHODS;
	
	static {
		WRITE_METHODS = ImmutableSet.of(HttpMethod.DELETE, HttpMethod.POST, HttpMethod.PUT);
	}
}
