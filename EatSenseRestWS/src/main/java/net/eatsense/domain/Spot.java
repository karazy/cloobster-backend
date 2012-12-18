package net.eatsense.domain;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.persistence.Transient;

import net.eatsense.exceptions.ServiceException;

import com.google.common.base.Objects;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Parent;


/**
 * Represents a location in e. g. a restaurant.
 * A spot is a place with a barcode where a user is able to check in.
 * 
 * @author Frederik Reifschneider
 *
 */
@Cached
public class Spot extends GenericEntity<Spot>{
	public static final String BARCODE_FORMAT = "%d-%d";
	
	/**
	 * The business this spot belongs to.
	 */
	@Parent
	private Key<Location> business;
	
	/**
	 * Barcode identifying this spot.
	 */
	private String barcode;
	
	/**
	 * A human readable identifier for the spot where the barcode is located.
	 * E.g. Table no. 4, Lounge etc.
	 */
	private String name;
	
	/**
	 * Determines if the customer is able to checkin on that spot.
	 * (Default is true, for downwards combatibility).
	 */
	private boolean active = true;
	
	private Key<Area> area;
	

	public Key<Area> getArea() {
		return area;
	}

	public void setArea(Key<Area> area) {
		
		if(!Objects.equal(this.area, area)) {
			this.setDirty(true);
			this.area = area;
		}
	}

	private String qrImageUrl;

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		if(!Objects.equal(this.barcode, barcode)) {
			this.setDirty(true);
			this.barcode = barcode;
		}
	}	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(!Objects.equal(this.name, name)) {
			this.setDirty(true);
			this.name = name;
		}
	}

	public Key<Location> getBusiness() {
		return business;
	}

	public void setBusiness(Key<Location> business) {
		if(!Objects.equal(this.business, business)) {
			this.setDirty(true);
			this.business = business;
		}
	}

	@Transient
	public Key<Spot> getKey() {
	   return getKey(getBusiness(), getId());
	}
	
	@Transient
	public static Key<Spot> getKey(Key<Location> business, Long spotId) {
		return new Key<Spot>(business ,Spot.class,spotId);
	}
	
	public String getBarcodeWithDownloadURL() {
		return System.getProperty("net.karazy.app.download.url") + "#" + barcode;
	}
	
	public String getQrImageUrl() {
		if(barcode != null) {
			if(qrImageUrl == null) {
				try {
					qrImageUrl = "https://chart.googleapis.com/chart?cht=qr&chs=150x150&chl=" + URLEncoder.encode(getBarcodeWithDownloadURL(),"UTF-8");
				} catch (UnsupportedEncodingException e) {
					throw new ServiceException(e);
				}
			}
			return qrImageUrl;
		}
		else
			return null;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		if(!Objects.equal(this.active, active)) {
			this.setDirty(true);
			this.active = active;
		}
	}
	
	/**
	 * @param businessId representing the parent business
	 * @param id representing the spot
	 * @return
	 */
	public static String generateBarcode(long businessId, long id) {		
		return String.format(BARCODE_FORMAT, businessId, id);
	}
	
	/**
	 * @return
	 */
	public String generateBarcode() {
		this.barcode = generateBarcode(business.getId(), getId()); 
		return barcode;
	}
}

