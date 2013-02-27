package net.eatsense.documents;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import net.eatsense.counter.Counter;
import net.eatsense.counter.CounterRepository;
import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.domain.Document;
import net.eatsense.exceptions.ServiceException;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.LocationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class CounterReportXLSGenerator extends AbstractDocumentGenerator {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final ByteArrayOutputStream byteOutput;
	private final CounterRepository counterRepo;
	private final AreaRepository areaRepo;
	private final LocationRepository locationRepo;

	@Inject
	public CounterReportXLSGenerator(CounterRepository counterRepo, LocationRepository locationRepo, AreaRepository areaRepo) {
		this.counterRepo = counterRepo;
		this.locationRepo = locationRepo;
		this.areaRepo = areaRepo;
		byteOutput = new ByteArrayOutputStream();
	}
	
	@Override
	public String getMimeType() {
		return "application/vnd.ms-excel";
	}

	@Override
	public byte[] generate( Document document) {
		Collection<Counter> entities = counterRepo.getByKeys(document.getEntityNames());
		
		if(entities.isEmpty()) {
			logger.error("No entities found for report generation.");
			throw new ServiceException("No entities found for report generation.");
		}
		
		HashMap<Long, Area> areaMap = Maps.newHashMap();
		Business location = locationRepo.getByKey(document.getBusiness());
		
		try {
			WritableWorkbook workbook = Workbook.createWorkbook(byteOutput);
			WritableSheet sheet = workbook.createSheet("cloobster Report", 0);
			Label label = new Label(0, 0, "KPI");
			Label label2 = new Label(1, 0, "Location");
			Label label3 = new Label(2, 0, "Area");
			Label label4 = new Label(3, 0, "Anzahl");
			Label label5 = new Label(4, 0, "Tag");
			Label label6 = new Label(5, 0, "Monat");
			Label label7 = new Label(6, 0, "Jahr");
			
			sheet.addCell(label);
			sheet.addCell(label2);
			sheet.addCell(label3);
			sheet.addCell(label4);
			sheet.addCell(label5);
			sheet.addCell(label6);
			sheet.addCell(label7);
			
			int row = 1;
			for (Counter counter : entities) {
				Area area = areaMap.get(counter.getAreaId());
				if(area == null) {
					area = areaRepo.getById(document.getBusiness(), counter.getLocationId());
				}
				
				addCells(sheet, row, counter, location.getName(), area.getName());
				
				row++;
			}
			
			workbook.write();
			workbook.close();
			
		} catch (Exception e) {
			logger.error("Unable to create XLS output", e);
			throw new ServiceException("Error generating XLS output.");
		}
		
		
		
		return byteOutput.toByteArray();
	}

	private void addCells(WritableSheet sheet, int row, Counter counter,
			String locationName, String areaName) throws WriteException {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(counter.getPeriod());
		
		Label KPICell = new Label(0, row, counter.getName());
		sheet.addCell(KPICell);
		Label locationCell = new Label(1, row, locationName);
		sheet.addCell(locationCell);
		Label areaCell = new Label(2, row, areaName);
		sheet.addCell(areaCell);
		Number valueCell = new Number(3, row, counter.getCount());
		sheet.addCell(valueCell);
		Number dayCell = new Number(4, row, calendar.get(Calendar.DAY_OF_MONTH));
		sheet.addCell(dayCell);
		Number monthCell = new Number(5, row, calendar.get(Calendar.MONTH));
		sheet.addCell(monthCell);
		Number yearCell = new Number(6, row, calendar.get(Calendar.YEAR));
		sheet.addCell(yearCell);
	}

}
