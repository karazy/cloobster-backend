package net.eatsense.controller;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.SpotsData;
import net.eatsense.validation.ValidationHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.googlecode.objectify.Key;

@RunWith(MockitoJUnitRunner.class)
public class SpotControllerTest {
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

	@Before
	public void setUp() throws Exception {
		ctrl = new SpotController(spotRepo, validationHelper, areaRepo);
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
}
