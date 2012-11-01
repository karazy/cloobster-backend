/**
 * 
 */
package net.eatsense.configuration;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.googlecode.objectify.NotFoundException;

/**
 * @author Nils
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationProviderTest {
	
	private ConfigurationProvider provider;
	
	@Mock
	private ConfigurationRepository repository;

	@Mock
	private Configuration mockConfig;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		provider = new ConfigurationProvider(repository);
	}

	@Test
	public void testGetConfigFound() {
		when(repository.get("default")).thenReturn(mockConfig );
		
		Configuration config = provider.get();
		
		assertThat(config, is(mockConfig));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetConfigNotFound() {
		when(repository.get("default")).thenThrow(NotFoundException.class);
		when(repository.createDefault()).thenReturn(mockConfig);
		
		Configuration config = provider.get();
		
		assertThat(config, is(mockConfig));
	}
}
