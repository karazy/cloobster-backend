package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.eatsense.domain.Account;
import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.domain.Spot;
import net.eatsense.event.DeleteSpotEvent;
import net.eatsense.event.NewSpotEvent;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.OfyService;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.SpotsData;
import net.eatsense.validation.ValidationHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.KeyRange;
import com.googlecode.objectify.NotFoundException;

public class SpotController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String NAME_FORMAT = "%s %03d";
	private final SpotRepository spotRepo;
	private final AreaRepository areaRepo;
	private final ValidationHelper validator;

	private final EventBus eventBus;

	private final OfyService ofyService;

	@Inject
	public SpotController(SpotRepository spotRepo,
			ValidationHelper validationHelper, AreaRepository areaRepo,
			OfyService ofyService, EventBus eventBus) {
		super();
		this.areaRepo = areaRepo;
		this.spotRepo = spotRepo;
		this.validator = validationHelper;
		this.ofyService = ofyService;
		this.eventBus = eventBus;
	}

	/**
	 * Mass generation of Spot entities.
	 * 
	 * @param spotsData
	 * @return List of auto generated Spots
	 */
	public List<Spot> createSpots(Key<Business> locationKey, SpotsData spotsData) {
		checkNotNull(spotsData);		
		validator.validate(spotsData);
		
		List<Spot> spots = new ArrayList<Spot>();
		
		if( spotsData.getStartNumber() == 0) {
			spotsData.setStartNumber(1);
		}
		
		Key<Area> areaKey = areaRepo.getKey(locationKey, spotsData.getAreaId());
		try {
			Area area = areaRepo.getByKey(areaKey);
			if(area.isWelcome()) {
				throw new ValidationException("Unable to create new Spots at Welcome area");
			}
		} catch (NotFoundException e) {
			logger.error("Unable to create Spots for unknown Area. key={}", areaKey);
			throw new ValidationException("No Area found with id="+areaKey.getId());
		}

		int spotCount = countSpots(locationKey);
		KeyRange<Spot> idRange = ofyService.factory().allocateIds(locationKey, Spot.class, spotsData.getCount());
		
		int i = 0;
		for (Key<Spot> key : idRange) {
			Spot spot = spotRepo.newEntity();
			
			spot.setActive(true);
			spot.setArea(areaKey);
			spot.setId(key.getId());
			spot.setBusiness(locationKey);
			spot.setName(String.format(NAME_FORMAT, spotsData.getName(), spotsData.getStartNumber() + i));
			spot.generateBarcode();
			
			spots.add(spot);
			i++;
		}
		
		spotRepo.saveOrUpdate(spots);
		
		eventBus.post(new NewSpotEvent(locationKey, null, spotCount + spotsData.getCount(), true));
			 
		return spots;
	}

	/**
	 * @param businessKey
	 * @param spotIds
	 * @return
	 */
	private List<Key<Spot>> createKeys(Key<Business> businessKey,
			List<Long> spotIds) {
		ArrayList<Key<Spot>> spotKeys = new ArrayList<Key<Spot>>();

		for (Long spotId : spotIds) {
			spotKeys.add(spotRepo.getKey(businessKey, spotId));
		}

		return spotKeys;
	}

	/**
	 * @param businessKey
	 * @param spotsData
	 * @return
	 */
	public List<Spot> updateSpots(Key<Business> businessKey,
			List<Long> spotIds, boolean active) {
		checkNotNull(businessKey, "businessKey was null");
		checkNotNull(spotIds, "spotIds were null");

		if (spotIds.isEmpty()) {
			return Collections.emptyList();
		}

		Collection<Spot> spots = spotRepo.getByKeys(createKeys(businessKey,
				spotIds));

		// De-/Activate all spots
		for (Spot spot : spots) {
			spot.setActive(active);
		}

		spotRepo.saveOrUpdate(spots);

		return new ArrayList<Spot>(spots);
	}

	/**
	 * Delete given spots.
	 * 
	 * @param businessKey
	 * @param spotIds
	 * @return
	 */
	public List<Spot> deleteSpots(Key<Business> businessKey,
			List<Long> spotIds, Account account) {
		checkNotNull(businessKey, "businessKey was null");
		checkNotNull(spotIds, "spotIds were null");

		if (spotIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<Key<Spot>> spotKeys = createKeys(businessKey, spotIds);

		Collection<Spot> spots = spotRepo.getByKeys(spotKeys);
		int spotCount = countSpots(businessKey);
		int deletedSpots = 0;
		for (Iterator<Spot> iterator = spots.iterator(); iterator.hasNext();) {
			Spot spot = iterator.next();
			if (spot.isWelcome() || spot.isMaster()) {
				logger.warn("Filtered master/welcome Spot from deletion request.");
				iterator.remove();
			} else {
				deletedSpots++;
				spot.setActive(false);
			}
		}

		spotRepo.trashEntities(spots, account.getEmail());
		eventBus.post(new DeleteSpotEvent(businessKey, null, spotCount
				- deletedSpots, true));

		return new ArrayList<Spot>(spots);
	}

	/**
	 * @param locationKey
	 * @return
	 */
	private int countSpots(Key<Business> locationKey) {
		return spotRepo.query().ancestor(locationKey).filter("trash", false)
				.count();
	}
}
