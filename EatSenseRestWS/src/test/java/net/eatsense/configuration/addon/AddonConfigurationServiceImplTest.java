package net.eatsense.configuration.addon;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import net.eatsense.ObjectifyModule;

import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.googlecode.objectify.cache.CachingDatastoreServiceFactory;

public class AddonConfigurationServiceImplTest {
	private final LocalServiceTestHelper helper =
		        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(), new LocalMemcacheServiceTestConfig());
	
	private AddonConfigurationServiceImpl service;

	private String testname;

	private Map<String, String> testConfigMap;

	private Key parentKey;

	private AddonConfiguration testConfig;
	
	@Before
	public void setUp() throws Exception {
		helper.setUp();
		Injector injector = Guice.createInjector(new ObjectifyModule());
		service = injector.getInstance(AddonConfigurationServiceImpl.class);
		testname = "testaddon";
		testConfigMap = Maps.newHashMap();
		testConfigMap.put("param1", "test1");
		testConfigMap.put("param2", "test2");
		DatastoreService datastore = CachingDatastoreServiceFactory.getDatastoreService();
		
		Entity parentEntity = new Entity("Parent", "test");
		parentEntity.setProperty("test", "test");
		parentKey = datastore.put(parentEntity);
		
		
		Entity entity = new Entity(AddonConfiguration.KIND,testname);
		entity.setUnindexedProperty("param1", "test1");
		entity.setUnindexedProperty("param2", "test2");
		datastore.put(entity);
		
		entity = new Entity(AddonConfiguration.KIND,testname,parentKey);
		entity.setUnindexedProperty("param1", "test1");
		entity.setUnindexedProperty("param2", "test2");
		
		testConfig = new AddonConfiguration(testname,null, testConfigMap);
		
		datastore.put(entity);
	}

	@Test
	public void testGetString() {
		AddonConfiguration testConfig = service.get(testname);
		
		assertThat(testConfig.getAddonName(), is(testname));
		assertThat(testConfig.getConfigMap().get("param1"), is("test1"));
		assertThat(testConfig.getConfigMap().get("param2"), is("test2"));
	}

	@Test
	public void testGetStringKey() {
		AddonConfiguration testConfig = service.get(testname, parentKey);
		
		assertThat(testConfig.getParent(), is(parentKey));
		assertThat(testConfig.getAddonName(), is(testname));
		assertThat(testConfig.getConfigMap().get("param1"), is("test1"));
		assertThat(testConfig.getConfigMap().get("param2"), is("test2"));
	}

	@Test
	public void testGetAll() {
	}

	@Test
	public void testPut() {
		testConfig.getConfigMap().put("param3", "test3");
		service.put(testConfig);
		AddonConfiguration result = service.get(testConfig.getAddonName());
		
		assertThat(result.getConfigMap().get("param3"), is("test3"));
	}

	@Test
	public void testCreate() {
		AddonConfiguration result = service.create(testname, testConfigMap);
		
		assertThat(result.getAddonName(), is(testname));
		assertThat(result.getConfigMap(), is(testConfigMap));
	}
	
	@Test
	public void testCreateWithParent() {
		Key testParent = KeyFactory.createKey("Test", 1);
		AddonConfiguration result = service.create(testname, testParent, testConfigMap);
		
		assertThat(result.getParent(), is(testParent));
		assertThat(result.getAddonName(), is(testname));
		assertThat(result.getConfigMap(), is(testConfigMap));
	}

}
