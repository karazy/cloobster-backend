package net.eatsense.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.eatsense.domain.Business;
import net.eatsense.domain.DashboardConfiguration;
import net.eatsense.domain.DashboardItem;
import net.eatsense.exceptions.DataConflictException;
import net.eatsense.persistence.DashBoarditemRepository;
import net.eatsense.representation.DashboardConfigDTO;
import net.eatsense.representation.DashboardItemDTO;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.googlecode.objectify.Key;

@RunWith(MockitoJUnitRunner.class)
public class DashboardControllerTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private DashboardController ctrl;
	
	@Mock
	private DashBoarditemRepository itemRepo;
	@Mock
	private Key<Business> locationKey;
	@Mock
	private DashboardConfiguration config;

	@Mock
	private Key<DashboardItem> itemKey;

	@Mock
	private List<Key<DashboardItem>> itemKeys;
	

	@Before
	public void setUp() throws Exception {
		ctrl =  new DashboardController(itemRepo);
		when(itemRepo.getConfiguration(locationKey)).thenReturn(config);
		when(config.getItems()).thenReturn(itemKeys );
	}

	private DashboardItemDTO getTestItemData() {
		DashboardItemDTO data = new DashboardItemDTO();
		data.setEntityIds(Arrays.asList(1l,2l,3l));
		data.setType("testtype");
		return data ;
	}
	
	@Test
	public void testCreateAndSave() throws Exception {
		DashboardItem newItem = new DashboardItem();
		when(itemRepo.newEntity()).thenReturn(newItem );
		when(itemRepo.saveOrUpdate(newItem)).thenReturn(itemKey);
		DashboardItemDTO itemData = getTestItemData();
		
		DashboardItem item = ctrl.createAndSave(locationKey, itemData );
		
		verify(itemKeys).add(itemKey);
		verify(itemRepo).saveOrUpdateConfiguration(config);
		
		assertThat(item, is(newItem));
		assertThat(item.getLocation(), is(locationKey));
	}
	
	@Test
	public void testCreateAndSaveMaxItems() throws Exception {
		when(itemKeys.size()).thenReturn(10);
		
		thrown.expect(DataConflictException.class);
		
		ctrl.createAndSave(locationKey, getTestItemData());
	}
	
	@Test
	public void testGetAndUpdateConfig() throws Exception {
		
		DashboardConfigDTO configData = new DashboardConfigDTO();
		List<Long> itemIds = Arrays.asList(1l,2l,3l);
		configData.setItemIds(itemIds );
		
		when(itemRepo.getKeys(locationKey, itemIds)).thenReturn(itemKeys);
		
		ctrl.getAndUpdateConfig(locationKey, configData);
		
		
		verify(config).setItems(itemKeys);
		verify(itemRepo).saveOrUpdateConfiguration(config);
	}
	
	@Test
	public void testUpdate() throws Exception {
		
		DashboardItemDTO itemData = getTestItemData();
		DashboardItem item = mock(DashboardItem.class);
		
		ctrl.update(item , itemData );
		
		verify(item).setType(itemData.getType());
		verify(item).setEntityIds(itemData.getEntityIds());
		verify(itemRepo).saveOrUpdate(item);
	}

	@Test
	public void testDelete() throws Exception {
		long itemId = 1;
		when(itemRepo.getKey(locationKey, itemId )).thenReturn(itemKey);
		
		ctrl.delete(locationKey, itemId);
		
		verify(itemRepo).delete(itemKey);
	}
}
