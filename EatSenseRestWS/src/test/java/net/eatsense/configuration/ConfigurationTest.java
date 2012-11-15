/**
 * 
 */
package net.eatsense.configuration;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Nils
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationTest {
	
	private Configuration config;
	
	@Mock
	private ConfigurationRepository repository;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		config = new Configuration();
		config.setRepository(repository);
	}

	@Test
	public void testSave() {
		config.save();
		
		verify(repository).save(config);		
	}
	
	@Test
	public void testSaveNoRepository() {
		config = new Configuration();
		thrown.expect(IllegalStateException.class);
		
		config.save();
	}
}
