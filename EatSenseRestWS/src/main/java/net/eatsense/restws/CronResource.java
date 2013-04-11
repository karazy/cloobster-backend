package net.eatsense.restws;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import net.eatsense.auth.AccessToken;
import net.eatsense.auth.AccessTokenRepository;
import net.eatsense.controller.ChannelController;
import net.eatsense.controller.CheckInController;
import net.eatsense.controller.ReportController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.googlecode.objectify.Key;

@Path("/cron")
public class CronResource {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final AccessTokenRepository accessTokenRepo;
	private final Provider<ChannelController> channelCtrlProvider;
	private final Provider<CheckInController> checkInCtrlProvider;
	private final Provider<ReportController> reportProvider;

	@Inject
	public CronResource(AccessTokenRepository accessTokenRepo, Provider<ChannelController> channelCtrlProvider, Provider<CheckInController> chekInCtrlProvider, Provider<ReportController> reportProvider) {
		this.accessTokenRepo = accessTokenRepo;
		this.channelCtrlProvider = channelCtrlProvider;
		this.checkInCtrlProvider = chekInCtrlProvider;
		this.reportProvider = reportProvider;
	}
	
	/**
	 * Delete all expired access tokens from the datastore.
	 * 
	 * @return "OK" if done.
	 */
	@GET
	@Path("cleantokens")
	public Response deleteExpiredTokens() {
		QueryResultIterable<Key<AccessToken>> expiredTokens = this.accessTokenRepo.query().filter("expires <=", new Date()).fetchKeys();
		
		accessTokenRepo.delete(expiredTokens);
		
		return Response.ok().build();
	}
	
	@GET
	@Path("checkcockpits")
	public Response checkCockpitChannels(){
		channelCtrlProvider.get().checkAllOnlineChannels();
		
		return Response.ok().build();
	}
	
	@GET
	@Path("checkinactivecheckins")
	public Response checkInactiveCheckIns() {
		checkInCtrlProvider.get().cleanInactiveCheckInsAndNotify();
		
		return Response.ok().build();
	}
	
	@GET
	@Path("generatedailycounters")
	public Response generateDailyLocationCounterReport() {
		reportProvider.get().generateDailyLocationCounterReport(Optional.<Date>absent());		
		return Response.ok().build();
	}
	
}
