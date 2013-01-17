package net.eatsense;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import net.eatsense.ApiVersionFilter;
import net.eatsense.exceptions.ApiVersionException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sun.jersey.spi.container.ContainerRequest;

@RunWith(MockitoJUnitRunner.class)
public class ApiVersionFilterTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private ApiVersionFilter filter;

	@Mock
	private ContainerRequest request;
	
	@Before
	public void setUp() throws Exception {
		filter = new ApiVersionFilter();
		System.setProperty("net.karazy.api.version", "1");
	}
	
	@Test
	public void testFilterWrongVersion() throws Exception {
		when(request.getHeaderValue("cloobster-api")).thenReturn("2");
		thrown.expect(ApiVersionException.class);
		filter.filter(request);
	}
	
	@Test
	public void testFilterNoVersionSet() throws Exception {
		filter.filter(request);
	}
	
	@Test
	public void testFilter() throws Exception {
		when(request.getHeaderValue("cloobster-api")).thenReturn("1");
		
		filter.filter(request);
	}
}
