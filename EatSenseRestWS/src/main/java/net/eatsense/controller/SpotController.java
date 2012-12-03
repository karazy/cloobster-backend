package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.validation.Validator;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;

import net.eatsense.domain.Account;
import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.SpotsData;
import net.eatsense.validation.ValidationHelper;

public class SpotController {
	
	public static final String NAME_FORMAT = "%s %03d";
	private final SpotRepository spotRepo;
	private final AreaRepository areaRepo;
	private final ValidationHelper validator;

	@Inject
	public SpotController(SpotRepository spotRepo, ValidationHelper validationHelper, AreaRepository areaRepo) {
		super();
		this.areaRepo = areaRepo;
		this.spotRepo = spotRepo;
		this.validator = validationHelper;
	}
	
	/**
	 * Mass generation of Spot entities.
	 * 
	 * @param spotsData
	 * @return List of auto generated Spots
	 */
	public List<Spot> createSpots(Key<Business> businessKey, SpotsData spotsData) {
		checkNotNull(spotsData);		
		validator.validate(spotsData);
		
		List<Spot> spots = new ArrayList<Spot>();
		
		if( spotsData.getStartNumber() == 0) {
			spotsData.setStartNumber(1);
		}
		
		for (int i = 0; i < spotsData.getCount(); i++) {
			Spot spot = spotRepo.newEntity();			
			spot.setActive(true);
			spot.setArea(areaRepo.getKey(businessKey, spotsData.getAreaId()));
			spot.setBusiness(businessKey);
			spot.setName(String.format(NAME_FORMAT, spotsData.getName(), spotsData.getStartNumber() + i));
			
			spots.add(spot);
		}
		
		spotRepo.saveOrUpdate(spots);
		
		for (Spot spot : spots) {
			spot.generateBarcode();
		}
		
		spotRepo.saveOrUpdate(spots);
			 
		return spots;
	}
	
	/**
	 * @param businessKey
	 * @param spotIds
	 * @return 
	 */
	private List<Key<Spot>> createKeys(Key<Business> businessKey, List<Long> spotIds) {
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
	public List<Spot> updateSpots(Key<Business> businessKey, List<Long> spotIds, boolean active) {
		checkNotNull(businessKey, "businessKey was null");
		checkNotNull(spotIds, "spotIds were null");
		
		if(spotIds.isEmpty()) {
			return Collections.emptyList();
		}
		
		Collection<Spot> spots = spotRepo.getByKeys(createKeys(businessKey, spotIds));
		
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
	public List<Spot> deleteSpots(Key<Business> businessKey, List<Long> spotIds, Account account) {
		checkNotNull(businessKey, "businessKey was null");
		checkNotNull(spotIds, "spotIds were null");
		
		if(spotIds.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<Key<Spot>> spotKeys = createKeys(businessKey, spotIds);
				
		Collection<Spot> spots = spotRepo.getByKeys(spotKeys);
		
		for (Spot spot : spots) {
			spot.setActive(false);
		}
		
		spotRepo.trashEntities(spots, account.getEmail());
				
		return new ArrayList<Spot>(spots);
	}
	
}
