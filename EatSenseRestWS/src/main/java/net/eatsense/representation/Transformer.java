package net.eatsense.representation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;
import net.eatsense.domain.Choice;
import net.eatsense.domain.Order;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.Product;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.BusinessRepository;
import net.eatsense.persistence.ChoiceRepository;
import net.eatsense.persistence.OrderChoiceRepository;
import net.eatsense.persistence.ProductRepository;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.representation.cockpit.CheckInStatusDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Class for transforming from/to Data Transfer Objects (DTOs)
 * 
 * @author Nils Weiher
 *
 */
@Singleton
public class Transformer {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private ChoiceRepository choiceRepo;
	private ProductRepository productRepo;
	private OrderChoiceRepository orderChoiceRepo;
	private BusinessRepository businessRepo;
	private SpotRepository spotRepo;
	
	
	@Inject
	private Transformer(SpotRepository spotRepo, ChoiceRepository choiceRepo, ProductRepository productRepo, OrderChoiceRepository orderChoiceRepo, BusinessRepository businessRepo) {
		super();
		this.choiceRepo = choiceRepo;
		this.productRepo = productRepo;
		this.orderChoiceRepo = orderChoiceRepo;
		this.businessRepo = businessRepo;
		this.spotRepo = spotRepo;
	}

	public List<OrderDTO> ordersToDto(List<Order> orders ) {
		if(orders == null || orders.isEmpty()) {
			logger.error("orders list is null or empty");
		}
			
		List<OrderDTO> dtos = new ArrayList<OrderDTO>();
		for (Order order : orders) {
			dtos.add(orderToDto( order ));
		}
		
		return dtos;
	}
	
	public OrderDTO orderToDto(Order order) {
		if(order == null || order.getId() == null) {
			logger.error("order is null or order id is null");
			return null;
		}
			
		OrderDTO dto = new OrderDTO();
		Product product = productRepo.getByKey(order.getProduct());
		if(product == null) {
			logger.error("product not found: " +order.getProduct());
			return null;
		}
			
		dto.setProduct( productToDtoOmitChoices( product ) );
		
		List<OrderChoice> orderChoices = orderChoiceRepo.getByParent(order.getKey());
		if(orderChoices != null && !orderChoices.isEmpty()) {
			ArrayList<ChoiceDTO> choiceDtos = new ArrayList<ChoiceDTO>();
			
			for (OrderChoice orderChoice : orderChoices) {
				choiceDtos.add(choiceToDto(orderChoice.getChoice()));
			}
			dto.getProduct().setChoices(choiceDtos);
			
		}
		else {
			dto.getProduct().setChoices(getChoicesForProduct(product));
		}
		dto.setId(order.getId());
		dto.setAmount(order.getAmount());
		dto.setOrderTime(order.getOrderTime());
		dto.setStatus(order.getStatus());
		dto.setComment(order.getComment());
		dto.setCheckInId(order.getCheckIn().getId());
				
		return dto;
	}

	public List<ProductDTO> productsToDto(List<Product> products) {
		List<ProductDTO> productDTOs = new ArrayList<ProductDTO>();
		if(products != null) {
			for( Product p : products) {
				ProductDTO dto = productToDto(p);
				productDTOs.add(dto);
			}	
		}
		return productDTOs;
	}

	public ProductDTO productToDto(Product product) {
		if(product == null)
			return null;
		ProductDTO dto = productToDtoOmitChoices(product);
		 
		dto.setChoices(getChoicesForProduct(product));
		 
		return dto;
	}
	
	public ProductDTO productToDtoOmitChoices(Product product) {
		if(product == null)
			return null;
		ProductDTO dto = new ProductDTO();
		 
		 dto.setId(product.getId());		 
		 dto.setName( product.getName() );
		 dto.setLongDesc( product.getLongDesc() );
		 dto.setShortDesc( product.getShortDesc() );
		 dto.setPrice( product.getPrice() );
		 
		return dto;
	}
	
