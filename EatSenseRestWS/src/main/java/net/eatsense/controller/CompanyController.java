package net.eatsense.controller;

import java.util.Collection;

import net.eatsense.domain.Company;
import net.eatsense.persistence.CompanyRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.NotFoundException;

public class CompanyController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final CompanyRepository repo;
	
	@Inject
	public CompanyController(CompanyRepository repo) {
		super();
		this.repo = repo;
	}
	
	/**
	 * @param id
	 * @return Company saved with this id
	 */
	public Company get(long id) {
		try {
			return repo.getById(id);
		} catch (NotFoundException e) {
			logger.error("Unknown Company id={}", id);
			throw new net.eatsense.exceptions.NotFoundException("Unknown Company id="+ id);
		}
	}
	
	/**
	 * 
	 * @return All Company entities.
	 */
	public Collection<Company> getAll() {
		return repo.getAll();
	}
}
