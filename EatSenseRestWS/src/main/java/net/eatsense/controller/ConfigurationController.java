package net.eatsense.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.eatsense.configuration.Configuration;
import net.eatsense.configuration.ConfigurationProvider;
import net.eatsense.configuration.WhiteLabelConfiguration;
import net.eatsense.configuration.addon.AddonConfiguration;
import net.eatsense.configuration.addon.AddonConfigurationService;
import net.eatsense.exceptions.ValidationException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONObject;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;

public class ConfigurationController {

	private final AddonConfigurationService addonService;
	private final ConfigurationProvider cfgProvider;
	
	@Inject
	public ConfigurationController(AddonConfigurationService addonService, ConfigurationProvider cfgProvider) {
		this.addonService = addonService;
		this.cfgProvider = cfgProvider;
		
		//call get, will create a whitelabel configuration if non exists
		cfgProvider.getWhitelabels();
	}
	
	/**
	 * @param wlc
	 * @return
	 */
	public List<Map<String,String>> getAllWhitelabels(Key<WhiteLabelConfiguration> wlc) {
		 Iterable<AddonConfiguration> configs = addonService.getAll(wlc.getRaw(), false);
		 List<Map<String, String>> whitelabels = new ArrayList<Map<String,String>>();
		 
		 while(configs.iterator().hasNext()) {
			 whitelabels.add(configs.iterator().next().getConfigMap());
		 }
		 return whitelabels;
	}
	
	/**
	 * Get whitelabel configuration.
	 * @param name
	 * @return
	 */
	public Map<String, String> getWhitelabel(Key<WhiteLabelConfiguration> wlc ,String name) {		
		return addonService.get(name, wlc.getRaw()).getConfigMap();
	}
	

	/**
	 * Save configuration.
	 * @param company
	 * @param name
	 * @param configMap
	 * @return
	 */
	public Map<String, String> saveWhitelabel(Key<WhiteLabelConfiguration> wlc,
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
		
		AddonConfiguration config = addonService.create(name, wlc.getRaw(), _map);
		addonService.put(config);
		
		return config.getConfigMap();
	}
	
	/**
	 * @param wlc
	 * @param name
	 */
	public void deleteWhitelabel(Key<WhiteLabelConfiguration> wlc, String name) {
		addonService.delete(name, wlc.getRaw());
//		AddonConfiguration cfg = addonService.get(name, wlc.getRaw());
//		if(cfg.getConfigMap().containsValue(name)) {
//			cfg.getConfigMap().remove(name);
//			addonService.put(cfg);
//			
//		}
	}

}
