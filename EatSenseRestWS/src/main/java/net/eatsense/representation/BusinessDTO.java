package net.eatsense.representation;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.bval.constraints.NotEmpty;

import com.google.common.base.Strings;

import net.eatsense.domain.Business;
import net.eatsense.domain.embedded.PaymentMethod;

public class BusinessDTO {
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
	
	@Valid
	private List<PaymentMethod> paymentMethods;
	
	public BusinessDTO() {
		super();
	}
	
	public BusinessDTO(Business business) {
		if(business == null)
			return;
		this.name = business.getName();
		this.description = business.getDescription();
		this.currency = business.getCurrency();
		this.trash = business.isTrash();
		this.id = business.getId();
		this.theme = Strings.isNullOrEmpty(business.getTheme())?"default":business.getTheme();
		this.paymentMethods = business.getPaymentMethods();
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
}