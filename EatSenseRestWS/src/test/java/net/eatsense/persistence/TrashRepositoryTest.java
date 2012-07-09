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

public class TrashRepositoryTest {
	private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	private TrashRepository trashRepo;
	private ProductRepository productRepo;
	private BusinessRepository businessRepo;
	
	@Before
	public void setUp() {
		helper.setUp();
		Injector injector = Guice.createInjector(new EatSenseDomainModule(), new ValidationModule());
		trashRepo = injector.getInstance(TrashRepository.class);
		productRepo = injector.getInstance(ProductRepository.class);
		businessRepo = injector.getInstance(BusinessRepository.class);
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
		trashRepo.saveNewTrashEntry(product.getKey(), "admin");
		
		trashRepo.deleteTrash(trashRepo.getAll());
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
		
		trashRepo.saveNewTrashEntry(product.getKey(), "admin");
		trashRepo.saveNewTrashEntry(business.getKey(), "admin");
		
		trashRepo.deleteTrash(trashRepo.getAll());
		assertThat(productRepo.getAll().isEmpty(), is(true));
		assertThat(businessRepo.getAll().isEmpty(), is(true));
	}
}
