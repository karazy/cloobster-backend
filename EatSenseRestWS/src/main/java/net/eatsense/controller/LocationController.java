package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import net.eatsense.configuration.Configuration;
import net.eatsense.controller.ImageController.UpdateImagesResult;
import net.eatsense.domain.Account;
import net.eatsense.domain.Area;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Company;
import net.eatsense.domain.FeedbackForm;
import net.eatsense.domain.Menu;
import net.eatsense.domain.Request;
import net.eatsense.domain.Request.RequestType;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.PaymentMethod;
import net.eatsense.event.CheckInActivityEvent;
import net.eatsense.event.DeleteCustomerRequestEvent;
import net.eatsense.event.DeleteSpotEvent;
import net.eatsense.event.NewCustomerRequestEvent;
import net.eatsense.event.NewLocationEvent;
import net.eatsense.event.NewSpotEvent;
import net.eatsense.event.TrashBusinessEvent;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.exceptions.ServiceException;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.persistence.AreaRepository;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.FeedbackFormRepository;
import net.eatsense.persistence.LocationRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.OfyService;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.AreaDTO;
import net.eatsense.representation.ImageDTO;
import net.eatsense.representation.LocationDTO;
import net.eatsense.representation.LocationProfileDTO;
import net.eatsense.representation.RequestDTO;
import net.eatsense.representation.SpotDTO;
import net.eatsense.representation.cockpit.SpotStatusDTO;
import net.eatsense.validation.CreationChecks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Query;


/**
 * Manages data concerning one business. 
 * 
 * @author Frederik Reifschneider
 * @author Nils Weiher
 */
public class LocationController {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final CheckInRepository checkInRepo;
	private final SpotRepository spotRepo;
	private final RequestRepository requestRepo;
	private final LocationRepository locationRepo;
	private final EventBus eventBus;
	private final AccountRepository accountRepo;
	private final ImageController imageController;
	private final Validator validator;
	private final AreaRepository areaRepo;
	private final MenuRepository menuRepo;
	private final Provider<Configuration> configProvider;
	private FeedbackFormRepository feedbackRepo;
	private final OfyService ofyService;
	
