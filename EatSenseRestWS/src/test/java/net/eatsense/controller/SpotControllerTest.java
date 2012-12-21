package net.eatsense.controller;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import net.eatsense.domain.Account;
import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.domain.Spot;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.SpotsData;
import net.eatsense.validation.ValidationHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Query;

@RunWith(MockitoJUnitRunner.class)
public class SpotControllerTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	SpotController ctrl;
	@Mock
	private AreaRepository areaRepo;
	@Mock
	private ValidationHelper validationHelper;
	@Mock
	private SpotRepository spotRepo;
	@Mock
	private Key<Business> businessKey;
	
	@Captor
    private ArgumentCaptor<List<Spot>> spotListCaptor;
	
	@Captor
    private ArgumentCaptor<List<Key<Spot>>> spotKeyListCaptor;
	@Mock
	private Key<Spot> spotKey;

	@Mock
	private EventBus eventBus;

	@Mock
	private Query<Spot> spotQuery;

	@Before
	public void setUp() throws Exception {
		ctrl = new SpotController(spotRepo, validationHelper, areaRepo, eventBus);
	}

	/** 
	 * @return Test Data for Spot creation
	 */
	private SpotsData getTestSpotsData() {

		SpotsData data = new SpotsData();
		data.setAreaId(1);
		data.setCount(20);
		data.setName("Prefix");
		data.setStartNumber(100);

		return data;
	}

	@Test
	public void testCreateSpots() throws Exception {
		SpotsData spotsData = getTestSpotsData();
		
		Spot spot = mock(Spot.class);
		when(spotRepo.newEntity()).thenReturn(spot);
		Key<Area> areaKey = mock(Key.class);
		when(areaRepo.getKey(businessKey, spotsData.getAreaId())).thenReturn(areaKey );
		Area area = mock(Area.class);
		when(areaRepo.getByKey(areaKey)).thenReturn(area );
		when(spotQuery.count()).thenReturn(1);
		when(spotQuery.filter("trash",false)).thenReturn(spotQuery);
		when(spotQuery.ancestor(businessKey)).thenReturn(spotQuery);
		when(spotRepo.query()).thenReturn(spotQuery);
		
		ctrl.createSpots(businessKey, spotsData);
		ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
		InOrder inOrder = Mockito.inOrder(spot, spotRepo);
		verify(spot, times(spotsData.getCount())).setActive(true);
		verify(spot, times(spotsData.getCount())).setArea(areaKey);
		verify(spot, times(spotsData.getCount())).setBusiness(businessKey);
		verify(spot, atLeast(1)).setName(String.format(SpotController.NAME_FORMAT, spotsData.getName(), spotsData.getStartNumber()));
		verify(spot, atLeast(1)).setName(String.format(SpotController.NAME_FORMAT, spotsData.getName(), spotsData.getStartNumber() + spotsData.getCount()-1));
		
		inOrder.verify(spotRepo).saveOrUpdate(spotListCaptor.capture());
		inOrder.verify(spot, times(spotsData.getCount())).generateBarcode();
		inOrder.verify(spotRepo).saveOrUpdate(spotListCaptor.capture());
		
		assertThat(spotListCaptor.getValue().size(), is(spotsData.getCount()));
	}
	
	@Test
	public void testCreateSpotsForWelcomeArea() throws Exception {
		SpotsData spotsData = getTestSpotsData();
		@SuppressWarnings("unchecked")
		Key<Area> areaKey = mock(Key.class);
		when(areaRepo.getKey(businessKey, spotsData.getAreaId())).thenReturn(areaKey );
		Area area = mock(Area.class);
		when(area.isWelcome()).thenReturn(true);
		when(areaRepo.getByKey(areaKey)).thenReturn(area );
		
		when(spotQuery.count()).thenReturn(1);
		when(spotQuery.ancestor(businessKey)).thenReturn(spotQuery);
		when(spotRepo.query()).thenReturn(spotQuery);
	
		thrown.expect(ValidationException.class);
				
		ctrl.createSpots(businessKey, spotsData);
	}

	@Test
	public void testUpdateSpots() throws Exception {
		List<Long> spotIds = Lists.newArrayList(10l, 11l, 12l);
		
		List<Spot> spots = new ArrayList<Spot>();
		
		Spot spot = mock(Spot.class);
		spots.add(spot);
		spots.add(spot);
		spots.add(spot);
		
		when(spotRepo.getKey(businessKey, spotIds.get(0))).thenReturn(spotKey);
		when(spotRepo.getKey(businessKey, spotIds.get(1))).thenReturn(spotKey);
		when(spotRepo.getKey(businessKey, spotIds.get(2))).thenReturn(spotKey);
		when(spotRepo.getByKeys(anyList())).thenReturn(spots);
		
		List<Spot> resultList = ctrl.updateSpots(businessKey, spotIds , true);
		
		verify(spot, times(spotIds.size())).setActive(true);
		verify(spotRepo).saveOrUpdate(spots);
		
		assertThat(resultList, hasItems(spot));
	}
	
	@Test
	public void testDeleteSpots() throws Exception {
		List<Long> spotIds = Lists.newArrayList(10l, 11l, 12l);
		List<Spot> spots = new ArrayList<Spot>();
		
		Spot spot = mock(Spot.class);
		spots.add(spot);
		spots.add(spot);
		spots.add(spot);
		
		when(spotRepo.getKey(businessKey, spotIds.get(0))).thenReturn(spotKey);
		when(spotRepo.getKey(businessKey, spotIds.get(1))).thenReturn(spotKey);
		when(spotRepo.getKey(businessKey, spotIds.get(2))).thenReturn(spotKey);
		when(spotRepo.getByKeys(anyList())).thenReturn(spots);
		
		Account account = mock(Account.class);
		List<Spot> resultList = ctrl.deleteSpots(businessKey, spotIds, account );
		
		verify(spotRepo).trashEntities(spotListCaptor.capture(), anyString());
		List<Spot> deleteList = spotListCaptor.getValue();
		assertThat(deleteList.size(), is(3));
		assertThat(deleteList, hasItems(spot));
		assertThat(resultList, hasItems(spot));
	}
}
