package net.eatsense.persistence;

import net.eatsense.domain.Company;

public class CompanyRepository extends GenericRepository<Company> {

	final static Class<Company> entityClass = Company.class;
	
	static {
		GenericRepository.register(entityClass);
	}
	
	public CompanyRepository() {
		super(entityClass);
	}
}
