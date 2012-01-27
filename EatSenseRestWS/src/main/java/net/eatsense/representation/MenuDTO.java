package net.eatsense.representation;

import java.util.Collection;

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
	private String title;
	
	private String description;
	
	
	/**
	 * All products that are listed under this menu entry.
	 */
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
	
}
