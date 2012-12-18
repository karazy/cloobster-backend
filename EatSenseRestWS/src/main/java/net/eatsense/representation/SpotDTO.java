package net.eatsense.representation;

import java.util.Collection;

import javax.validation.constraints.NotNull;

import net.eatsense.domain.Area;
import net.eatsense.domain.Location;
import net.eatsense.domain.Spot;
import net.eatsense.domain.embedded.PaymentMethod;
import net.eatsense.validation.CreationChecks;
import net.eatsense.validation.ImportChecks;

import org.apache.bval.constraints.NotEmpty;

import com.google.common.base.Function;



/**
 * Represents a pojo for data transfer of a location in e. g. a restaurant.
 * A spot is a place with a barcode where a user is able to check in.
 * 
 * @author Nils Weiher
 *
 */
/**
 * @author fred
 *
 */
public class SpotDTO {
	
	/**
	 * Barcode identifying this spot.
	 */
	@NotNull(groups={ImportChecks.class})
	@NotEmpty(groups={ImportChecks.class})
	private String barcode;
	
	private String qrImageUrl;
	
	private Long id;
	
	/**
	 * A human readable identifier for the spot where the barcode is located.
	 * E.g. Table no. 4, Lounge etc.
	 */
	@NotNull
	@NotEmpty
	private String name;
	
	private String business;
	
	private String theme;
	
	private Long businessId;
	
	private Collection<PaymentMethod> payments;
	
	private String currency;
	
	private boolean active;
	
	private String areaName;
	private String areaDescription;
	
	private boolean checked;
	
	/**
	 * Url to logo.
	 */
	private String logoUrl;
	
	/**
	 * Url to image of header displayed in App.
	 */
	private String headerUrl;
	
	@NotNull(groups= {CreationChecks.class})
	private Long areaId;
	
	public SpotDTO(Spot spot) {
		super();
		if(spot == null)
			return;
		
		if(spot.getBusiness() != null) {
			this.businessId = spot.getBusiness().getId();
		}
		this.id = spot.getId();
		this.active = spot.isActive();
		this.barcode = spot.getBarcode();
		this.qrImageUrl = spot.getQrImageUrl();
		this.name = spot.getName();
		this.groupTag = spot.getBarcode();
		if(spot.getArea()!=null) {
			this.areaId = spot.getArea().getId();
		}
	}
	
	public SpotDTO(Spot spot, Location business, Area area) {
		this(spot);
		if(business != null) {
			this.business = business.getName();			
			this.setBusiness(business.getName());
	    	this.setCurrency(business.getCurrency());
	    	this.setPayments(business.getPaymentMethods());
	    	this.setTheme(business.getTheme());
	    	
	       	if(business.getImages() != null) {
	    		for (ImageDTO i : business.getImages()) {
	    			if(i.getId().equals("logo")) {
	    				this.setLogoUrl(i.getUrl());
	    			}
	    			if(i.getId().equals("appheader")) {
	    				this.setHeaderUrl(i.getUrl());
	    			}
	    		}
	       	}
		}
		
		if(area != null) {
			this.areaDescription = area.getDescription();
			this.areaName = area.getName();
		}
	}
	
	public SpotDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getBusiness() {
		return business;
	}

	public void setBusiness(String business) {
		this.business = business;
	}

	public Long getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}

	/**
	 * A tag which can be used to group spots for easier organisation.
	 * E. g. Outside, Upper floor
	 */
	private String groupTag;


	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String code) {
		this.barcode = code;
	}	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroupTag() {
		return groupTag;
	}

	public void setGroupTag(String groupTag) {
		this.groupTag = groupTag;
	}

	public Collection<PaymentMethod> getPayments() {
		return payments;
	}

	public void setPayments(Collection<PaymentMethod> payments) {
		this.payments = payments;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getQrImageUrl() {
		return qrImageUrl;
	}


	public void setQrImageUrl(String qrImageUrl) {
		this.qrImageUrl = qrImageUrl;
	}


	public boolean isActive() {
		return active;
	}


	public void setActive(boolean active) {
		this.active = active;
	}


	public String getTheme() {
		return theme;
	}


	public void setTheme(String theme) {
		this.theme = theme;
	}


	public Long getAreaId() {
		return areaId;
	}


	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}


	public String getLogoUrl() {
		return logoUrl;
	}


	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}


	public String getHeaderUrl() {
		return headerUrl;
	}


	public void setHeaderUrl(String headerUrl) {
		this.headerUrl = headerUrl;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getAreaDescription() {
		return areaDescription;
	}

	public void setAreaDescription(String areaDescription) {
		this.areaDescription = areaDescription;
	}
	
	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public final static Function<Spot, SpotDTO> toDTO = 
			new Function<Spot, SpotDTO>() {
				@Override
				public SpotDTO apply(Spot input) {
					return new SpotDTO(input);
				}
		    };
}
