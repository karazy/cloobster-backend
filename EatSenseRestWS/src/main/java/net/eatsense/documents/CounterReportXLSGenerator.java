package net.eatsense.documents;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
	
	/**
	 * @param column
	 * @param row
	 * @param text
	 * @return
	 */
	Label makeLabel(int column, int row, String text) {
		return new Label(column, row, text);
	}
	
	/**
	 * @param column
	 * @param row
	 * @param value
	 * @return
	 */
	Number makeNumber(int column, int row, double value) {
		return new Number(column, row, value);
	}
	
	WritableWorkbook makeWorkbook(OutputStream outputStream) throws IOException {
		return Workbook.createWorkbook(outputStream);
	}

	/* (non-Javadoc)
	 * @see net.eatsense.documents.AbstractDocumentGenerator#generate(net.eatsense.domain.Document)
	 */
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
			WritableWorkbook workbook = makeWorkbook(byteOutput);
			WritableSheet sheet = workbook.createSheet("cloobster Report", 0);
			Label label = makeLabel(0, 0, "KPI");
			Label label2 = makeLabel(1, 0, "Location");
			Label label3 = makeLabel(2, 0, "Area");
			Label label4 = makeLabel(3, 0, "Anzahl");
			Label label5 = makeLabel(4, 0, "Tag");
			Label label6 = makeLabel(5, 0, "Monat");
			Label label7 = makeLabel(6, 0, "Jahr");
			
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
					area = areaRepo.getById(document.getBusiness(), counter.getAreaId());
					areaMap.put(area.getId(), area);
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
		
		Label KPICell = makeLabel(0, row, counter.getName());
		sheet.addCell(KPICell);
		Label locationCell = makeLabel(1, row, locationName);
		sheet.addCell(locationCell);
		Label areaCell = makeLabel(2, row, areaName);
		sheet.addCell(areaCell);
		double value;
		if(counter.getName().equals("turnover") && counter.getCount() != 0) {
			value = counter.getCount() / 100d;
		}
		else {
			value = counter.getCount();
		}
		Number valueCell = makeNumber(3, row, value);
		sheet.addCell(valueCell);
		Number dayCell = makeNumber(4, row, calendar.get(Calendar.DAY_OF_MONTH));
		sheet.addCell(dayCell);
		Number monthCell = makeNumber(5, row, calendar.get(Calendar.MONTH)+1);
		sheet.addCell(monthCell);
		Number yearCell = makeNumber(6, row, calendar.get(Calendar.YEAR));
		sheet.addCell(yearCell);
	}
}
