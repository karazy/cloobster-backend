package net.eatsense.representation;

import java.util.Collection;

import net.eatsense.domain.Product;

public class MenuDTO {

	private String title;
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
	
}