	@Inject
	public LocationController(RequestRepository rr, CheckInRepository cr,
			SpotRepository sr, LocationRepository br, EventBus eventBus,
			AccountRepository accountRepo, ImageController imageController,
			AreaRepository areaRepository, Validator validator,
			MenuRepository menuRepository,
			FeedbackFormRepository feedbackRepository,
			Provider<Configuration> configProvider,
			OfyService ofyService) {
		this.areaRepo = areaRepository;
		this.menuRepo = menuRepository;
		this.validator = validator;
		this.eventBus = eventBus;
		this.requestRepo = rr;
		this.spotRepo = sr;
		this.checkInRepo = cr;
		this.locationRepo = br;
		this.accountRepo = accountRepo;
		this.imageController = imageController;
		this.configProvider = configProvider;
		this.feedbackRepo = feedbackRepository;
		this.ofyService = ofyService;
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
		
		Iterable<Spot> allSpots = spotRepo.belongingToLocationAsync(business);
		Iterable<CheckIn> allCheckIns = checkInRepo.iterateByLocation(business.getKey());
		Iterable<Request> allRequests = requestRepo.belongingToLocationOrderedByReceivedTime(business);
		
		Map<Long, Integer> checkInCountBySpot = Maps.newHashMap();
		Map<Long, String> statusBySpot = Maps.newHashMap();
		
		List<SpotStatusDTO> spotDtos = new ArrayList<SpotStatusDTO>();
		
		// Count every checkIn and save by spot id.
		for (CheckIn checkIn : allCheckIns) {
			Integer count = checkInCountBySpot.get(checkIn.getSpot().getId());
			if(count == null) {
				count = 1;
			}
			else
				count++;
			
			checkInCountBySpot.put(checkIn.getSpot().getId(), count);
		}
		
		// Find every spot status
		for (Request request : allRequests) {
			if(statusBySpot.get(request.getSpot().getId()) == null) {
				statusBySpot.put(request.getSpot().getId(), request.getStatus());
			}
		}
		
		for (Spot spot : allSpots) {
			SpotStatusDTO spotDto = new SpotStatusDTO(spot);
			Integer checkInCount = Objects.firstNonNull(checkInCountBySpot.get(spot.getId()), 0);
			spotDto.setCheckInCount(checkInCount);
			
			// Dont add the spot if it is not active and there are 0 checkins.
			if(!spot.isActive() && checkInCount == 0) {
				continue;
			}

			if(checkInCount > 0) {
				// Get the request status from memory
				spotDto.setStatus(statusBySpot.get(spot.getId()));
			}
			
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
	public RequestDTO saveCustomerRequest(CheckIn checkIn, RequestDTO requestData) {
		checkNotNull(checkIn, "checkin cannot be null");
		checkNotNull(checkIn.getId(), "checkin id cannot be null");
		checkNotNull(checkIn.getBusiness(), "business for checkin cannot be null");
		checkNotNull(checkIn.getSpot(), "spot for checkin cannot be null");
		checkNotNull(requestData, "requestData cannot be null");
		checkNotNull(requestData.getType(), "requestData type cannot be null");
		checkArgument("CALL_WAITER".equals(requestData.getType()), "invalid request type %s", requestData.getType());
		checkArgument(!checkIn.isArchived(), "checkin cannot be archived entity");
		
		Business location = locationRepo.getByKey(checkIn.getBusiness());
		if(location.isBasic()) {
			logger.error("Unable to post request at Business with basic subscription");
			throw new IllegalAccessException("Unable to post request at Business with basic subscription");
		}
		
		Spot spot = spotRepo.getByKey(checkIn.getSpot());
		if(spot == null) {
			throw new ServiceException("Unable to find Spot for this CheckIn.");
		}
		
		if(spot.isWelcome()) {
			logger.error("Unable to post request at welcome spot");
			throw new IllegalAccessException("Unable to post request at welcome spot");
		}
		
		requestData.setCheckInId(checkIn.getId());
		requestData.setSpotId(checkIn.getSpot().getId());
		
		List<Request> requests = requestRepo.ofy().query(Request.class).ancestor(checkIn.getBusiness()).filter("checkIn", checkIn).list();		
		
		for (Request oldRequest : requests) {
			if(oldRequest.getType() == RequestType.CUSTOM && oldRequest.getStatus().equals("CALL_WAITER")) {
				logger.info("{} already saved.", oldRequest.getKey());
				oldRequest.setReceivedTime(new Date());
				requestData.setId(oldRequest.getId());
				requestRepo.saveOrUpdate(oldRequest);
				return requestData;
			}	
		}
		
		Request request = new Request(checkIn, spot);
		request.setStatus(requestData.getType());
		
		requestRepo.saveOrUpdate(request);
		
		requestData.setId(request.getId());
		
		eventBus.post(new NewCustomerRequestEvent(location, checkIn, request));
		eventBus.post(new CheckInActivityEvent(checkIn, true));
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
	public List<RequestDTO> getCustomerRequestData(Business business, Long checkInId, Long spotId) {
		checkNotNull(business, "business cannot be null");
		checkNotNull(business.getId(), "business id cannot be null");
		
		List<RequestDTO> requestDataList = new ArrayList<RequestDTO>();
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
				requestDataList.add(new RequestDTO(request));
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
	public List<RequestDTO> getCustomerRequestsForCheckIn(CheckIn checkIn) {
		checkNotNull(checkIn, "checkIn cannot be null");
		checkNotNull(checkIn.getId(), "checkIn id cannot be null");
		checkNotNull(checkIn.getBusiness(), "checkIn business cannot be null");
		
		List<RequestDTO> requestDataList = new ArrayList<RequestDTO>();
		List<Request> requests = requestRepo.query().ancestor(checkIn.getBusiness()).filter("checkIn", checkIn).list();
		
		for (Request request : requests) {
			if(request.getType() == RequestType.CUSTOM && request.getStatus().equals("CALL_WAITER")) {
				
				RequestDTO requestData = new RequestDTO(request);
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
	public RequestDTO deleteCustomerRequestForCheckIn(CheckIn checkIn, long requestId) throws IllegalAccessException {
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
		RequestDTO requestData = new RequestDTO();
		requestData.setCheckInId(request.getCheckIn().getId());
		requestData.setId(request.getId());
		requestData.setSpotId(request.getSpot().getId());
		requestData.setType(request.getStatus());
		
		requestRepo.delete(request);
		
		eventBus.post(new DeleteCustomerRequestEvent(locationRepo.getByKey(checkIn.getBusiness()), request, true));
		eventBus.post(new CheckInActivityEvent(checkIn, true));
		
		return requestData;
	}
	
	/**
	 * Get and transform the Business entities belonging to the supplied account.
	 * 
	 * @param account
	 * @return List of businesses.
	 */
	public List<LocationDTO> getBusinessDtosForAccount(Account account) {
		ArrayList<LocationDTO> businessDtos = new ArrayList<LocationDTO>();
		if(account != null && account.getBusinesses() != null ) {
			for (Business business :locationRepo.getByKeys(account.getBusinesses())) {
				businessDtos.add(new LocationDTO(business));
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
	public LocationProfileDTO createBusinessForAccount(Account account, LocationProfileDTO businessData) {
		checkNotNull(account, "account was null");
		checkNotNull(businessData, "businessData was null");
		
		if(account.getBusinesses() == null) {
			account.setBusinesses(new ArrayList<Key<Business>>());	
		}
		Business business = locationRepo.newEntity();
		
		addDefaultFeedbackForm(business);
		
		
		
		business.setPaymentMethods(new ArrayList<PaymentMethod>() );
		business.getPaymentMethods().add(new PaymentMethod("Rechnung"));
		business.setCompany(account.getCompany());
		
		
		Key<Business> businessKey = updateBusiness(business, businessData);
		
		// Let other controllers know we created a new business.
		eventBus.post(new NewLocationEvent(business));
		
		createWelcomeAreaAndSpot(businessKey);
		account.getBusinesses().add(businessKey);
		accountRepo.saveOrUpdate(account);
		
		return new LocationProfileDTO(business);
	}

	/**
	 * Load default FeedbackForm, copy and set it for the given Business entity.
	 * 
	 * @param business
	 */
	public Business addDefaultFeedbackForm(Business business) {
		Configuration config = configProvider.get();
		
		if(config.getDefaultFeedbackForm() != null) {
			// Copy default feedback form if it exists.
			FeedbackForm feedbackForm = null;
			
			try {
				feedbackForm = feedbackRepo.getByKey(config.getDefaultFeedbackForm());				
			} catch (com.googlecode.objectify.NotFoundException e) {
				logger.warn("Default FeedbackForm not found.");				
			}
			
			if(feedbackForm != null) {
				// Remove id so we create a copy of the original entity.
				feedbackForm.setId(null);
				business.setFeedbackForm(feedbackRepo.saveOrUpdate(feedbackForm));
			}
		}
		else {
			logger.warn("defaultFeedbackForm not set in default configuration.");
		}
		
		return business;
	}

	/**
	 * @param businessKey
	 * @return 
	 */
	public Area createWelcomeAreaAndSpot(Key<Business> businessKey) {
		return createWelcomeAreaAndSpot(businessKey, Optional.<String>absent()); 
	}

	/**
	 * @param businessKey
	 * @param optWelcomeBarcode Override auto generated barcode
	 */
	public Area createWelcomeAreaAndSpot(Key<Business> businessKey, Optional<String> optWelcomeBarcode) {
		checkNotNull(businessKey, "businessKey was null");
		
		Area welcomeArea = createWelcomeArea(businessKey);
		
		createWelcomeSpot(businessKey, areaRepo.getKey(welcomeArea), optWelcomeBarcode);
		
		return welcomeArea;
	}

	/**
	 * Update and save the Business entity with new data.
	 * 
	 * @param business - Entity to update.
	 * @param businessData - The data transfer object to update the entity with.
	 * @return Datastore key of the business.
	 */
	public Key<Business> updateBusiness(Business business,	LocationProfileDTO businessData) {
		checkNotNull(business, "business was null");
		checkNotNull(businessData, "businessData was null");
		
		Set<ConstraintViolation<LocationProfileDTO>> violationSet = validator.validate(businessData);
		
		if(!violationSet.isEmpty()) {
			StringBuilder stringBuilder = new StringBuilder("validation errors:");
			for (ConstraintViolation<LocationProfileDTO> violation : violationSet) {
				// Format the message like: '"{property}" {message}.'
				stringBuilder.append(String.format(" \"%s\" %s.", violation.getPropertyPath(), violation.getMessage()));
			}
			throw new ValidationException(stringBuilder.toString());
		}
		
		// Pass through of spot count
		business.setSpotCount(businessData.getSpotCount());
		
		business.setAddress(businessData.getAddress());
		business.setCity(businessData.getCity());
		business.setDescription(businessData.getDescription());
		business.setName(businessData.getName());
		business.setPhone(businessData.getPhone());
		business.setPostcode(businessData.getPostcode());
		business.setSlogan(businessData.getSlogan());
		business.setCurrency(businessData.getCurrency());
		business.setUrl(businessData.getUrl());
		business.setFbUrl(businessData.getFbUrl());
		business.setLang(businessData.getLang());
		business.setEmail(businessData.getEmail());
		business.setStars(businessData.getStars());
		business.setOfflineEmailAlertActive(businessData.isOfflineEmailAlertActive());
		business.setInactiveCheckInNotificationActive(businessData.isInactiveCheckInNotificationActive());
		business.setIncomingOrderNotifcationEnabled(businessData.isIncomingOrderNotificationEnabled());
		
		if(businessData.getFeatures() != null) {
			for (Entry<String, Boolean> featureEntry : businessData.getFeatures().entrySet()) {
				String featureName = featureEntry.getKey();
				if(!Business.AVAILABLE_FEATURES.contains(featureName)) {
					logger.warn("Unknown feature name: {}", featureName);
				}
				else {
					if(featureEntry.getValue()) {
						if(business.getDisabledFeatures().remove(featureName))
							business.setDirty(true);
					}
					else {
						if(business.getDisabledFeatures().add(featureName))
							business.setDirty(true);
					}
				}
			}
		}
		
		if( !Strings.isNullOrEmpty(businessData.getTheme()) ) {
			// Do not override default theme
			business.setTheme(businessData.getTheme());
		}
		
		if(businessData.getPaymentMethods() != null && !businessData.getPaymentMethods().isEmpty()) {
			business.setPaymentMethods(businessData.getPaymentMethods());
		}

		Key<Business> key;
		
		if(business.isDirty()) {
			key = locationRepo.saveOrUpdate(business);
		}
		else {
			key = locationRepo.getKey(business);
		}
		
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
			locationRepo.saveOrUpdate(business);
		}

		return result.getUpdatedImage();
	}
	

	/**
	 * Remove an image embedded in this Business entity.
	 * 
	 * @param business The Business containing the image.
	 * @param imageId Unique identifier for the image.
	 * @return <code>true</code> if an image was removed, <code>false</code> otherwise.
	 */
	public boolean removeBusinessImage(Business business, String imageId) {
		checkNotNull(business, "business was null");
		checkNotNull(Strings.emptyToNull(imageId), "imageId was null or empty");
		
		UpdateImagesResult result = imageController.removeImage(imageId, business.getImages());
		
		if(result.isDirty()) {
			business.setImages(result.getImages());
			locationRepo.saveOrUpdate(business);
		}
		
		return result.isDirty();
	}
	
	/**
	 * @param business
	 * @param account
	 */
	public void trashBusiness(Business business, Account account) {
		checkNotNull(business, "business was null");
		checkNotNull(account, "account was null");
		checkArgument(!business.isTrash(), "business was already trashed");
		
		locationRepo.trashEntity(business, account.getLogin());
		
		List<Area> area = areaRepo.getByParent(business.getKey());
		for (Area spot : area) {
			spot.setActive(false);
		}
		areaRepo.saveOrUpdate(area);
		
		eventBus.post(new TrashBusinessEvent(business));
	}
	
	
	/**
	 * @param businessKey
	 * @param areaId If different from 0, filter by area.
	 * @param welcome 
	 * @param noMaster 
	 * @return List of spots for the business and for the area if specified.
	 */
	public List<SpotDTO> getSpots(Key<Business> businessKey, long areaId, boolean welcome, boolean noMaster) {
		checkNotNull(businessKey, "businessKey was null");
		
		ArrayList<SpotDTO> spotDTOList = new ArrayList<SpotDTO>();
		
		List<Spot> spots;
		if(welcome) {
			spots = spotRepo.getListByParentAndProperty(businessKey, "welcome", true);
		}
		else if(areaId != 0) {
			Key<Area> areaKey = areaRepo.getKey(businessKey, areaId);
			spots = spotRepo.getListByParentAndProperty(businessKey, "area", areaKey);
		}
		else {
			spots = spotRepo.getByParent(businessKey);
		}
		
		for(Spot spot :  spots) {
			// Dont return master Spot if noMaster is true
			if(noMaster && spot.isMaster())
				continue;
			
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
	public SpotDTO createSpot(Key<Business> locationKey, SpotDTO spotData, boolean welcome) {
		checkNotNull(locationKey, "businessKey was null");
		
		// Make sure to not create a new Spot at the "welcome" Area
		if(spotData.getAreaId() != null) {
			Key<Area> areaKey = areaRepo.getKey(locationKey, spotData.getAreaId());
			if(!welcome) {
				try {
					Area area = areaRepo.getByKey(areaKey);
					if(area.isWelcome()) {
						throw new ValidationException("Unable to create new Spots at welcome area");
					}
				} catch (com.googlecode.objectify.NotFoundException e) {
					logger.error("Area for spot creation not found.");
					throw new ValidationException("No Area found with id=" + areaKey.getId());
				}
			}			
		}
		
		Spot spot = spotRepo.newEntity();
		
		spot.setBusiness(locationKey);
		spot.setWelcome(welcome);
		spot.setId(ofyService.factory().allocateId(locationKey, Spot.class));
		// Generate the  new barcode
		spot.generateBarcode();
		
		int spotCount = 0;
		if(!welcome) {
			spotCount = countSpots(locationKey);
		}
		
		updateSpot(spot, spotData);
		
		if(!welcome)
			eventBus.post(new NewSpotEvent(locationKey, spot, spotCount + 1, false));
		
		return new SpotDTO(spot);
	}
	
	private Spot createWelcomeSpot(Key<Business> locationKey, Key<Area> welcomeAreaKey, Optional<String> optionalBarcode) {
		Spot spot = spotRepo.newEntity();
		spot.setActive(true);
		spot.setBusiness(locationKey);
		spot.setId(ofyService.factory().allocateId(locationKey, Spot.class));
		spot.setWelcome(true);
		spot.setName("Welcome Spot");
		spot.setArea(welcomeAreaKey);
		if(optionalBarcode.isPresent()) {
			logger.info("Creating welcome Spot with barcode={}", optionalBarcode.get() );
			spot.setBarcode(optionalBarcode.get());
		}
		else {
			// Generate the  new barcode
			spot.generateBarcode();
		}
		
		spotRepo.saveOrUpdate(spot);
		
		return spot;
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
		
		if(spot.isWelcome() || spot.isMaster()) {
			String message = "Not allowed to delete welcome or master Spot.";
			logger.error(message);
			throw new IllegalAccessException(message);
		}
		
		int spotCount = countSpots(spot.getBusiness());
		
		spot.setActive(false);
		spotRepo.trashEntity(spot, account.getLogin());
		
		eventBus.post(new DeleteSpotEvent(spot.getBusiness(), spot, spotCount - 1, false));
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
	 * @param business
	 * @param areaData
	 * @return
	 */
	public Area createArea(Key<Business> businessKey, AreaDTO areaData) {
		checkNotNull(businessKey, "businessKey was null");
		checkNotNull(areaData, "areaData was null");
		
		Area area = areaRepo.newEntity();
		area.setBusiness(businessKey);
		area.setWelcome(areaData.isWelcome());
		updateArea(area, areaData);
	
		createMasterSpot(businessKey, areaRepo.getKey(area));
		
		return area;
	}

	/**
	 * Helper function to create a master Spot for an Area. 
	 * 
	 * @param businessKey
	 * @param area
	 * @return 
	 */
	public Spot createMasterSpot(Key<Business> businessKey, Key<Area> areaKey) {
		return createMasterSpot(businessKey, areaKey, Optional.<String>absent());
	}

	/**
	 * Helper function to create a master Spot for an Area. 
	 * 
	 * @param locationKey
	 * @param optBarcode TODO
	 * @param area
	 * @return 
	 */
	public Spot createMasterSpot(Key<Business> locationKey, Key<Area> areaKey, Optional<String> optBarcode) {
		// create "master" Spot
		logger.info("Creating master Spot for {}.", areaKey);
		Spot spot = spotRepo.newEntity();
		spot.setActive(true);
		spot.setArea(areaKey);
		spot.setBusiness(locationKey);
		spot.setMaster(true);
		spot.setName("Master Spot");
		// Get a new Id from the datastore, so we can generate the barcode immediately
		spot.setId(ofyService.factory().allocateId(locationKey, Spot.class));
		if(optBarcode.isPresent()) {
			// Set Barcode if override is present.
			spot.setBarcode(optBarcode.get());
		}
		else {
			spot.generateBarcode();
		}
		
		
		spotRepo.saveOrUpdate(spot);
		
		return spot;
	}
	
	/**
	 * Create the welcome area for this business.
	 * 
	 * @param businessKey
	 * @return
	 */
	private Area createWelcomeArea(Key<Business> businessKey) {
		Area area = areaRepo.newEntity();
		area.setBusiness(businessKey);
		area.setActive(true);
		area.setDescription("Welcome Area");
		area.setName("Welcome Area");
		area.setWelcome(true);
		
		areaRepo.saveOrUpdate(area);
		
		return area;
	}

	/**
	 * 
	 * @param businessKey
	 * @param noWelcome 
	 * @return List of areas as transfer objects.
	 */
	public List<AreaDTO> getAreas(Key<Business> businessKey, boolean onlyActive, boolean noWelcome) {
		checkNotNull(businessKey, "businessKey was null");
		ArrayList<AreaDTO> areaDtos = new ArrayList<AreaDTO>();
		List<Area> areas = areaRepo.getListByParentAndProperty(businessKey, "trash", false);
		
		for(Area area : areas) {
			if(noWelcome && area.isWelcome())
				continue;
			if(onlyActive && !area.isActive()) {
				continue;
			}
			
			areaDtos.add(new AreaDTO(area));
		}
		
		return areaDtos;
	}

	/**
	 * @param area
	 * @param areaData
	 * @return
	 */
	public Area updateArea(Area area, AreaDTO areaData) {
		checkNotNull(area, "area was null");
		checkNotNull(areaData, "areaData was null");
		
		validator.validate(areaData, Default.class, CreationChecks.class);
		
		area.setActive(areaData.isActive());
		area.setDescription(areaData.getDescription());
		area.setName(areaData.getName());
		area.setBarcodeRequired(areaData.isBarcodeRequired());
		
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
	
	/**
	 * Mark area and the associated spots as trashed.
	 *  
	 * @param area
	 * @param account
	 */
	public void deleteArea(Area area, Account account) {
		checkNotNull(area, "area was null");
		
		if(area.isWelcome()) {
			throw new IllegalAccessException("Not allowed to delete welcome area.");
		}
		int spotCount = countSpots(area.getBusiness());		
		List<Spot> spots = spotRepo.getListByParentAndProperty(area.getBusiness(), "area", area);
		
		int deletedSpots = 0;
		for (Spot spot : spots) {
			spot.setActive(false);
			deletedSpots++;
		}
		spotRepo.trashEntities(spots, account.getEmail());
		eventBus.post(new DeleteSpotEvent(area.getBusiness(), null, spotCount - deletedSpots, true));
		
		area.setActive(false);
		areaRepo.trashEntity(area, account.getLogin());
	}
	
	/**
	 * @param companyId
	 * @return Location entities for this company
	 */
	public List<Business> getLocations(long companyId) {
		if(companyId == 0) 
			return locationRepo.getAll();
		else 
			return locationRepo.getListByProperty("company", Company.getKey(companyId));
	}
	
	/**
	 * @param locationId
	 * @param countSpots
	 * @return Location(Business) entity
	 */
	public Business get(long locationId, boolean countSpots) {
		Business location;
		try {
			location = locationRepo.getById(locationId);
		} catch (com.googlecode.objectify.NotFoundException e) {
			throw new NotFoundException("No location found with id=" + locationId);
		}
		
		if(countSpots) {
			setSpotCount(location);
		}
		
		return location;
	}
	
	/**
	 * Retrieve the location data for a given QR-code.
	 * 
	 * @param spotCode
	 * @return
	 */
	public Business getLocationBySpotCode(String spotCode) {
		Spot spot = spotRepo.getByProperty("barcode", spotCode);
		if(spot== null) {
			throw new NotFoundException("Unknown spotCode");
		}
		
		Business location;
		try {
			location = locationRepo.getByKey(spot.getBusiness());
		} catch (com.googlecode.objectify.NotFoundException e) {
			logger.error("Unable to retrieve {} for Spot (id={})", spot.getBusiness(), spot.getId());
			throw new NotFoundException();
		}
		
		return location;
	}
	
	/**
	 * @param locationId
	 * @return
	 */
	public SpotDTO getWelcomeSpot(long locationId) {
		if(locationId == 0) {
			throw new ValidationException("locationId was 0");
		}
		
		Key<Business> locationKey = locationRepo.getKey(locationId);
		Business location;
		try {
			location = locationRepo.getByKey(locationKey);
		} catch (com.googlecode.objectify.NotFoundException e) {
			logger.error("Not found: {}", locationKey);			
			throw new NotFoundException("Unknown locationId");
		}
		
		Spot spot = spotRepo.belongingToLocationAndWelcomeSpot(locationKey);
		
		if(spot == null) {
			logger.error("Welcome spot not found for {}", locationKey);
			throw new NotFoundException("No welcome spot found");
		}
		
		Area area = areaRepo.getByKey(spot.getArea());
		return new SpotDTO(spot, location, area);
	}

	/**
	 * @param location Business entity 
	 * @return Business entity with spotcount set
	 */
	
	public Business setSpotCount(Business location) {
		checkNotNull(location, "location was null");
		location.setSpotCount(countSpots(location.getKey()));
		return location;
	}

	/**
	 * @param locationKey
	 * @return
	 */
	private int countSpots(Key<Business> locationKey) {
		return spotRepo.query().ancestor(locationKey).filter("trash", false).count();
	}
}