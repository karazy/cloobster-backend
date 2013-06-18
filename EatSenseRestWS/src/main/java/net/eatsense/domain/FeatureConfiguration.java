package net.eatsense.domain;

import java.util.Map;

import javax.persistence.Id;

import com.google.common.collect.Maps;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Serialized;

public class FeatureConfiguration {
	@Id
	private String featureName;
	
	@Parent
	private Key<Business> location;
	
	@Serialized
	private Map<String, String> values = Maps.newHashMap();
}
