package net.eatsense.persistence;

import java.util.List;

import com.googlecode.objectify.Key;

import net.eatsense.domain.Business;
import net.eatsense.domain.Product;

public class ProductRepository extends GenericRepository<Product> {
	static {
		GenericRepository.register(Product.class);
	}	
	public ProductRepository() {
		super(Product.class);
	}

	public List<Product> getActiveProductsForBusiness(Key<Business> businessKey){
		return ofy().query(Product.class).ancestor(businessKey).filter("active", true).list();
	}
}
