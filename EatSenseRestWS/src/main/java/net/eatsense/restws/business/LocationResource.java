package net.eatsense.restws.business;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import net.eatsense.auth.Role;
import net.eatsense.controller.ChannelController;
import net.eatsense.controller.LocationController;
import net.eatsense.controller.SpotController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.persistence.DashBoarditemRepository;
import net.eatsense.representation.ImageDTO;
import net.eatsense.representation.LocationProfileDTO;
import net.eatsense.representation.SpotDTO;
import net.eatsense.representation.SpotsData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.core.ResourceContext;

/**
 * Sub-resource representing {@link Business} entities.
 * @author Nils Weiher
 *
 */
public class LocationResource {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	@Context
	ResourceContext resourceContext;
	@Context
	HttpServletRequest servletRequest;
	
	private Business business;
	private LocationController businessCtrl;
	private Account account;
	private final Provider<ChannelController> channelCtrlProvider;
	private SpotController spotController;
	
	public void setBusiness(Business business) {
		this.business = business;
	}
	
	public void setAccount(Account account) {
		this.account = account;
	}
	
	@Inject
	public LocationResource(LocationController businessCtrl, SpotController spotCtrl, Provider<ChannelController> channelCtrlProvider) {
		super();
		this.channelCtrlProvider = channelCtrlProvider;
		this.businessCtrl = businessCtrl;
		this.spotController = spotCtrl;
	}

	@GET
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public LocationProfileDTO getBusiness(@QueryParam("countSpots")boolean countSpots) {
		if(business == null)
			throw new NotFoundException();
		if(countSpots) {
			businessCtrl.setSpotCount(business);
		}
		return new LocationProfileDTO(business);
	}
	
	@PUT
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public LocationProfileDTO updateBusinessProfile(LocationProfileDTO businessData) {
		//Update Business synchronizes data between the entity and transfer object.
		businessCtrl.updateBusiness(business, businessData);
		return new LocationProfileDTO(business);
	}
	
	@DELETE
	@RolesAllowed({Role.COMPANYOWNER})
	public void deleteBusiness() {
		businessCtrl.trashBusiness(business, account);
	}
	
	
	@POST
	@Path("channels")
	@Produces("text/plain; charset=UTF-8")
	@Consumes("application/x-www-form-urlencoded; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public String createChannel( @FormParam("clientId") String clientId ) {
		Optional<Integer> timeout = Optional.of( Integer.valueOf(System.getProperty("net.karazy.channels.cockpit.timeout")));
		
		String token = channelCtrlProvider.get().createCockpitChannel(business,account, clientId, timeout);
		if(token == null)
			throw new NotFoundException();
		return token;
	};
	
	@DELETE
	@Path("channels/{clientId}")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public void removeChannel( @PathParam("clientId") String clientId ) {
		channelCtrlProvider.get().disconnectCockpitChannel(business, clientId);
	}
	
	@POST
	@Path("images/{id}")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public ImageDTO updateOrCreateImage(@PathParam("id") String imageId, ImageDTO imageData) {
		return businessCtrl.updateBusinessImage(account, business, imageData);
	}
	
	@PUT
	@Path("images")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public void createMultipleImages(@PathParam("id") String imageId, List<ImageDTO> images) {
		businessCtrl.updateBusinessImages(account, business, images);
	}
	
	
	
	@DELETE
	@Path("images/{id}")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public void removeImage(@PathParam("id") String imageId) {
		if(!businessCtrl.removeBusinessImage(business, imageId)) {
			throw new NotFoundException(String.format("No image found with id '%s'", imageId));
		}
	}

	
	@Path("menus")
	public MenusResource getMenusResource() {
		MenusResource menusResource = resourceContext.getResource(MenusResource.class);
		menusResource.setBusiness(business);
		return menusResource;
	}
	
	@Path("products")
	public ProductsResource getProductsResource() {
		ProductsResource resource = resourceContext.getResource(ProductsResource.class);
		resource.setBusiness(business);
		resource.setAccount(account);
		return resource;
	}
	
	@Path("choices")
	public ChoicesResource getChoicesResource() {
		ChoicesResource resource = resourceContext.getResource(ChoicesResource.class);
		resource.setBusiness(business);
		return resource;
	}

	@Path("orders")
	public OrdersResource getOrdersResource() {
		OrdersResource ordersResource = resourceContext.getResource(OrdersResource.class);
		ordersResource.setBusiness(business);
		return ordersResource;
	}
	
	@Path("checkins")
	public CheckInsResource getCheckInsResource() {
		CheckInsResource checkInsResource = resourceContext.getResource(CheckInsResource.class);
		
		checkInsResource.setBusiness(business);
		
		return checkInsResource;
	}
	
