package net.eatsense.documents;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import net.eatsense.counter.Counter;
import net.eatsense.counter.CounterRepository;
import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.domain.Document;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.LocationRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.appengine.api.appidentity.dev.LocalAppIdentityService;
import com.googlecode.objectify.Key;

@RunWith(MockitoJUnitRunner.class)
public class CounterReportXLSGeneratorTest {
	
	private CounterReportXLSGenerator ctrl;
	@Mock
	private AreaRepository areaRepo;
	@Mock
	private LocationRepository locationRepo;
	@Mock
	private CounterRepository counterRepo;
	@Mock
	private Key<Business> locationKey;

	@Before
	public void setUp() throws Exception {
		ctrl = spy(new CounterReportXLSGenerator(counterRepo, locationRepo, areaRepo));
	}
	
	@Test
	public void testGenerate() throws Exception {
		List<String> ids = Arrays.asList("test1","test2","test3");
		
		Counter counter = mock(Counter.class);
		Collection<Counter> counters = Arrays.asList(counter , counter,counter);
		when(counterRepo.getByKeys(ids)).thenReturn(counters );
		String counterName = "kpi";
		when(counter.getName()).thenReturn(counterName);
		long value = 10l;
		when(counter.getCount()).thenReturn(value);
		
		Document document = mock(Document.class);
		long areaId = 1l;
		when(document.getEntityNames()).thenReturn(ids);
		when(document.getBusiness()).thenReturn(locationKey);
		when(counter.getAreaId()).thenReturn(areaId);
		String locationName = "location";
		Business location = mock(Business.class);
		when(location.getName()).thenReturn(locationName);
		
		when(locationRepo.getByKey(locationKey)).thenReturn(location );
		
		Date period = new Date();
		when(counter.getPeriod()).thenReturn(period);
		
		
		Area area = mock(Area.class);
		String areaName = "area";
		when(area.getName()).thenReturn(areaName);
		when(areaRepo.getById(locationKey, areaId )).thenReturn(area );
		
		WritableWorkbook workbook = mock(WritableWorkbook.class);
		doReturn(workbook).when(ctrl).makeWorkbook(any(OutputStream.class));
		WritableSheet sheet = mock(WritableSheet.class);
		when(workbook.createSheet("cloobster Report", 0)).thenReturn(sheet );
		
		Label kpiLabel = mock(Label.class);
		doReturn(kpiLabel).when(ctrl).makeLabel(0, 0, "KPI");
		Label locLabel = mock(Label.class);
		doReturn(locLabel).when(ctrl).makeLabel(1, 0, "Location");
		Label areaLabel = mock(Label.class);
		doReturn(areaLabel).when(ctrl).makeLabel(2, 0, "Area");
		Label countLabel = mock(Label.class);
		doReturn(countLabel).when(ctrl).makeLabel(3, 0, "Anzahl");
		Label dayLabel = mock(Label.class);
		doReturn(dayLabel).when(ctrl).makeLabel(4, 0, "Tag");
		Label monthLabel = mock(Label.class);
		doReturn(monthLabel).when(ctrl).makeLabel(5, 0, "Monat");
		Label yearLabel = mock(Label.class);
		doReturn(yearLabel).when(ctrl).makeLabel(6, 0, "Jahr");
		
		Label counterKpiLabel = mock(Label.class);
		doReturn(counterKpiLabel).when(ctrl).makeLabel(0, 1, counterName);
		doReturn(counterKpiLabel).when(ctrl).makeLabel(0, 2, counterName);
		doReturn(counterKpiLabel).when(ctrl).makeLabel(0, 3, counterName);
		
		Label locationCell = mock(Label.class);
		doReturn(locationCell).when(ctrl).makeLabel(1, 1, locationName );
		doReturn(locationCell).when(ctrl).makeLabel(1, 2, locationName);
		doReturn(locationCell).when(ctrl).makeLabel(1, 3, locationName);
		
		Label areaCell = mock(Label.class);
		doReturn(areaCell).when(ctrl).makeLabel(2, 1, areaName);
		doReturn(areaCell).when(ctrl).makeLabel(2, 2, areaName);
		doReturn(areaCell).when(ctrl).makeLabel(2, 3, areaName);
		
		jxl.write.Number numberCell = mock(Number.class);
		doReturn(numberCell).when(ctrl).makeNumber(3, 1, value);
		doReturn(numberCell).when(ctrl).makeNumber(3, 2, value);
		doReturn(numberCell).when(ctrl).makeNumber(3, 3, value);
		
		Calendar calendar = Calendar.getInstance();
		jxl.write.Number dayCell = mock(Number.class);
		doReturn(dayCell).when(ctrl).makeNumber(4, 1, calendar.get(Calendar.DAY_OF_MONTH));
		doReturn(dayCell).when(ctrl).makeNumber(4, 2, calendar.get(Calendar.DAY_OF_MONTH));
		doReturn(dayCell).when(ctrl).makeNumber(4, 3, calendar.get(Calendar.DAY_OF_MONTH));
		
		jxl.write.Number monthCell = mock(Number.class);
		doReturn(monthCell).when(ctrl).makeNumber(5, 1, calendar.get(Calendar.MONTH) + 1 );
		doReturn(monthCell).when(ctrl).makeNumber(5, 2, calendar.get(Calendar.MONTH) + 1 );
		doReturn(monthCell).when(ctrl).makeNumber(5, 3, calendar.get(Calendar.MONTH) + 1 );
		
		jxl.write.Number yearCell = mock(Number.class);
		doReturn(yearCell).when(ctrl).makeNumber(6, 1, calendar.get(Calendar.YEAR) );
		doReturn(yearCell).when(ctrl).makeNumber(6, 2, calendar.get(Calendar.YEAR) );
		doReturn(yearCell).when(ctrl).makeNumber(6, 3, calendar.get(Calendar.YEAR) );
		
		ctrl.generate(document );
		
		verify(sheet).addCell(yearLabel);
		verify(sheet).addCell(monthLabel);
		verify(sheet).addCell(dayLabel);
		verify(sheet).addCell(countLabel);
		verify(sheet).addCell(areaLabel);
		verify(sheet).addCell(locLabel);
		verify(sheet).addCell(kpiLabel);
		
		verify(sheet,times(3)).addCell(counterKpiLabel);
		verify(sheet, times(3)).addCell(locationCell);
		verify(sheet, times(3)).addCell(areaCell);
		verify(sheet, times(3)).addCell(numberCell);
		verify(sheet, times(3)).addCell(dayCell);
		verify(sheet, times(3)).addCell(monthCell);
		verify(sheet, times(3)).addCell(yearCell);
	}
}
