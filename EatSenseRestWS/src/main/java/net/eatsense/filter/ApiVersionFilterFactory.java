package net.eatsense.filter;

import java.util.Collections;
import java.util.List;

import net.eatsense.filter.annotation.ApiVersion;

import com.google.common.primitives.Ints;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

/**
 * {@link ResourceFilterFactory} that supports {@link ApiVersion} on
 * resource methods and classes.
 * Creates {@link ApiVersionFilter} that blocks request if they have not set the minimum required api version header. 
 * 
 * @author Nils Weiher
 *
 */
public class ApiVersionFilterFactory implements ResourceFilterFactory {

	@Override
	public List<ResourceFilter> create(AbstractMethod am) {
		ApiVersion annotation = am.getAnnotation(ApiVersion.class);
		
		// Annotation on the resource method
		if(annotation != null){
			return Collections.<ResourceFilter>singletonList(new ApiVersionFilter(annotation.min(), Ints.asList(annotation.value())));
		}
		
		// Check the resource class		
		annotation = am.getResource().getAnnotation(ApiVersion.class);
		
		if(annotation != null){
			return Collections.<ResourceFilter>singletonList(new ApiVersionFilter(annotation.min(), Ints.asList(annotation.value())));
		}
		
		return null;			
	}

}
