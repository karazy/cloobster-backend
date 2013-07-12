package net.eatsense.filter;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.eatsense.filter.annotation.ApiVersion;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.spi.container.ResourceFilter;

@RunWith(MockitoJUnitRunner.class)
public class ApiVersionFilterFactoryTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private ApiVersionFilterFactory factory;

	@Before
	public void setUp() throws Exception {
		factory = new ApiVersionFilterFactory();
		
	}
	
	
	@Test
	public void testCreateNoFilter() throws Exception {
		AbstractMethod am = mock(AbstractMethod.class);
		AbstractResource ar = mock(AbstractResource.class);
		when(am.getResource()).thenReturn(ar );
		
		Assert.assertNull(factory.create(am));
	}
	
	@Test
	public void testCreateFilterForMethod() throws Exception {
		AbstractMethod am = mock(AbstractMethod.class);
		AbstractResource ar = mock(AbstractResource.class);
		ApiVersion value = mock(ApiVersion.class);
		int[] versions = {1};
		when(value.value()).thenReturn(versions );
		when(am.getAnnotation(ApiVersion.class)).thenReturn(value );
		when(am.getResource()).thenReturn(ar);
		
		Assert.assertThat(factory.create(am), hasItem(any(ResourceFilter.class)));
	}
}
