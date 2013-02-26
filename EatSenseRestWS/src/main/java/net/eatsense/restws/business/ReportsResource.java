package net.eatsense.restws.business;

import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import net.eatsense.controller.ReportController;
import net.eatsense.domain.Business;
import net.eatsense.representation.CounterReportDTO;

public class ReportsResource {
	private Business location;
	private final ReportController reportController;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Inject
	public ReportsResource(ReportController reportController) {
		super();
		this.reportController = reportController;
	}

	public void setLocation(Business location) {
		this.location = location;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<CounterReportDTO> getKPIReport(@QueryParam("kpi") String kpi, @QueryParam("areaId") long areaId, @QueryParam("fromDate") long fromTimeStamp, @QueryParam("toDate") long toTimeStamp) {
		return reportController.getReportForLocationAreaAndDateRange(location, kpi, areaId, new Date(fromTimeStamp), new Date(toTimeStamp));
	}
}
