package net.eatsense.controller;

import java.util.ArrayList;
import java.util.List;

import net.eatsense.domain.CheckIn;
import net.eatsense.domain.CheckInStatus;
import net.eatsense.domain.Order;
import net.eatsense.domain.Request;
import net.eatsense.domain.Restaurant;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.CheckInRepository;
import net.eatsense.persistence.MenuRepository;
import net.eatsense.persistence.OrderRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.RequestRepository;
import net.eatsense.persistence.RestaurantRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.cockpit.SpotCockpitDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;


/**
 * @author Frederik Reifschneider
 *
 */
public class RestaurantController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private RestaurantRepository restaurantRepo;
	private MenuRepository menuRepo;
	private ProductRepository productRepo;
	private CheckInRepository checkInRepo;
	private SpotRepository spotRepo;
	private OrderRepository orderRepo;
	private RequestRepository requestRepo;
	private ChannelController channelCtrl;
	
	@Inject
	public RestaurantController(RequestRepository rr, RestaurantRepository r, MenuRepository mr, ProductRepository pr, CheckInRepository cr, SpotRepository sr, OrderRepository or, ChannelController channelCtrl) {
		this.requestRepo = rr;
		this.orderRepo = or;
		this.spotRepo = sr;
		this.checkInRepo = cr;
		this.restaurantRepo = r;
		this.menuRepo = mr;
		this.productRepo = pr;
		this.channelCtrl = channelCtrl;
	}
	
	public List<SpotCockpitDTO> getSpotDtos(Long restaurantId){
		List<Spot> allSpots = spotRepo.getByParent(Restaurant.getKey(restaurantId));
		//Restaurant restaurant = restaurantRepo.getById(restaurantId);
		List<SpotCockpitDTO> spotDtos = new ArrayList<SpotCockpitDTO>();
		for (Spot spot : allSpots) {
			SpotCockpitDTO spotDto = new SpotCockpitDTO();
			spotDto.setId(spot.getId());
			spotDto.setName(spot.getName());
			spotDto.setGroupTag(spot.getGroupTag());
			spotDto.setCheckInCount(checkInRepo.ofy().query(CheckIn.class).filter("spot", spot.getKey()).filter("status !=", CheckInStatus.PAYMENT_REQUEST).count());
			Request request = requestRepo.ofy().query(Request.class).filter("spot",spot.getKey()).order("-receivedTime").get();
			
			if(request != null) {
				spotDto.setStatus(request.getStatus());
			}
			
			spotDtos.add(spotDto);
		}
		try {
			logger.debug(channelCtrl.sendMessage("123", "spot", "UPDATE", spotDtos));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return spotDtos;
	}
	
}
