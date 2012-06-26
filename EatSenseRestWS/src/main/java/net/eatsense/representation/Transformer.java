package net.eatsense.representation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.eatsense.domain.Bill;
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
import com.googlecode.objectify.Key;

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
	
	public BillDTO billToDto(Bill bill) {
		if(bill == null)
			return null;
		
		BillDTO billData = new BillDTO();
		
		billData.setTotal(bill.getTotal());
		billData.setCheckInId(bill.getCheckIn().getId());
		billData.setCleared(bill.isCleared());
		billData.setId(bill.getId());
		billData.setTime(bill.getCreationTime());
		billData.setPaymentMethod(bill.getPaymentMethod());
		
		return billData;
	}
	

	public List<OrderDTO> ordersToDto(List<Order> orders ) {
		List<OrderDTO> dtos = new ArrayList<OrderDTO>();
		if(orders == null || orders.isEmpty()) {
			logger.info("orders are null or empty");
		}
		else {
			List<Key<Product>> productKeys = new ArrayList<Key<Product>>(orders.size());
			List<Key<OrderChoice>> choiceKeys = new ArrayList<Key<OrderChoice>>();
			for (Order order : orders) {
				// Add keys to lists for loading.
				productKeys.add(order.getProduct());
				choiceKeys.addAll(order.getChoices());
			}
			Map<Key<Product>, Product> productMap = productRepo.getByKeysAsMap(productKeys);
			Map<Key<OrderChoice>, OrderChoice> choicesMap = orderChoiceRepo.getByKeysAsMap(choiceKeys);
			
			for (Order order : orders) {
				OrderDTO orderDto = new OrderDTO();
				orderDto.setId(order.getId());
				orderDto.setAmount(order.getAmount());
				orderDto.setOrderTime(order.getOrderTime());
				orderDto.setStatus(order.getStatus());
				orderDto.setComment(order.getComment());
				orderDto.setCheckInId(order.getCheckIn().getId());
				orderDto.setProduct(productToDtoOmitChoices(productMap.get(order.getProduct())));
				
				if( !order.getChoices().isEmpty() ) {
					ArrayList<ChoiceDTO> choiceDtos = new ArrayList<ChoiceDTO>();
					
					for (Key<OrderChoice> choiceKey : order.getChoices()) {
						choiceDtos.add(choiceToDto( choicesMap.get(choiceKey).getChoice()));
					}
					orderDto.getProduct().setChoices(choiceDtos);
				}
					
				dtos.add( orderDto );
			}
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
		
		Collection<OrderChoice> orderChoices = orderChoiceRepo.getByKeys(order.getChoices());
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
			List<Key<Choice>> choiceKeys = new ArrayList<Key<Choice>>();
			//Collect all choices for a batch load.
			for( Product p : products) {
				if(p.getChoices() != null && !p.getChoices().isEmpty() ) {
					choiceKeys.addAll(p.getChoices());
				}
			}
			//Load all choices with one query.
			Map<Key<Choice>, Choice> choiceMap = choiceRepo.getByKeysAsMap(choiceKeys);
			
			//Build dto objects.
			for( Product p : products) {
				ProductDTO productDto = productToDtoOmitChoices(p);
				if( p.getChoices() != null && !p.getChoices().isEmpty() ) {
					ArrayList<ChoiceDTO> choiceDtos = new ArrayList<ChoiceDTO>();
					
					for (Key<Choice> choiceKey : p.getChoices())  {
						choiceDtos.add( choiceToDto( choiceMap.get(choiceKey)) );
					}
					
					productDto.setChoices(choiceDtos);
				}
				productDTOs.add(productDto);
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
		return new ProductDTO(product);
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
		if(choice.getParentChoice() != null)
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
				choiceDtos.add( choiceToDto(choice) );
			}
		}
		
		return choiceDtos; 		
	}
	
	public CheckInDTO checkInToDto(CheckIn checkIn, boolean loadAll) {
		if(checkIn == null)
			return null;
		
		CheckInDTO dto = new CheckInDTO();
		dto.setDeviceId(checkIn.getDeviceId());
		dto.setLinkedCheckInId(checkIn.getLinkedUserId());
		dto.setNickname(checkIn.getNickname());
		dto.setUserId(checkIn.getUserId());
		
		if(loadAll) {
			Business business = businessRepo.getByKey(checkIn.getBusiness());
			dto.setBusinessName(business.getName());
		}
		
		dto.setBusinessId(checkIn.getBusiness().getId());
		dto.setStatus(checkIn.getStatus());
		if(loadAll) {
			Spot spot = spotRepo.getByKey(checkIn.getSpot());
			dto.setSpot(spot.getName());
			dto.setSpotId(spot.getBarcode());
		}
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
