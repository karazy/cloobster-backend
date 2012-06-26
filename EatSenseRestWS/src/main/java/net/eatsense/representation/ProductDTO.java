package net.eatsense.representation;

import java.util.Collection;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import net.eatsense.domain.Product;

import org.apache.bval.constraints.NotEmpty;
import org.codehaus.jackson.annotate.JsonIgnore;

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
	
	@NotNull
	@Min(0)
	private Float price;
	
	@Valid
	private Collection<ChoiceDTO> choices;
	
	private Integer order;
	
	
	
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
		this.price = product.getPrice();
		this.order = product.getOrder();
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


	public Float getPrice() {
		return price;
	}


	public void setPrice(Float price) {
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
}
