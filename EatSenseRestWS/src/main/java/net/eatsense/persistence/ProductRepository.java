package net.eatsense.persistence;

import java.util.List;

import com.googlecode.objectify.Key;

import net.eatsense.domain.Location;
import net.eatsense.domain.Product;

public class ProductRepository extends GenericRepository<Product> {
	public ProductRepository() {
		super(Product.class);
	}

	public List<Product> getActiveProductsForBusiness(Key<Location> businessKey){
		logger.info("business: {}", businessKey);
		return ofy().query(Product.class).ancestor(businessKey).filter("active", true).list();
	}
}
