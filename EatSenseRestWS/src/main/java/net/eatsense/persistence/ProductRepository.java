package net.eatsense.persistence;

import net.eatsense.domain.Product;

public class ProductRepository extends GenericRepository<Product> {
	static {
		GenericRepository.register(Product.class);
	}	
	public ProductRepository() {
		super(Product.class);
	}

}
