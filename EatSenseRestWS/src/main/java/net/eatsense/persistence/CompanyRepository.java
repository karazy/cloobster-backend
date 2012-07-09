package net.eatsense.persistence;

import net.eatsense.domain.Company;

public class CompanyRepository extends GenericRepository<Company> {

	final static Class<Company> entityClass = Company.class;

	public CompanyRepository() {
		super(entityClass);
	}
}
