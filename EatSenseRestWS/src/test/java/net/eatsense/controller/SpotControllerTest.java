package net.eatsense.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.SpotRepository;
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

	@Before
	public void setUp() throws Exception {
		ctrl = new SpotController(spotRepo, validationHelper, areaRepo);
	}

	@Test
	public void testCreateSpots() throws Exception {
		SpotsData spotsData = getTestSpotsData();
		
		Spot spot = mock(Spot.class);
		when(spotRepo.newEntity()).thenReturn(spot);
		Key<Area> areaKey = mock(Key.class);
		when(areaRepo.getKey(businessKey, spotsData.areaId)).thenReturn(areaKey );
		
		ctrl.createSpots(businessKey, spotsData);
		ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
		InOrder inOrder = Mockito.inOrder(spot, spotRepo);
		verify(spot, times(spotsData.count)).setActive(true);
		verify(spot, times(spotsData.count)).setArea(areaKey);
		verify(spot, times(spotsData.count)).setBusiness(businessKey);
		verify(spot, atLeast(1)).setName(String.format(SpotController.NAME_FORMAT, spotsData.name, spotsData.startNumber));
		verify(spot, atLeast(1)).setName(String.format(SpotController.NAME_FORMAT, spotsData.name, spotsData.startNumber + spotsData.count-1));
		
		inOrder.verify(spotRepo).saveOrUpdate(spotListCaptor.capture());
		inOrder.verify(spot, times(spotsData.count)).generateBarcode();
		inOrder.verify(spotRepo).saveOrUpdate(spotListCaptor.capture());
		
		assertThat(spotListCaptor.getValue().size(), is(spotsData.count));
	}

	private SpotsData getTestSpotsData() {

		SpotsData data = new SpotsData();
		data.setAreaId(1);
		data.setCount(20);
		data.setName("Prefix");
		data.setStartNumber(3);

		return data;
	}
}