	@Path("spots")
	public SpotsResource getSpotsResource() {
		SpotsResource spotsResource = resourceContext.getResource(SpotsResource.class);
		
		spotsResource.setBusiness(business);
		
		return spotsResource;
	}
	
	
	@Path("bills")
	public BillsResource getBillsResource() {
		BillsResource billsResource = resourceContext.getResource(BillsResource.class);
		
		billsResource.setBusiness(business);
		
		return billsResource;
	}
	
	@Path("requests")
	public RequestsResource getRequestsResource() {
		RequestsResource requestsResource = resourceContext.getResource(RequestsResource.class);
		
		requestsResource.setBusiness(business);
		return requestsResource;
	}
	
	@Path("areas")
	public AreasResource getAreasResource() {
		AreasResource areasResource = resourceContext.getResource(AreasResource.class);
		areasResource.setAccount(account);
		areasResource.setBusiness(business);
		return areasResource;
	}
	
	@Path("infopages")
	public InfoPagesResource getInfoPagesResource() {
		InfoPagesResource infoPagesResource = resourceContext.getResource(InfoPagesResource.class);
		
		infoPagesResource.setBusiness(business);
		infoPagesResource.setAccount(account);
		
		return infoPagesResource;
	}
	
	@Path("documents")
	public DocumentsResource getDocumentResource() {
		DocumentsResource docResource = resourceContext.getResource(DocumentsResource.class);
		
		docResource.setBusiness(business);
		docResource.setAccount(account);
		
		return docResource;
	}
	
	@Path("spotsdata")
	@GET
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public List<SpotDTO> getSpots(@QueryParam("areaId") long areaId, @QueryParam("welcome") boolean welcome) {
		return businessCtrl.getSpots(business.getKey(), areaId, welcome, false);
	}
	
	@Path("spotsdata")
	@POST
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public SpotDTO createSpot(SpotDTO spotData) {
		return businessCtrl.createSpot(business.getKey(), spotData, false);
	}
	
	@Path("spotsdata/{spotId}")
	@PUT
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public SpotDTO updateSpot(@PathParam("spotId") long spotId, SpotDTO spotData) {
		return new SpotDTO(businessCtrl.updateSpot(businessCtrl.getSpot(business.getKey(), spotId), spotData));
	}
	
	@Path("spotsdata")
	@PUT
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public List<SpotDTO> modifySpots(SpotsData spotsData) {
		if(spotsData.getIds() == null || spotsData.getIds().isEmpty()) {
			return Lists.transform(spotController.createSpots(business.getKey(), spotsData), SpotDTO.toDTO );
		}
		else if(spotsData.isRemove()) {
			return Lists.transform(spotController.deleteSpots(business.getKey(), spotsData.getIds(), account), SpotDTO.toDTO );
		}
		else {
			return Lists.transform(spotController.updateSpots(business.getKey(), spotsData.getIds(), spotsData.isActive()), SpotDTO.toDTO );
		}
	}
	
	@Path("spotsdata/{spotId}")
	@DELETE
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public void deleteSpot(@PathParam("spotId") long spotId) {
		businessCtrl.trashSpot(businessCtrl.getSpot(business.getKey(), spotId), account);
	}
	
	@Path("spotsdata/{spotId}")
	@GET
	@Produces("application/json; charset=UTF-8")
	@RolesAllowed({Role.COCKPITUSER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public SpotDTO getSpot(@PathParam("spotId") long spotId) {
		return new SpotDTO(businessCtrl.getSpot(business.getKey(), spotId));
	}
	
	@Path("subscriptions")
	public SubscriptionsResource getSubscriptionsResource() {
		SubscriptionsResource resource = resourceContext.getResource(SubscriptionsResource.class);
		resource.setBusiness(business);
		return resource;
	}

	@Path("reports")
	public ReportsResource getReportsResource() {
		ReportsResource resource = resourceContext.getResource(ReportsResource.class);
		resource.setLocation(business);
		return resource;
	}
	
	@Path("dashboarditems")
	public DashboardItemsResource getDashboardItemsResource() {
		DashboardItemsResource resource = resourceContext.getResource(DashboardItemsResource.class);
		resource.setLocation(business);
		
		return resource;
	}
	
	@Path("feedback")
	public FeedbackResource getFeedbackResource() {
		FeedbackResource resource = resourceContext.getResource(FeedbackResource.class);
		resource.setLocation(business);
		
		return resource;
	}
	
	@Path("feedbackforms")
	public FeedbackFormsResource getFeedbackFormsResource() {
		FeedbackFormsResource resource = resourceContext.getResource(FeedbackFormsResource.class);
		resource.setLocation(business);
		
		return resource;
	}
	
}
