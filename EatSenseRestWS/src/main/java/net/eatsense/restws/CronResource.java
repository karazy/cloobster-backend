package net.eatsense.restws;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.CheckInStatus;
import net.eatsense.domain.NicknameAdjective;
import net.eatsense.domain.NicknameNoun;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.NicknameAdjectiveRepository;
import net.eatsense.persistence.NicknameNounRepository;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Query;

@Path("/cron")
public class CronResource {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private CheckInRepository checkInRepo;

	@Inject
	public CronResource(CheckInRepository cr) {
		this.checkInRepo = cr;

	}
	
	/**
	 * Removes checkin intents older than 10 minutes
	 */
	@GET
	@Path("cleanintents")
	public void cleanIntents() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, -10);			
		logger.info("JOB: Cleaning old checkin intents ...");
		
		Iterable<Key<CheckIn>> checkins = checkInRepo.getOfy().query(CheckIn.class).filter("status", CheckInStatus.INTENT).filter("checkInTime <", calendar.getTime())
				.fetchKeys();
		
		checkInRepo.getOfy().delete(checkins);
	}
}
