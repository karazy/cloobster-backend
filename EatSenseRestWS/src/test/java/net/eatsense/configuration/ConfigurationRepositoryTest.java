/**
 * 
 */
package net.eatsense.configuration;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.NotFoundException;

/**
 * @author Nils
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationRepositoryTest {
	
	private final LocalServiceTestHelper helper =
	        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	
	private ConfigurationRepository repository;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private Configuration defaultConfig;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		helper.setUp();
		repository = new ConfigurationRepository();
		defaultConfig = new Configuration();
		repository.ofy().put(defaultConfig);
	}

	@Test
	public void testGetConfigFound() {
		Configuration config = repository.get("default");
		
		assertThat(config.getId(), is("default"));
		assertThat(config.getRepository(), is(repository));
	}
	
	@Test
	public void testGetConfigNotFound() {
		thrown.expect(NotFoundException.class);
		repository.get("unknown");
	}
	
	@Test
	public void testCreateDefaultConfig() throws Exception {
		Configuration config = repository.createDefault();
		
		assertThat(config.getRepository(), is(repository));
		assertThat(config.getId(), is("default"));
	}
}
