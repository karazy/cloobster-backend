package net.eatsense.representation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import net.eatsense.domain.Menu;
import net.eatsense.domain.Product;
import net.eatsense.validation.CreationChecks;

import org.apache.bval.constraints.NotEmpty;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

/**
 * A POJO which represents a collection of products with a title.
 * 
 * @author Nils Weiher
 *
 */
public class MenuDTO {

	/**
	 * Title of the menu entry.
	 */
	@NotNull(groups= { Default.class, CreationChecks.class})
	@NotEmpty
	private String title;
	
	private String description;
	
	private Integer order;
	
	private Long id;
	
	private boolean active;
	
	private List<Long> productIds;
	
	public MenuDTO() {
		super();
		this.productIds = new ArrayList<Long>();
	}
	
	/**
	 * @param menu Entity to copy property values from.
	 */
	public MenuDTO(Menu menu) {
		this();
		this.title = menu.getTitle();
		this.description = menu.getDescription();
		this.order = menu.getOrder();
		this.id = menu.getId();
		this.active = menu.isActive();
				
		if(menu.getProducts() != null) {
			for(Key<Product> productKey : menu.getProducts()) {
				if(productKey != null) {
					this.productIds.add(productKey.getId());
				}
			}
		}
	}
	
	/**
	 * All products that are listed under this menu entry.
	 */
	@NotNull
	@NotEmpty
	@Valid
	private Collection<ProductDTO> products;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Collection<ProductDTO> getProducts() {
		return products;
	}
	public void setProducts(Collection<ProductDTO> products) {
		this.products = products;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getOrder() {
		return order;
	}
	public void setOrder(Integer order) {
		this.order = order;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public List<Long> getProductIds() {
		return productIds;
	}

	public void setProductIds(List<Long> productIds) {
		this.productIds = productIds;
	}
	
}
