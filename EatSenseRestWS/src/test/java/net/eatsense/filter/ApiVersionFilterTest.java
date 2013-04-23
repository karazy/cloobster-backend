package net.eatsense.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.PathSegment;

import net.eatsense.exceptions.ApiVersionException;
import net.eatsense.filter.ApiVersionFilter;

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
		filter = new ApiVersionFilter( 0, Arrays.asList(1));
	}
	@Test
	public void testFilterMinVersion() throws Exception {
		filter = new ApiVersionFilter( 2, null);
		when(request.getHeaderValue("cloobster-api")).thenReturn("2");
		filter.filter(request);
	}
	
	@Test
	public void testFilterWrongVersion() throws Exception {
		PathSegment pathSegment = mock(PathSegment.class);
		when(pathSegment.getPath()).thenReturn("anypath");
		List<PathSegment> pathList = Arrays.asList(pathSegment );
		when(request.getPathSegments()).thenReturn(pathList );
		when(request.getHeaderValue("cloobster-api")).thenReturn("2");
		thrown.expect(ApiVersionException.class);
		filter.filter(request);
	}
	
	@Test
	public void testFilterNoVersionSet() throws Exception {
		PathSegment pathSegment = mock(PathSegment.class);
		when(pathSegment.getPath()).thenReturn("anypath");
		List<PathSegment> pathList = Arrays.asList(pathSegment );
		when(request.getPathSegments()).thenReturn(pathList );

		filter.filter(request);
	}
	
	@Test
	public void testFilter() throws Exception {
		PathSegment pathSegment = mock(PathSegment.class);
		when(pathSegment.getPath()).thenReturn("anypath");
		List<PathSegment> pathList = Arrays.asList(pathSegment );
		when(request.getPathSegments()).thenReturn(pathList );
		when(request.getHeaderValue("cloobster-api")).thenReturn("1");
		
		filter.filter(request);
	}
}
