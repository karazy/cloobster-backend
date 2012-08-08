package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import net.eatsense.controller.ImageController.UpdateImagesResult;
import net.eatsense.domain.Account;
import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Request;
import net.eatsense.domain.Request.RequestType;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.PaymentMethod;
import net.eatsense.event.DeleteCustomerRequestEvent;
import net.eatsense.event.NewCustomerRequestEvent;
import net.eatsense.event.TrashBusinessEvent;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.AreaDTO;
import net.eatsense.representation.BusinessDTO;
import net.eatsense.representation.BusinessProfileDTO;
import net.eatsense.representation.CustomerRequestDTO;
import net.eatsense.representation.ImageDTO;
import net.eatsense.representation.SpotDTO;
import net.eatsense.representation.cockpit.SpotStatusDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Query;


/**
 * Manages data concerning one business. 
 * 
 * @author Frederik Reifschneider
 * @author Nils Weiher
 */
public class BusinessController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final CheckInRepository checkInRepo;
	private final SpotRepository spotRepo;
	private final RequestRepository requestRepo;
	private final BusinessRepository businessRepo;
	private final EventBus eventBus;
	private final AccountRepository accountRepo;
	private final ImageController imageController;
	private final Validator validator;
	private final AreaRepository areaRepo;
	private final MenuRepository menuRepo;
	
	@Inject
	public BusinessController(RequestRepository rr, CheckInRepository cr,
			SpotRepository sr, BusinessRepository br, EventBus eventBus,
			AccountRepository accountRepo, ImageController imageController,AreaRepository areaRepository, Validator validator, MenuRepository menuRepository) {
		this.areaRepo = areaRepository;
		this.menuRepo = menuRepository;
		this.validator = validator;
		this.eventBus = eventBus;
		this.requestRepo = rr;
		this.spotRepo = sr;
		this.checkInRepo = cr;
		this.businessRepo = br;
		this.accountRepo = accountRepo;
		this.imageController = imageController;
	}
	
	/**
	 * Retrieve initial status data of all spots for the given business id.<br>
	 * (mainly used by the Eatsense Cockpit application).
	 * 
	 * @param business
	 * @return List of SpotCockpitDTO objects
	 */
	public List<SpotStatusDTO> getSpotStatusData(Business business){
		checkNotNull(business, "business cannot be null");
		checkNotNull(business.getId(), "business id cannot be null");
		List<Spot> allSpots = spotRepo.getByParent(business);
		List<SpotStatusDTO> spotDtos = new ArrayList<SpotStatusDTO>();
		
		for (Spot spot : allSpots) {
			SpotStatusDTO spotDto = new SpotStatusDTO();
			spotDto.setId(spot.getId());
			spotDto.setName(spot.getName());
			int checkInCount = checkInRepo.countActiveCheckInsAtSpot(spot.getKey());
			spotDto.setCheckInCount(checkInCount);
			
			if(checkInCount > 0) {
				// Only check the request status if there are checkins.
				Request request = requestRepo.ofy().query(Request.class).filter("spot",spot.getKey()).order("-receivedTime").get();
				if(request != null) {
					spotDto.setStatus(request.getStatus());
				}
			}
			
			// Dont add the spot if it is not active and there are 0 checkins.
			if(spot.isActive() || checkInCount > 0)
				spotDtos.add(spotDto);
		}
		
		return spotDtos;
	}
	
	/**
	 * Save an outstanding request posted by a checkedin customer.
	 * 
	 * @param checkInUid
	 * @param requestData
	 * @return requestData
	 */
	public CustomerRequestDTO saveCustomerRequest(CheckIn checkIn, CustomerRequestDTO requestData) {
		checkNotNull(checkIn, "checkin cannot be null");
		checkNotNull(checkIn.getId(), "checkin id cannot be null");
		checkNotNull(checkIn.getBusiness(), "business for checkin cannot be null");
		checkNotNull(checkIn.getSpot(), "spot for checkin cannot be null");
		checkNotNull(requestData, "requestData cannot be null");
		checkNotNull(requestData.getType(), "requestData type cannot be null");
		checkArgument("CALL_WAITER".equals(requestData.getType()), "invalid request type %s", requestData.getType());
		checkArgument(!checkIn.isArchived(), "checkin cannot be archived entity");
		
		requestData.setCheckInId(checkIn.getId());
		requestData.setSpotId(checkIn.getSpot().getId());
		
		List<Request> requests = requestRepo.ofy().query(Request.class).ancestor(checkIn.getBusiness()).filter("checkIn", checkIn).list();		
		
		for (Request oldRequest : requests) {
			if(oldRequest.getType() == RequestType.CUSTOM && oldRequest.getStatus().equals("CALL_WAITER")) {
				logger.info("{} already registered.", oldRequest.getKey());
				oldRequest.setReceivedTime(new Date());
				requestData.setId(oldRequest.getId());
				requestRepo.saveOrUpdate(oldRequest);
				return requestData;
			}	
		}
		
		Request request = new Request();
		request.setBusiness(checkIn.getBusiness());
		request.setCheckIn(checkIn.getKey());
		request.setReceivedTime(new Date());
		request.setSpot(checkIn.getSpot());
		request.setStatus(requestData.getType());
		request.setType(RequestType.CUSTOM);
		
		requestRepo.saveOrUpdate(request);
		
		requestData.setId(request.getId());
		
		eventBus.post(new NewCustomerRequestEvent(businessRepo.getByKey(checkIn.getBusiness()), checkIn, request));								
		return requestData;
	}
	
	/**
	 * Get outstanding CALL_WAITER requests for the given business and optionally checkin or spot.
	 * 
	 * @param business
	 * @param checkInId can be null
	 * @param spotId can be null
	 * @return list of found request dtos or empty list if none found
	 */
	public List<CustomerRequestDTO> getCustomerRequestData(Business business, Long checkInId, Long spotId) {
		checkNotNull(business, "business cannot be null");
		checkNotNull(business.getId(), "business id cannot be null");
		
		List<CustomerRequestDTO> requestDataList = new ArrayList<CustomerRequestDTO>();
		Query<Request> query = requestRepo.query().ancestor(business);

		if( spotId != null) {
			query = query.filter("spot", Spot.getKey(business.getKey(), spotId));
		}
		
		if( checkInId != null ) {
			query = query.filter("checkIn", CheckIn.getKey(checkInId));
		}
		
		List<Request> requests = query.list();
		for (Request request : requests) {
			if(request.getType() == RequestType.CUSTOM && request.getStatus().equals("CALL_WAITER")) {
				
				CustomerRequestDTO requestData = new CustomerRequestDTO();
				requestData.setId(request.getId());
				requestData.setCheckInId(request.getCheckIn().getId());
				requestData.setSpotId(request.getSpot().getId());
				requestData.setType(request.getStatus());
				
				requestDataList.add(requestData);
			}
				
		}
		
		return requestDataList;
	}

	/**
	 * Clear an outstanding request of the customer.
	 * 
	 * @param business
	 * @param requestId
	 * @throws IllegalArgumentException if the request was not found
	 */
	public void deleteCustomerRequest(Business business, long requestId)  throws IllegalArgumentException{
		checkNotNull(business, "business cannot be null");
		checkNotNull(business.getId(), "business id cannot be null");
		checkArgument(requestId != 0, "requestId cannot be 0");
		
		Request request;
		try {
			request = requestRepo.getById(business.getKey(), requestId);
		} catch (com.googlecode.objectify.NotFoundException e1) {
			throw new IllegalArgumentException("request not found", e1);
		}

		requestRepo.delete(request);
		
		eventBus.post(new DeleteCustomerRequestEvent(business, request, false));
	}
	
	/**
	 * Get outstanding CALL_WAITER requests for the given checkin.
	 * 
	 * @param checkIn
	 * @return list of found request dtos or empty list if none found
	 */
	public List<CustomerRequestDTO> getCustomerRequestsForCheckIn(CheckIn checkIn) {
		checkNotNull(checkIn, "checkIn cannot be null");
		checkNotNull(checkIn.getId(), "checkIn id cannot be null");
		checkNotNull(checkIn.getBusiness(), "checkIn business cannot be null");
		
		List<CustomerRequestDTO> requestDataList = new ArrayList<CustomerRequestDTO>();
		List<Request> requests = requestRepo.query().ancestor(checkIn.getBusiness()).filter("checkIn", checkIn).list();
		
		for (Request request : requests) {
			if(request.getType() == RequestType.CUSTOM && request.getStatus().equals("CALL_WAITER")) {
				
				CustomerRequestDTO requestData = new CustomerRequestDTO();
				requestData.setId(request.getId());
				requestData.setCheckInId(request.getCheckIn().getId());
				requestData.setSpotId(request.getSpot().getId());
				requestData.setType(request.getStatus());
				
				requestDataList.add(requestData);
			}
				
		}
		
		return requestDataList;
	}
	
	/**
	 * Delete request of the given checkIn or throws exception if not possible.
	 * 
	 * @param checkIn
	 * @param requestId
	 * @return 
	 * @throws IllegalAccessException if the checkin does not own the request
	 */
	public CustomerRequestDTO deleteCustomerRequestForCheckIn(CheckIn checkIn, long requestId) throws IllegalAccessException {
		checkNotNull(checkIn, "checkIn cannot be null");
		checkNotNull(checkIn.getId(), "checkIn id cannot be null");
		checkNotNull(checkIn.getBusiness(), "checkIn business cannot be null");
		checkArgument(requestId != 0, "requestId cannot be 0");
		
		Request request;
		try {
			request = requestRepo.getById(checkIn.getBusiness(), requestId);
		} catch (com.googlecode.objectify.NotFoundException e1) {
			throw new NotFoundException(String.format("request %d not found", requestId));
		}

		if( !checkIn.getId().equals(request.getCheckIn().getId())) {
			throw new IllegalAccessException("checkIn does not own the request");
		}
		CustomerRequestDTO requestData = new CustomerRequestDTO();
		requestData.setCheckInId(request.getCheckIn().getId());
		requestData.setId(request.getId());
		requestData.setSpotId(request.getSpot().getId());
		requestData.setType(request.getStatus());
		
		requestRepo.delete(request);
		
		eventBus.post(new DeleteCustomerRequestEvent(businessRepo.getByKey(checkIn.getBusiness()), request, true));
		
		return requestData;
	}
	
	/**
	 * Get and transform the Business entities belonging to the supplied account.
	 * 
	 * @param account
	 * @return List of businesses.
	 */
	public List<BusinessDTO> getBusinessDtosForAccount(Account account) {
		ArrayList<BusinessDTO> businessDtos = new ArrayList<BusinessDTO>();
		if(account != null && account.getBusinesses() != null ) {
			for (Business business :businessRepo.getByKeys(account.getBusinesses())) {
				businessDtos.add(new BusinessDTO(business));
			}
		}
		return businessDtos;
	}
	
	/**
	 * Create a new Business entity owned by the supplied Account.
	 * 
	 * @param account - The account that manages the new Business.
	 * @param businessData - The profile data to use for the business.
	 * @return The profile data updated with the id of the new entity.
	 */
	public BusinessProfileDTO createBusinessForAccount(Account account, BusinessProfileDTO businessData) {
		checkNotNull(account, "account was null");
		checkNotNull(businessData, "businessData was null");
		
		if(account.getBusinesses() == null) {
			account.setBusinesses(new ArrayList<Key<Business>>());	
		}
		Business business = businessRepo.newEntity();
		
		business.setPaymentMethods(new ArrayList<PaymentMethod>() );
		business.getPaymentMethods().add(new PaymentMethod("Bar"));
		business.setCompany(account.getCompany());
		
		account.getBusinesses().add(updateBusiness(business, businessData));
		accountRepo.saveOrUpdate(account);
		
		return businessData;
	}

	/**
	 * Update and save the Business entity with new data.
	 * 
	 * @param business - Entity to update.
	 * @param businessData - The data transfer object to update the entity with.
	 * @return Datastore key of the business.
	 */
	public Key<Business> updateBusiness(Business business,	BusinessProfileDTO businessData) {
		checkNotNull(business, "business was null");
		checkNotNull(businessData, "businessData was null");
		
		Set<ConstraintViolation<BusinessProfileDTO>> violationSet = validator.validate(businessData);
		
		if(!violationSet.isEmpty()) {
			StringBuilder stringBuilder = new StringBuilder("validation errors:");
			for (ConstraintViolation<BusinessProfileDTO> violation : violationSet) {
				// Format the message like: '"{property}" {message}.'
				stringBuilder.append(String.format(" \"%s\" %s.", violation.getPropertyPath(), violation.getMessage()));
			}
			throw new ValidationException(stringBuilder.toString());
		}
		
		business.setAddress(businessData.getAddress());
		business.setCity(businessData.getCity());
		business.setDescription(businessData.getDescription());
		business.setName(businessData.getName());
		business.setPhone(businessData.getPhone());
		business.setPhone(businessData.getPhone());
		business.setPostcode(businessData.getPostcode());
		business.setSlogan(businessData.getSlogan());
		business.setCurrency(businessData.getCurrency());
		
		if( !Strings.isNullOrEmpty(businessData.getTheme()) ) {
			// Do not override default theme
			business.setTheme(businessData.getTheme());
		}
		
		if(businessData.getPaymentMethods() != null && !businessData.getPaymentMethods().isEmpty()) {
			business.setPaymentMethods(businessData.getPaymentMethods());
		}

		Key<Business> key;
		
		if(business.isDirty()) {
			key = businessRepo.saveOrUpdate(business);
		}
		else {
			key = businessRepo.getKey(business);
		}
		
		businessData = new BusinessProfileDTO(business);
		
		return key;
	}
	
	
	/**
	 * Update the images of the given Business entity, with the supplied image
	 * data.
	 * 
	 * @param account
	 *            - The Account that uploaded the image to use.
	 * @param business
	 *            - The Business for which the images will be updated.
	 * @param updatedImage
	 *            - Data transfer object, containing the {@link BlobKey} string and string id ("logo","scrapbook",etc. ) of the
	 *            image to save.
	 * @return The saved image data.
	 */
	public ImageDTO updateBusinessImage(Account account, Business business, ImageDTO updatedImage) {
		checkNotNull(account, "account was null");
		checkNotNull(business, "business was null");
		checkNotNull(updatedImage, "updatedImage was null ");
		checkArgument(!Strings.isNullOrEmpty(updatedImage.getId()),	"updatedImage id was null or empty");

		UpdateImagesResult result = imageController.updateImages(account, business.getImages(), updatedImage);

		if (result.isDirty()) {
			// Only save if we updated or added an image to the list.
			business.setImages(result.getImages());
			businessRepo.saveOrUpdate(business);
		}

		return result.getUpdatedImage();
	}
	
	/**
	 * @param business
	 * @param account
	 */
	public void trashBusiness(Business business, Account account) {
		checkNotNull(business, "business was null");
		checkNotNull(account, "account was null");
		checkArgument(!business.isTrash(), "business was already trashed");
		
		businessRepo.trashEntity(business, account.getLogin());
		
		List<Spot> spots = spotRepo.getByParent(business.getKey());
		for (Spot spot : spots) {
			spot.setActive(false);
		}
		spotRepo.saveOrUpdate(spots);
		
		eventBus.post(new TrashBusinessEvent(business));
	}
	
	
	/**
	 * @param businessKey
	 * @param areaId If different from 0, filter by area.
	 * @return List of spots for the business and for the area if specified.
	 */
	public List<SpotDTO> getSpots(Key<Business> businessKey, long areaId) {
		checkNotNull(businessKey, "businessKey was null");
		
		ArrayList<SpotDTO> spotDTOList = new ArrayList<SpotDTO>();
		
		List<Spot> spots;
		if(areaId != 0) {
			Key<Area> areaKey = areaRepo.getKey(businessKey, areaId);
			spots = spotRepo.getListByParentAndProperty(businessKey, "area", areaKey);
		}
		else {
			spots = spotRepo.getByParent(businessKey);
		}
		
		for(Spot spot :  spots) {
			if(!spot.isTrash()) {
				spotDTOList.add(new SpotDTO(spot));
			}
		}
		
		return spotDTOList;
	}
	
	/**
	 * @param business
	 * @param spotData
	 * @return 
	 */
	public SpotDTO createSpot(Key<Business> businessKey, SpotDTO spotData) {
		checkNotNull(businessKey, "businessKey was null");
		
		Spot spot = spotRepo.newEntity();
		spot.setBusiness(businessKey);
		updateSpot(spot, spotData);
		// Generate the barcode like this: {businessId}-{spotId}
		spot.setBarcode(String.format("%d-%d", businessKey.getId(), spot.getId()));
		spotRepo.saveOrUpdate(spot);
		
		return new SpotDTO(spot);
	}

	/**
	 * @param spot
	 * @param spotData
	 * @return updated spotData
	 */
	public Spot updateSpot(Spot spot, SpotDTO spotData) {
		checkNotNull(spot, "spot was null");
		checkNotNull(spotData, "spotData was null");
		
		Set<ConstraintViolation<SpotDTO>> violationSet = validator.validate(spotData);
		
		if(!violationSet.isEmpty()) {
			StringBuilder stringBuilder = new StringBuilder("validation errors:");
			for (ConstraintViolation<SpotDTO> violation : violationSet) {
				// Format the message like: '"{property}" {message}.'
				stringBuilder.append(String.format(" \"%s\" %s.", violation.getPropertyPath(), violation.getMessage()));
			}
			throw new ValidationException(stringBuilder.toString());
		}
		
		spot.setActive(spotData.isActive());
		spot.setName(spotData.getName());
		
		Key<Area> areaKey = null;
		if(spotData.getAreaId() != null) {
			areaKey = areaRepo.getKey(spot.getBusiness(), spotData.getAreaId());
		}
		spot.setArea(areaKey);
		
		
		
		if(spot.isDirty()) {
			spotRepo.saveOrUpdate(spot);
		}
		
		return spot;
	}
	
	/**
	 * Deactivate and save a Spot as trash.
	 * 
	 * @param spot
	 * @param account
	 */
	public void trashSpot(Spot spot, Account account) {
		checkNotNull(spot, "spot was null");
		checkNotNull(account, "account was null");
		
		spot.setActive(false);
		spotRepo.trashEntity(spot, account.getLogin());
	}

	/**
	 * @param businessKey
	 * @param spotId
	 * @return Spot
	 */
	public Spot getSpot(Key<Business> businessKey, long spotId) {
		checkNotNull(businessKey, "businessKey was null");
		
		try {
			return spotRepo.getById(businessKey, spotId);
		} catch (com.googlecode.objectify.NotFoundException e) {
			throw new NotFoundException();
		}
	}
	
	/**
	 * @param businessKey
	 * @param id
	 * @return
	 */
	public Area getArea(Key<Business> businessKey, long id) {
		checkNotNull(businessKey, "businessKey was null");
		checkArgument(id != 0, "id was zero");
		
		try {
			return areaRepo.getById(businessKey, id);
		} catch (com.googlecode.objectify.NotFoundException e) {
			throw new NotFoundException();
		}
	}
	
	/**
	 * 
	 * @param businessKey
	 * @return List of areas as transfer objects.
	 */
	public List<AreaDTO> getAreas(Key<Business> businessKey) {
		checkNotNull(businessKey, "businessKey was null");
		ArrayList<AreaDTO> areaDtos = new ArrayList<AreaDTO>();
		
		for(Area area : areaRepo.getByParent(businessKey)) {
			areaDtos.add(new AreaDTO(area));
		}
		
		return areaDtos;
	}
	
	/**
	 * @param business
	 * @param areaData
	 * @return
	 */
	public Area createArea(Key<Business> businessKey, AreaDTO areaData) {
		checkNotNull(businessKey, "businessKey was null");
		checkNotNull(areaData, "areaData was null");
		
		Area area = areaRepo.newEntity();
		area.setBusiness(businessKey);
		updateArea(area, areaData);
		
		return area;
	}

	/**
	 * @param area
	 * @param areaData
	 * @return
	 */
	public Area updateArea(Area area, AreaDTO areaData) {
		checkNotNull(area, "area was null");
		checkNotNull(areaData, "areaData was null");
		
		validator.validate(areaData);
		
		area.setActive(areaData.isActive());
		area.setDescription(areaData.getDescription());
		area.setName(areaData.getName());
		
		List<Key<Menu>> menus = null;
		
		if(areaData.getMenuIds() != null && !areaData.getMenuIds().isEmpty()) {
			menus = new ArrayList<Key<Menu>>();
			for (Long menuId : areaData.getMenuIds()) {
				Key<Menu> menuKey = menuRepo.getKey(area.getBusiness(), menuId);
				
				if(!menus.contains(menuKey)) {
					menus.add(menuKey);
				}
				else {
					logger.warn("Duplicate menu id: {}", menuId);
				}
			}
		}
		area.setMenus(menus);
		
		if(area.isDirty()) {
			areaRepo.saveOrUpdate(area);
		}
		
		return area;
	}
}