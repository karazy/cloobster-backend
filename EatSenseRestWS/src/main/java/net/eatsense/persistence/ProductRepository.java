package net.eatsense.persistence;

import java.util.List;

import com.googlecode.objectify.Key;

import net.eatsense.domain.Business;
import net.eatsense.domain.Product;

public class ProductRepository extends GenericRepository<Product> {
	public ProductRepository() {
		super(Product.class);
	}

	public List<Product> getActiveProductsForBusiness(Key<Business> businessKey){
		logger.info("business: {}", businessKey);
		return ofy().query(Product.class).ancestor(businessKey).filter("active", true).list();
	}
	
	public Iterable<Product> iterateActiveProductsForBusiness(Key<Business> businessKey){
		logger.info("business: {}", businessKey);
		return ofy().query(Product.class).ancestor(businessKey).filter("active", true).fetch();
	}
}
