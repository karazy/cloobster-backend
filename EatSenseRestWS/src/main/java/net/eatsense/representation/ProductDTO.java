package net.eatsense.representation;

import java.util.Collection;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import net.eatsense.domain.Product;
import net.eatsense.validation.CreationChecks;
import net.eatsense.validation.ImportChecks;

import org.apache.bval.Validate;
import org.apache.bval.constraints.NotEmpty;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

public class ProductDTO {
//	@NotNull
//	@NotEmpty
	private Long id;
	
	private Long menuId;
	
	@NotNull
	@NotEmpty
	private String name;
	private String shortDesc;
	/**
	 * Detailed description of this product.
	 */
	private String longDesc;
	
	@Min(0)
	private double price;
	
	@Validate(groups = {ImportChecks.class})
	private Collection<ChoiceDTO> choices;
	
	private Integer order;
	
	private boolean active;	
	
	public ProductDTO() {
		super();
	}
	
	/**
	 * @param product Entity to copy property values from.
	 */
	public ProductDTO(Product product) {
		super();
		if(product == null)
			return;
		this.id = product.getId();
		
		this.menuId = product.getMenu() != null ? product.getMenu().getId():null;
		this.name = product.getName();
		this.shortDesc = product.getShortDesc();
		this.longDesc = product.getLongDesc();
		this.price = product.getPrice() / 100d;
		this.order = product.getOrder();
		this.active = product.isActive();
	}



	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getShortDesc() {
		return shortDesc;
	}


	public void setShortDesc(String shortDesc) {
		this.shortDesc = shortDesc;
	}


	public String getLongDesc() {
		return longDesc;
	}


	public void setLongDesc(String longDesc) {
		this.longDesc = longDesc;
	}


	public double getPrice() {
		return price;
	}
	
	/**
	 * @return price*100 rounded to the closest integer
	 */
	@JsonIgnore
	public long getPriceMinor() {
		return Math.round(price * 100);
	}

	public void setPrice(float price) {
		this.price = price;
	}

	

	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public Collection<ChoiceDTO> getChoices() {
		return choices;
	}


	public void setChoices(Collection<ChoiceDTO> choices) {
		this.choices = choices;
	}


	public Long getMenuId() {
		return menuId;
	}


	public void setMenuId(Long menuId) {
		this.menuId = menuId;
	}


	public Integer getOrder() {
		return order;
	}


	public void setOrder(Integer order) {
		this.order = order;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
