package net.eatsense;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.container.filter.UriConnegFilter;

public class SuffixFilter extends UriConnegFilter {
	private final static Map<String,String> languageExtensions;
	
	static {
		languageExtensions = new HashMap<String, String>();
		languageExtensions.put("de", "de");
		languageExtensions.put("en", "en");
		languageExtensions.put("fr", "fr");
	}

	public SuffixFilter() {
		super(Collections.<String,MediaType>emptyMap(), languageExtensions);
		// TODO Auto-generated constructor stub
	}

}
