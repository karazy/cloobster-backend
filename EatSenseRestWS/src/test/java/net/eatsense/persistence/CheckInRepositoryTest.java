package net.eatsense.persistence;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import net.eatsense.cache.EntityKeyCache;
import net.eatsense.domain.CheckIn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Query;

@RunWith(MockitoJUnitRunner.class)
public class CheckInRepositoryTest {
	 private final LocalServiceTestHelper helper =
		        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	@Mock
	private EntityKeyCache keyCache;
	private CheckInRepository repo;
	@Mock
	private Objectify ofy;
	@Mock
	private Key<CheckIn> key;
	@Mock
	private Query<CheckIn> query;
	@Mock
	private CheckIn checkIn;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		
		repo = spy(new CheckInRepository(keyCache));
		doReturn(ofy).when(repo).ofy();
	}

	@Test
	public void testGetByUserIdCached() {
		String userId = "testid";
		when(keyCache.get(userId, CheckIn.class)).thenReturn(key );
		repo.getByUserId(userId);
		
		verify(ofy).find(key);
	}
	
	@Test
	public void testGetByUserIdNotCached() {
		String userId = "testid";
		when(query.get()).thenReturn(checkIn);
		when(query.filter("userId", userId)).thenReturn(query);
		when(ofy.query(CheckIn.class)).thenReturn(query);
		
		
		assertThat(repo.getByUserId(userId), is(checkIn));
		
		verify(keyCache).get(userId, CheckIn.class);
	}

	@Test
	public void testGetByPropertyStringObject() {
		String userId = "testid";
		when(query.get()).thenReturn(checkIn);
		when(query.filter("userId", userId)).thenReturn(query);
		when(ofy.query(CheckIn.class)).thenReturn(query);
		
		
		assertThat(repo.getByProperty("userId", userId), is(checkIn));
		
		verify(repo).getByUserId(userId);
	}

	@Test
	public void testSaveOrUpdateCheckIn() {
		String userId = "testid";
		when(checkIn.getUserId()).thenReturn(userId);
		
		repo.saveOrUpdate(checkIn);
	}

}
