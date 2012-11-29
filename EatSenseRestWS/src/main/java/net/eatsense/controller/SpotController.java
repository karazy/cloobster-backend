package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Validator;

import net.eatsense.domain.Area;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.validation.ValidationHelper;

public class SpotController {
	
	private final SpotRepository spotRepo;
	private final ValidationHelper validator;

	public SpotController(SpotRepository spotRepo, ValidationHelper validationHelper) {
		super();
		this.spotRepo = spotRepo;
		this.validator = validationHelper;
	}
	
	/**
	 * Mass generation of Spot entities.
	 * 
	 * @param spotsData
	 * @return List of auto generated Spots
	 */
	public List<Spot> createSpots(SpotsData spotsData) {
		checkNotNull(spotsData);		
		validator.validate(spotsData);
		
		List<Spot> spots = new ArrayList<Spot>();
		
		for (int i = 0; i < spotsData.count; i++) {
			Spot spot = spotRepo.newEntity();			
			spot.setActive(true);
			//TODO call setter
			
			spots.add(spot);
		}
		
		spotRepo.saveOrUpdate(spots);
		 
		return spots;
	}
}
