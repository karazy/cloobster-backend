package net.eatsense.representation;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.bval.constraints.NotEmpty;

import com.google.common.base.Strings;

import net.eatsense.domain.Business;
import net.eatsense.domain.embedded.PaymentMethod;

public class LocationDTO {
	@NotNull
	@NotEmpty
	String name;
	@NotNull
	@NotEmpty
	String description;
	Long id;

	@NotNull
	@NotEmpty
	private String currency;
	
	private boolean trash;
	
	private String theme;
	private String url;
	private String fbUrl;
	
	private List<String> lang;
	
	@Valid
	private List<PaymentMethod> paymentMethods;
	
	private boolean basic;
	
	public LocationDTO() {
		super();
	}
	
	public LocationDTO(Business business) {
		if(business == null)
			return;
		this.name = business.getName();
		this.description = business.getDescription();
		this.currency = business.getCurrency();
		this.trash = business.isTrash();
		this.id = business.getId();
		this.lang = business.getLang();
		this.theme = Strings.isNullOrEmpty(business.getTheme())?"default":business.getTheme();
		this.paymentMethods = business.getPaymentMethods();
		this.url = business.getUrl();
		this.fbUrl = business.getFbUrl();
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
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public boolean isTrash() {
		return trash;
	}

	public void setTrash(boolean trash) {
		this.trash = trash;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}
	
	public List<PaymentMethod> getPaymentMethods() {
		return paymentMethods;
	}

	public void setPaymentMethods(List<PaymentMethod> paymentMethods) {
		this.paymentMethods = paymentMethods;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFbUrl() {
		return fbUrl;
	}

	public void setFbUrl(String fbUrl) {
		this.fbUrl = fbUrl;
	}

	public List<String> getLang() {
		return lang;
	}

	public void setLang(List<String> lang) {
		this.lang = lang;
	}

	public boolean isBasic() {
		return basic;
	}

	public void setBasic(boolean basic) {
		this.basic = basic;
	}
}