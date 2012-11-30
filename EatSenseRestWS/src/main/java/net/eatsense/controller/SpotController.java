package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Validator;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;

import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.SpotRepository;
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
		
		if( spotsData.startNumber == 0) {
			spotsData.startNumber = 1;
		}
		
		for (int i = 0; i < spotsData.count; i++) {
			Spot spot = spotRepo.newEntity();			
			spot.setActive(true);
			spot.setArea(areaRepo.getKey(businessKey, spotsData.getAreaId()));
			spot.setBusiness(businessKey);
			spot.setName(String.format(NAME_FORMAT, spotsData.getName(), spotsData.startNumber + i));
			
			spots.add(spot);
		}
		
		spotRepo.saveOrUpdate(spots);
		
		for (Spot spot : spots) {
			spot.generateBarcode();
		}
		
		spotRepo.saveOrUpdate(spots);
			 
		return spots;
	}
	
	
}