	public ChoiceDTO choiceToDto(Choice choice) {
		if(choice == null)
			return null;
		
		ChoiceDTO dto = new ChoiceDTO();
		
		dto.setId(choice.getId());

		dto.setIncluded(choice.getIncludedChoices());
		dto.setMaxOccurence(choice.getMaxOccurence());
		dto.setMinOccurence(choice.getMinOccurence());
		dto.setOverridePrice(choice.getOverridePrice());
		dto.setParent(choice.getParentChoice().getId());
		
		dto.setPrice(choice.getPrice() == null ? 0 : choice.getPrice());
		dto.setText(choice.getText());
		
		if( choice.getOptions() != null && !choice.getOptions().isEmpty() ) {		
			dto.setOptions(choice.getOptions());						
		}
		//	else if (choice.getAvailableProducts() != null && !choice.getAvailableProducts().isEmpty()) {
		//		ArrayList<ProductOption> options = new ArrayList<ProductOption>();
		//		Map<Key<Product>,Product> products =  productRepo.getOfy().get(choice.getAvailableProducts());
		//		
		//		for (Product choiceProduct : products.values() ) {
		//			options.add(new ProductOption(choiceProduct.getName(), choiceProduct.getPrice(), choiceProduct.getId()));
		//		}
		//		
		//		dto.setOptions(options);
		//	}
	
		
		return dto;
	}
	
	public Collection<ChoiceDTO> getChoicesForProduct(Product p)
	{
		ArrayList<ChoiceDTO> choiceDtos = null;
		
		Collection<Choice> choices = null;
		
		if(p != null && p.getChoices() != null && !p.getChoices().isEmpty()) 
			choices = choiceRepo.getByKeys(p.getChoices());
		
		if(choices != null && !choices.isEmpty())  {
			choiceDtos = new ArrayList<ChoiceDTO>();
			
			for (Choice choice : choices)  {
				ChoiceDTO dto = new ChoiceDTO();
				
				dto = choiceToDto(choice);

				choiceDtos.add( dto );
				
			}
		}
		
		return choiceDtos; 		
	}
	
	public CheckInDTO checkInToDto(CheckIn checkIn) {
		if(checkIn == null)
			return null;
		
		CheckInDTO dto = new CheckInDTO();
		dto.setDeviceId(checkIn.getDeviceId());
		dto.setLinkedCheckInId(checkIn.getLinkedUserId());
		dto.setNickname(checkIn.getNickname());
		dto.setUserId(checkIn.getUserId());
		Business business;
		business = businessRepo.getByKey(checkIn.getBusiness());
		dto.setBusinessId(business.getId());
		dto.setBusinessName(business.getName());
		dto.setStatus(checkIn.getStatus());
		Spot spot = spotRepo.getByKey(checkIn.getSpot());
		dto.setSpot(spot.getName());
		dto.setSpotId(spot.getBarcode());
		
		return dto;
	}

	public Collection<CheckInStatusDTO> toStatusDtos(List<CheckIn> checkIns) {
		if(checkIns == null) {
			return null;
		}
		ArrayList<CheckInStatusDTO> checkInStatuses = new ArrayList<CheckInStatusDTO>();
		
		for (CheckIn checkIn : checkIns) {
			checkInStatuses.add(toStatusDto(checkIn));
		}
		
		return checkInStatuses;
	}

	public CheckInStatusDTO toStatusDto(CheckIn checkIn) {
		if(checkIn == null)
			return null;
			
		CheckInStatusDTO checkInStatus = new CheckInStatusDTO();
		
		checkInStatus.setId(checkIn.getId());
		checkInStatus.setNickname(checkIn.getNickname());
		checkInStatus.setStatus(checkIn.getStatus());
		checkInStatus.setCheckInTime(checkIn.getCheckInTime());
		checkInStatus.setSpotId(checkIn.getSpot().getId());
		
		return checkInStatus;
	}
}
