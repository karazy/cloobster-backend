package net.eatsense.representation;

import java.util.Collection;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import net.eatsense.domain.Menu;
import net.eatsense.validation.CreationChecks;

import org.apache.bval.constraints.NotEmpty;

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
	
	public MenuDTO() {
		super();
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
	
}
