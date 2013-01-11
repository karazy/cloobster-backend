package net.eatsense.persistence;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.Date;

import net.eatsense.EatSenseDomainModule;
import net.eatsense.domain.Business;
import net.eatsense.domain.Product;

import org.apache.bval.guice.ValidationModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.googlecode.objectify.Key;

public class TrashEntityTest {
	private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	private ProductRepository productRepo;
	private LocationRepository businessRepo;
	
	@Before
	public void setUp() {
		helper.setUp();
		Injector injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		productRepo = injector.getInstance(ProductRepository.class);
		businessRepo = injector.getInstance(LocationRepository.class);
	}
	
	@After
	public void tearDown() {
		helper.tearDown();
	}
	
	@Test
	public void testSaveNewTrashEntryAndDelete() throws Exception {
		Product product = productRepo.newEntity();
		product.setBusiness(new Key<Business>(Business.class, 1));
		product.setName("test product");
		productRepo.saveOrUpdate(product);
		assertThat(productRepo.getAll().isEmpty(), is(false));
		productRepo.trashEntity(product.getKey(), "admin");
		
		productRepo.deleteAllTrash();
		assertThat(productRepo.getAll().isEmpty(), is(true));
	}
	
	@Test
	public void testMixedEntries() throws Exception {
		Product product = productRepo.newEntity();
		Business business = businessRepo.newEntity();
		business.setName("test business");
		
		product.setBusiness(businessRepo.saveOrUpdate(business));
		product.setName("test product");
		productRepo.saveOrUpdate(product);
		
		productRepo.trashEntity(product.getKey(), "admin");
		businessRepo.trashEntity(business.getKey(), "admin");
		
		productRepo.deleteAllTrash();
		assertThat(productRepo.getAll().isEmpty(), is(true));
		assertThat(businessRepo.getAll().isEmpty(), is(true));
	}
}
