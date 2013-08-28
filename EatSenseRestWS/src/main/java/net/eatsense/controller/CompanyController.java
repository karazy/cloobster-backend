package net.eatsense.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.eatsense.configuration.addon.AddonConfiguration;
import net.eatsense.configuration.addon.AddonConfigurationService;
import net.eatsense.domain.Business;
import net.eatsense.domain.Company;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.CompanyRepository;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.googlecode.objectify.NotFoundException;

public class CompanyController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final CompanyRepository repo;	
	private final AddonConfigurationService addonConfig;
	
	@Inject
	public CompanyController(CompanyRepository repo,  AddonConfigurationService addonConfig) {
		super();
		this.repo = repo;
		this.addonConfig = addonConfig;
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
	
	/**
	 * Get configuration.
	 * @param name
	 * @return
	 */
	public Map<String, String> getConfiguration(Company company, String name) {		
		return addonConfig.get(name, company.getKey().getRaw()).getConfigMap();
	}
	

	/**
	 * Save configuration.
	 * @param company
	 * @param name
	 * @param configMap
	 * @return
	 */
	public Map<String, String> saveConfiguration(Company company,
			String name, JSONObject configMap) {
		
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, String>  _map = null;
		try {
			_map = mapper.readValue(configMap.toString(), HashMap.class);
		} catch (JsonParseException e1) {
			throw new ValidationException("Could not parse json configuration. " + e1.toString());
		} catch (JsonMappingException e1) {
			throw new ValidationException("Could not parse json configuration. " + e1.toString());
		} catch (IOException e1) {
			throw new ValidationException("Could not parse json configuration. " + e1.toString());
		}
		
		AddonConfiguration config = addonConfig.create(name, company.getKey().getRaw(), _map);
		addonConfig.put(config);
		
		return config.getConfigMap();
	}
}
