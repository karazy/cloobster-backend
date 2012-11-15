package net.eatsense.representation;

import java.util.Collection;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import net.eatsense.domain.embedded.PaymentMethod;

import org.apache.bval.constraints.NotEmpty;

/**
 * POJO for data import, which represents a location where you can check in and order food/drinks what ever.
 * 
 * @author Nils Weiher
 *
 */
@XmlRootElement
public class BusinessImportDTO {

	/**
	 * Name of location.
	 */
	@NotNull
	@NotEmpty
	@Size(min=1)
	private String name;
	
	@NotNull
	@NotEmpty
	private String city;
	@NotEmpty
	@NotNull
	private String address;

	@NotEmpty
	@NotNull
	private String currency;
	
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	@NotEmpty
	@NotNull
	private String postcode;

	/**
	 * Description of location.
	 */
	private String description;

	/**
	 * All menus the business is offering.
	 */
	@NotNull
	@NotEmpty
	@Valid
	private Collection<MenuDTO> menus;
	
	/**
	 * All different service areas, containing the spots.
	 */
	@NotNull
	@NotEmpty
	@Valid
	private Collection<AreaImportDTO> areas;
	
	/**
	 * All payment methods the business accepts.
	 */
	@NotNull
	@NotEmpty
	@Valid
	private Collection<PaymentMethod> payments;

	public BusinessImportDTO() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public Collection<MenuDTO> getMenus() {
		return menus;
	}

	public void setMenus(Collection<MenuDTO> menus) {
		this.menus = menus;
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

	public Collection<AreaImportDTO> getAreas() {
		return areas;
	}

	public void setAreas(Collection<AreaImportDTO> areas) {
		this.areas = areas;
	}
}
