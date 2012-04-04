package net.eatsense.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Order;
import net.eatsense.domain.Business;
import net.eatsense.domain.Spot;
import net.eatsense.domain.User;
import net.eatsense.domain.embedded.CheckInStatus;
import net.eatsense.domain.validation.CheckInStep2;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.CheckInDTO;
import net.eatsense.representation.ErrorDTO;
import net.eatsense.representation.SpotDTO;
import net.eatsense.representation.Transformer;
import net.eatsense.representation.cockpit.CheckInStatusDTO;
import net.eatsense.representation.cockpit.MessageDTO;
import net.eatsense.representation.cockpit.SpotStatusDTO;
import net.eatsense.util.IdHelper;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.sun.jersey.api.NotFoundException;

/**
 * Controller for checkIn logic and process. When an attempt to checkIn at a
 * business is made, various validations have must be executed.
 * 
 * @author Frederik Reifschneider
 * 
 */
public class CheckInController {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private BusinessRepository businessRepo;
	private CheckInRepository checkInRepo;
	private SpotRepository barcodeRepo;
	private Transformer transform;
	private ChannelController channelCtrl;
	private ObjectMapper mapper;
    private Validator validator;

	/**
	 * Constructor using injection for creation.
	 * 
	 * @param businessRepository
	 * @param checkInRepository
	 * @param spotRepository
	 * @param transformer
	 * @param channelController
	 * @param objectMapper
	 * @param validator
	 */
	@Inject
	public CheckInController(BusinessRepository businessRepository, CheckInRepository checkInRepository, SpotRepository spotRepository,
			Transformer transformer, ChannelController channelController, ObjectMapper objectMapper, Validator validator) {
		this.businessRepo = businessRepository;
		this.checkInRepo = checkInRepository;
		this.channelCtrl = channelController;
		this.barcodeRepo = spotRepository;
		this.transform = transformer;
		this.mapper = objectMapper;
		this.validator = validator;
	}
	

	

    /**
     * Get spot data for a given barcode.
     * 
     * @param barcode
     * @return SpotDTO containing all relevant data for the client
     * @throws NotFoundException
     */
    public SpotDTO getSpotInformation(String barcode) throws NotFoundException {
    	if(barcode == null || barcode.isEmpty() )
    		return null;
    	SpotDTO spotDto = new SpotDTO();
    	Spot spot = barcodeRepo.getByProperty("barcode", barcode);
    	if(spot == null )
    		throw new NotFoundException("barcode unknown");
    	
    	Business business = businessRepo.getByKey(spot.getBusiness());
    	
    	    	
    	spotDto.setBarcode(barcode);
    	spotDto.setName(spot.getName());
    	spotDto.setBusiness(business.getName());
    	spotDto.setBusinessId(business.getId());
    	spotDto.setPayments(business.getPaymentMethods());
    	spotDto.setGroupTag(spot.getGroupTag());
    	
		return spotDto ;
    }

