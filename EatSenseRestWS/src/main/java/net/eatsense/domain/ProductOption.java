package net.eatsense.domain;

public class ProductOption {
	String name;
	float price;
	
	/**
	 * Only set if this option represents an additional product. 
	 */
	Long id;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}
	
	public ProductOption(String name, float price, Long id) {
		super();
		this.name = name;
		this.price = price;
		this.id = id;
	}

	public ProductOption() {
		super();
	}
	
}
