package net.eatsense.representation;

import java.util.Collection;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import net.eatsense.domain.Product;
import net.eatsense.domain.Spot;
import net.eatsense.validation.ImportChecks;

import org.apache.bval.Validate;
import org.apache.bval.constraints.NotEmpty;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.google.common.base.Function;

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
	
	@Validate(groups=ImportChecks.class)
	private Collection<ChoiceDTO> choices;
	
	private Integer order;
	
	private boolean active;

	private boolean special;	
	
	private ImageDTO image;

	private String imageUrl;
	
	private boolean checked;
	
	private boolean hideInDashboard;

	private boolean noOrder;
	
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
		this.image = (product.getImages() != null && !product.getImages().isEmpty())
				? product.getImages().get(0)
				: null;
		this.setImageUrl((image != null) ? image.getUrl()
				: null);
		this.name = product.getName();
		this.shortDesc = product.getShortDesc();
		this.longDesc = product.getLongDesc();
		this.price = product.getPrice() / 100d;
		this.order = product.getOrder();
		this.active = product.isActive();
		this.setSpecial(product.isSpecial());
		this.hideInDashboard = product.isHideInDashboard();
		this.setNoOrder(product.isNoOrder());
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


	public boolean isSpecial() {
		return special;
	}

	public void setSpecial(boolean special) {
		this.special = special;
	}
	
	public ImageDTO getImage() {
		return image;
	}

	public void setImage(ImageDTO image) {
		this.image = image;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public boolean isHideInDashboard() {
		return hideInDashboard;
	}

	public void setHideInDashboard(boolean hideInDashboard) {
		this.hideInDashboard = hideInDashboard;
	}
	
	public boolean isNoOrder() {
		return noOrder;
	}

	public void setNoOrder(boolean noOrder) {
		this.noOrder = noOrder;
	}

	public final static Function<Product, ProductDTO> toDTO = 
			new Function<Product, ProductDTO>() {
				@Override
				public ProductDTO apply(Product input) {
					return new ProductDTO(input);
				}
		    };
}