	public CheckInDTO createCheckIn(CheckInDTO checkInDto) {
		String message = null;
		
		if(checkInDto == null ) {
			throw new RuntimeException("checkin data is empty");
		}
		if( checkInDto.getStatus()==null || checkInDto.getStatus() != CheckInStatus.INTENT ) {
			throw new RuntimeException("checkin status should be INTENT but is " + checkInDto.getStatus());
		}
			
		// set values for domain object
		Spot spot = barcodeRepo.getByProperty("barcode", checkInDto.getSpotId());
		
    	if(spot == null )
    		throw new NotFoundException("barcode unknown");
    	
    	Business business = businessRepo.getByKey(spot.getBusiness());
    	
    	CheckIn checkIn = new CheckIn();
    	
    	String checkInId = IdHelper.generateId();
		checkIn.setBusiness(business.getKey());
		checkIn.setSpot(spot.getKey());
		checkIn.setUserId(checkInId);
		checkIn.setStatus(CheckInStatus.CHECKEDIN);
		checkIn.setCheckInTime(new Date());
		checkIn.setDeviceId(checkInDto.getDeviceId());
		checkIn.setNickname(checkInDto.getNickname());

		// validation 
		Set<ConstraintViolation<CheckIn>> constraintViolations = validator.validate(checkIn, Default.class, CheckInStep2.class);
		if( !constraintViolations.isEmpty() )  {
			// constraint violations occurred setting status and logging error
			logger.info("CheckIn validation failed. Message(s):");
			for (ConstraintViolation<CheckIn> violation : constraintViolations) {
				
				logger.info( violation.getPropertyPath() + ": " +violation.getMessage() );
				if(violation.getPropertyPath().toString().equals("nickname")) {
					
					try {
						message = mapper.writeValueAsString(new ErrorDTO("checkInErrorNickname", "3","20"));
						logger.info("Writing json message:" + message);
					} catch (JsonGenerationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					throw new RuntimeException(message);
				}
				else {
					try {
						message = mapper.writeValueAsString(new ErrorDTO("checkInError", violation.getPropertyPath().toString() + " " + violation.getMessage()));
						logger.info("Writing json message:" + message);
					} catch (JsonGenerationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					throw new RuntimeException(message);
				}
					
			}
			
			return null;
		}			
 		
		List<CheckIn> checkInsAtSpot = getCheckInsBySpot(checkIn.getSpot());
		int checkInCount = 1;
		if(checkInsAtSpot != null) {
			Iterator<CheckIn> it = checkInsAtSpot.iterator();
			while(it.hasNext()) {
				checkInCount++;
				CheckIn next = it.next();
				
				if(next.getNickname().equals(checkIn.getNickname() ) ) {
					logger.info("Error: checkin with duplicate nickname tried: "+ checkIn.getNickname());
					try {
						message = mapper.writeValueAsString(new ErrorDTO("checkInErrorNicknameExists", ""));
					} catch (JsonGenerationException e) {
						e.printStackTrace();
					} catch (JsonMappingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					//abort checkin
					throw new RuntimeException(message);
				}
				
				
			}
		}

		checkInRepo.saveOrUpdate(checkIn);
		checkInDto.setUserId(checkInId);
		checkInDto.setStatus(CheckInStatus.CHECKEDIN);
		
		SpotStatusDTO spotData = new SpotStatusDTO();
		
		spotData.setId(spot.getId());
		// we already have all other checkins in a list, so we count them and add one for the new checkin
		
		spotData.setCheckInCount(checkInCount);
		
		List<MessageDTO> messages = new ArrayList<MessageDTO>();		
		
		// add the messages we want to send as one package
		messages.add(new MessageDTO("spot","update",spotData));
		messages.add(new MessageDTO("checkin","new", transform.toStatusDto(checkIn)));
		
		// send the messages
		try {
			channelCtrl.sendMessagesToAllClients(business.getId(), messages);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return checkInDto;
	}

	/**
	 * Step 3 - I (optional) Shows a list of all checkedIn Users at the same
	 * spot.
	 * 
	 * @param checkInId
	 * @return Map<String,String> - key is another users id - value is another
	 *         users nickname If no other users at this spot exist
	 *         <code>null</code>.
	 */
	public List<User> getUsersAtSpot(String spotId, String checkInId) {
		if(spotId == null || spotId.isEmpty()) 
			return null;
		
		List<User> usersAtSpot = null;
		
		Spot spot = barcodeRepo.getByProperty("barcode", spotId);
		
    	if(spot == null )
    		throw new NotFoundException("barcode unknown");
		
		List<CheckIn> checkInsAtSpot = getCheckInsBySpot(spot.getKey());
		
		if (checkInsAtSpot != null && !checkInsAtSpot.isEmpty()) {
			usersAtSpot = new ArrayList<User>();
			
			// Other users at this spot exist.
			for (CheckIn checkIn : checkInsAtSpot) {
				
				if(!checkIn.getUserId().equals(checkInId) && isPaymentLinkPossible(checkIn)) {
					User user = new User();
					
					user.setUserId(checkIn.getUserId());
					user.setNickname(checkIn.getNickname());
					
					usersAtSpot.add(user);
				}
			}
		}

		return usersAtSpot;
	}
	
	/**
	 * Update existing checkIn
	 * 
	 * @param checkInId
	 * @param checkInDto
	 * @return
	 */
	public CheckInDTO updateCheckIn(String checkInId, CheckInDTO checkInDto) {
		CheckIn checkInUser = checkInRepo.getByProperty("userId", checkInId);
		boolean save = false;
		if(checkInUser == null )
			throw new NotFoundException("Unknown checkInId");
		
		if(checkInUser.getLinkedUserId() != checkInDto.getLinkedCheckInId()) {
			CheckIn checkInLinkedUser = checkInRepo.getByProperty("userId", checkInDto.getLinkedCheckInId());
			if(checkInLinkedUser == null )
				throw new RuntimeException("linkedCheckInId unknown");
			
			if(checkInUser != null && checkInLinkedUser != null) {
				if (checkInUser.getStatus() == CheckInStatus.CHECKEDIN && isPaymentLinkPossible(checkInLinkedUser)) {
					checkInUser.setLinkedUserId(checkInLinkedUser.getUserId());
					save = true;
				}
				else
//					return "Can't link to this user";
				return null;
			}
		}
		//TODO: allow updating of other fields, like nickname

		if(save) {
			checkInRepo.saveOrUpdate(checkInUser);
		}
			
//		return "OK";
		return checkInDto;
	}

	/**
	 * User clicked cancel on checkIn confirm page. Deletes this checkIn form
	 * datastore.
	 * 
	 * @param userId
	 *            User issuing this request.
	 */
	public void cancelCheckIn(String userId) {
		//Don't return something. User is not really interested if check in cancel failed.
		//System has to deal with this.
		CheckIn chkin = checkInRepo.getByProperty("userId", userId);
		
		if (chkin == null) { // CheckIn not found for this userId
			logger.info("Error: Recieved cancel for CheckIn with userId {}, but this userId was not found.", userId);
			return;
		}
			
		if (chkin.getStatus() == CheckInStatus.INTENT) {
			logger.info("Cancel CheckIn with userId {}", userId);
			checkInRepo.delete(chkin);
			
			SpotStatusDTO spotData = new SpotStatusDTO();
			
			spotData.setId(chkin.getSpot().getId());

			spotData.setCheckInCount(countCheckInsAtSpot(chkin.getSpot()));
			
			// send the message with the updated data field
			try {
				channelCtrl.sendMessageToAllClients(chkin.getBusiness().getId(), "spot", "update", spotData);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else {
			// Error handling
			logger.info("Error: Recieved cancel for CheckIn with userId {}, but status was not INTENT.", userId);
			
		}
	}
	
	/**
	 * Load checkin.
	 * @param checkInId
	 * 			Id of CheckIn to load	
	 * @return
	 * 		found checkin data otherwise <code>null</code>
	 */
	public CheckInDTO getCheckInAsDTO(String checkInId) {
		return transform.checkInToDto(getCheckIn(checkInId));
	}
	
	/**
	 * Load checkin.
	 * @param checkInId
	 * 			Id of CheckIn to load	
	 * @return
	 * 		found checkin otherwise <code>null</code>
	 */
	public CheckIn getCheckIn(String checkInId) {
		CheckIn checkIn = checkInRepo.getByProperty("userId", checkInId);		
		if(checkIn == null)
			// returns with code 404(Not Found)
			throw new NotFoundException("unknown checkInId: " + checkInId);
		return checkIn;
	}
	
	/**
	 * Return other checkins at the same spot.
	 * 
	 * @param chkin A user
	 * @return All users at spot checkedin.
	 */
	private List<CheckIn> getOtherChekIns(CheckIn chkin)
	{
		List<CheckIn> otherCheckIns = null;
		
		List<CheckIn> checkInsAtSpot = getCheckInsBySpot(chkin.getSpot());
		
		if (checkInsAtSpot != null && checkInsAtSpot.size() > 0) {
			otherCheckIns = new ArrayList<CheckIn>();
			
			// Other users at this table exist.
			for (CheckIn checkIn : checkInsAtSpot) {
				
				if(!checkIn.getUserId().equals(chkin.getUserId())
						&& (checkIn.getStatus() == CheckInStatus.CHECKEDIN || checkIn.getStatus() == CheckInStatus.ORDER_PLACED)) {
					otherCheckIns.add(checkIn);
				}
			}
		}
		return otherCheckIns;
	}
	
	/**
	 * Helper method for condition checks of an existing CheckIn
	 * 
	 * @param CheckIn object which needs to be checked available linking
	 * @return TRUE if all conditions are met so this existing checked in user available for payment linking
	 */
	private boolean isPaymentLinkPossible(CheckIn existingCheckIn) {
		
		return (existingCheckIn.getLinkedUserId() == null && 
				existingCheckIn.getStatus() != CheckInStatus.INTENT && 
				existingCheckIn.getStatus() != CheckInStatus.PAYMENT_REQUEST);
	}

	/**
	 * Delete the checkIn from database only if there are no orders placed or payment requested.
	 * 
	 * @param checkInId
	 */
	public void checkOut(String checkInId) {
		CheckIn checkIn = getCheckIn(checkInId);
		
		if(checkIn.getStatus() == CheckInStatus.ORDER_PLACED || checkIn.getStatus() == CheckInStatus.PAYMENT_REQUEST) {
			// return with return code 403(Forbidden)
			throw new WebApplicationException(Response.noContent().status(Response.Status.FORBIDDEN).build());
		}
		else {
			List<MessageDTO> messages = new ArrayList<MessageDTO>();
			
			checkInRepo.ofy().delete(checkInRepo.ofy().query(Order.class).filter("status", "CART").listKeys());
					
			checkInRepo.delete(checkIn);
			SpotStatusDTO spotData = new SpotStatusDTO();
			
			spotData.setId(checkIn.getSpot().getId());
			
			spotData.setCheckInCount(countCheckInsAtSpot(checkIn.getSpot()));
			
			messages.add(new MessageDTO("spot", "update", spotData));
			messages.add(new MessageDTO("checkin","delete", transform.toStatusDto(checkIn)));
			try {
				channelCtrl.sendMessagesToAllClients(checkIn.getBusiness().getId(), messages);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}			
	}

	public Collection<CheckInStatusDTO> getCheckInStatusesBySpot(Long businessId, Long spotId) {
		return transform.toStatusDtos( getCheckInsBySpot(businessId, spotId));
	}
	
	private int countCheckInsAtSpot(Key<Spot> spotKey) {
		return checkInRepo.ofy().query(CheckIn.class).filter("spot", spotKey).filter("archived", false).count();
	}
	
	private List<CheckIn> getCheckInsBySpot(Key<Spot> spotKey) {
		return checkInRepo.ofy().query(CheckIn.class).filter("spot", spotKey).filter("archived", false).list();
	}

	private List<CheckIn> getCheckInsBySpot(Long businessId, Long spotId) {
		// Check if the business exists.
		Business business = businessRepo.getById(businessId);
		if(business == null) {
			logger.error("CheckIns cannot be retrieved, businessId unknown: " + businessId);
			throw new NotFoundException("CheckIns cannot be retrieved, businessId unknown: " + businessId);
		}
		
		return getCheckInsBySpot(Spot.getKey(business.getKey(), spotId));
	}
}
