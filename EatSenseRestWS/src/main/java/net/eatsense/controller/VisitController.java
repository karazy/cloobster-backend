package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import net.eatsense.controller.ImageController.UpdateImagesResult;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.Visit;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.LocationRepository;
import net.eatsense.persistence.VisitRepository;
import net.eatsense.representation.ToVisitDTO;
import net.eatsense.validation.ValidationHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.google.appengine.api.datastore.GeoPt;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.googlecode.objectify.NotFoundException;

/**
 * Manages Visit logic and processes, like retrieving, creating and updating of Visit entities in the datastore.
 * @author Nils Weiher
 *
 */
public class VisitController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final VisitRepository visitRepo;
	private final LocationRepository locationRepo;
	private final ValidationHelper validator;
	private final ImageController imageCtrl;
	
	@Inject
	public VisitController(VisitRepository allVisits, LocationRepository allLocations, ValidationHelper validator, ImageController imageCtrl) {
		super();
		this.visitRepo = allVisits;
		this.locationRepo = allLocations;
		this.validator = validator;
		this.imageCtrl = imageCtrl;
	}


	/**
	 * Create Visit entity, fill with the supplied data and save in the datastore.
	 * 
	 * @param account Must be valid user account;
	 * @param visitData
	 * @return New {@link Visit} with id field set.
	 */
	public Visit createVisit(Account account, ToVisitDTO visitData) {
		checkNotNull(account, "account was null");
		checkNotNull(visitData , "visitData was null");
		
		Visit visit = visitRepo.newEntity();
		visit.setCreatedOn(new Date());
		visit.setAccount(account.getKey());
		
		return updateVisit(account, visit, visitData);
	}
	
	/**
	 * Update fields of a Visit entity with the supplied data.
	 * Save the entity if dirty.
	 * 
	 * @param visit
	 * @param visitData
	 * @return
	 */
	public Visit updateVisit(Account account, Visit visit, ToVisitDTO visitData) {
		checkNotNull(visit, "visit was null");
		checkNotNull(visitData, "visitData was null");
		
		visit.setComment(visitData.getComment());
		
		if(visitData.getGeoLat() != null && visitData.getGeoLong() != null) {
			try {
				visit.setGeoLocation(new GeoPt(visitData.getGeoLat(), visitData.getGeoLong()));
			} catch (IllegalArgumentException e) {
				throw new ValidationException("Illegal value for geoLat or geoLong.");
			}
		}
		
		if(visitData.getLocationId() != null) {
			// Link the visit to a cloobster location.
			try {
				visit.setLocation(locationRepo.getKey(visitData.getLocationId()));
				Business location = locationRepo.getByKey(visit.getLocation());
				visit.setLocationName(location.getName());
			} catch (NotFoundException e) {
				throw new ValidationException("Unknown locationId");				
			}
		}
		else {
			// Save an app user supplied location name
			visit.setLocationName(visitData.getLocationName());
			if(Strings.isNullOrEmpty(visit.getLocationName())) {
				throw new ValidationException("locationName was empty");
			}
		}
		
		if(visitData.getImage() != null) {
			UpdateImagesResult result = imageCtrl.updateImages(account, visit.getImages(), visitData.getImage());
			if(result.isDirty()) {
				visit.setImages(result.getImages());
				visit.setDirty(true);
			}
		}
				
		visit.setLocationCity(visitData.getLocationCity());
		visit.setLocationRefId(visitData.getLocationRefId());
		visit.setVisitDate(visitData.getVisitDate());
		
		if(visit.isDirty())
			visitRepo.saveOrUpdate(visit);
		
		return visit;
	}
	
	/**
	 * @param account
	 * @param id
	 * @return
	 */
	public Visit getVisit(Account account, long id) {
		try {
			return visitRepo.getById(account.getKey(), id);
		} catch (NotFoundException e) {
			throw new net.eatsense.exceptions.NotFoundException();
		}
	}
	
	/**
	 * Shortcut method to retrieve and update a Visit entity from the store.
	 * 
	 * @param account
	 * @param id
	 * @param visitData
	 * @return
	 */
	public Visit getAndUpdateVisit(Account account, long id, ToVisitDTO visitData) {
		return updateVisit(account, getVisit(account, id), visitData);
	}
	
	/**
	 * Return Visit entities of the supplied Account, sorted by creation date.
	 * 
	 * @param account
	 * @param start 
	 * @param limit 
	 * @return
	 */
	public Iterable<Visit> getVisitsSorted(Account account, int start, int limit) {
		checkNotNull(account, "account was null");
		
		return visitRepo.belongingToAccountSortedByVisitAndCreationDate(account.getKey(), start, limit);
	}
	
	/**
	 * Delete Visit entity with the supplied id from the store.
	 * No exception if the entity did not exist.
	 */
	public void deleteVisit(Account account, long id) {
		visitRepo.delete(visitRepo.getKey(account.getKey(), id));
	}
}
