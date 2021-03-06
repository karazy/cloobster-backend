package net.eatsense.representation;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import net.eatsense.domain.Business;
import net.eatsense.domain.embedded.PaymentMethod;

import org.apache.bval.constraints.NotEmpty;

import com.google.common.base.Strings;

public class LocationDTO {
	@NotNull
	@NotEmpty
	private String name;
	private String description;
	private Long id;

	private String currency;
	
	private boolean trash;
	
	private String theme;
	private String url;
	private String fbUrl;
	
	private List<String> lang;
	
	@Valid
	private List<PaymentMethod> paymentMethods;
	
	private boolean basic;
	
	private Long activeSubscriptionId;
	private Long pendingSubscriptionId;
	private boolean inactiveCheckInNotificationActive;
	private boolean isIncomingOrderNotificationEnabled;
	
	private Float geoLong;
	private Float geoLat;
	
	public LocationDTO() {
		super();
	}
	
	public LocationDTO(Business business) {
		this();
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
		this.basic = business.isBasic();
		this.activeSubscriptionId = business.getActiveSubscription() != null ? business.getActiveSubscription().getId() : null;
		this.pendingSubscriptionId = business.getPendingSubscription() != null ? business.getPendingSubscription().getId() : null;
		this.inactiveCheckInNotificationActive = business.isInactiveCheckInNotificationActive();
		this.isIncomingOrderNotificationEnabled = business.isIncomingOrderNotifcationEnabled();
		if(business.getGeoLocation() != null) {
			this.setGeoLat(business.getGeoLocation().getLatitude());
			this.setGeoLong(business.getGeoLocation().getLongitude());
		}
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

	public Long getActiveSubscriptionId() {
		return activeSubscriptionId;
	}

	public void setActiveSubscriptionId(Long activeSubsriptionId) {
		this.activeSubscriptionId = activeSubsriptionId;
	}

	public Long getPendingSubscriptionId() {
		return pendingSubscriptionId;
	}

	public void setPendingSubscriptionId(Long pendingSubsriptionId) {
		this.pendingSubscriptionId = pendingSubsriptionId;
	}

	public boolean isInactiveCheckInNotificationActive() {
		return inactiveCheckInNotificationActive;
	}

	public void setInactiveCheckInNotificationActive(
			boolean inactiveCheckInNotificationActive) {
		this.inactiveCheckInNotificationActive = inactiveCheckInNotificationActive;
	}

	public boolean isIncomingOrderNotificationEnabled() {
		return isIncomingOrderNotificationEnabled;
	}

	public void setIncomingOrderNotificationEnabled(
			boolean isIncomingOrderNotificationEnabled) {
		this.isIncomingOrderNotificationEnabled = isIncomingOrderNotificationEnabled;
	}

	public Float getGeoLong() {
		return geoLong;
	}

	public void setGeoLong(Float geoLong) {
		this.geoLong = geoLong;
	}

	public Float getGeoLat() {
		return geoLat;
	}

	public void setGeoLat(Float geoLat) {
		this.geoLat = geoLat;
	}

}