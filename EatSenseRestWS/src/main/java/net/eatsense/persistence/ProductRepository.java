package net.eatsense.persistence;

import net.eatsense.domain.Product;

public class ProductRepository extends GenericRepository<Product> {

	public ProductRepository() {
		super();
		super.clazz = Product.class;
	}

}
