package net.eatsense.configuration.addon;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.Map;

import net.eatsense.exceptions.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.common.base.Strings;
import com.google.common.collect.UnmodifiableIterator;
import com.google.inject.Inject;

/**
 * Loads and stores configuration Objects for feature configuration
 * 
 * @author Nils Weiher
 *
 */
public class AddonConfigurationServiceImpl implements AddonConfigurationService {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final DatastoreService datastore;
	
	@Inject
	public AddonConfigurationServiceImpl(DatastoreService datastore) {
		this.datastore = datastore;
		
	}
	
	@Override
	public AddonConfiguration get(String addonName) {
		Key key = KeyFactory.createKey(AddonConfiguration.KIND, addonName);
		
		Entity entity; 
		try {
			entity = datastore.get(key);
		} catch (EntityNotFoundException e) {
			throw new NotFoundException("No entity found");
		}
		
		return new AddonConfiguration(entity);
	}
	
	@Override
	public AddonConfiguration get(String addonName,
			Key parent) {
		Key key = KeyFactory.createKey(AddonConfiguration.KIND, addonName);
		
		Entity entity; 
		try {
			entity = datastore.get(key);
		} catch (EntityNotFoundException e) {
			throw new NotFoundException("No entity found");
		}
		
		return new AddonConfiguration(entity);	}

	@Override
	public Key put(AddonConfiguration config) {
		checkNotNull(config, "config cannot be null");
		checkArgument(Strings.isNullOrEmpty(config.getAddonName()), "config");
		
		Entity entity = config.buildEntity();
						
		return datastore.put(entity);
	}

	@Override
	public AddonConfiguration create(String addonName, Map<String, String> configMap) {
		checkArgument(Strings.isNullOrEmpty(addonName), "addonName must not be null or empty");
		checkNotNull(configMap, "configMap must not be null");
		
		return new AddonConfiguration(addonName, configMap);
	}

	@Override
	public Iterable<AddonConfiguration> getAll(Key parent, boolean onlyKeys) {
		
		// Use class Query to assemble a query
		Query q = new Query(AddonConfiguration.KIND).setAncestor(parent);
		
		// Set keys only if requested
		if(onlyKeys)
			q = q.setKeysOnly();
		
		// Use PreparedQuery interface to retrieve results
		PreparedQuery pq = datastore.prepare(q);
		
		
		final Iterator<Entity> resultIter = pq.asIterator();
		
		return new Iterable<AddonConfiguration>() {
			
			@Override
			public Iterator<AddonConfiguration> iterator() {
				
				return new UnmodifiableIterator<AddonConfiguration>() {

					@Override
					public boolean hasNext() {
						return resultIter.hasNext();
					}

					@Override
					public AddonConfiguration next() {
						return new AddonConfiguration(resultIter.next());
					}
				};
			}
		};
	}

}
