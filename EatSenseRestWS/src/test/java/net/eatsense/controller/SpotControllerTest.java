package net.eatsense.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.eatsense.domain.Business;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.validation.ValidationHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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

	@Before
	public void setUp() throws Exception {
		ctrl = new SpotController(spotRepo, validationHelper, areaRepo);
	}
	
	@Test
	public void testCreateSpots() throws Exception {
		SpotsData spotsData = getTestSpotsData();
		
		Spot spot = mock(Spot.class);
		when(spotRepo.newEntity()).thenReturn(spot );
		
		ctrl.createSpots(businessKey, spotsData);
		
	}

	private SpotsData getTestSpotsData() {
		
		SpotsData data =  new SpotsData();
		data.setAreaId(1);
		data.setCount(20);
		data.setName("Prefix");
		data.setStartNumber(3);
		
		return data ;
	}
}
