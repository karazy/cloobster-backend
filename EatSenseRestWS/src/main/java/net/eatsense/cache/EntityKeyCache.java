package net.eatsense.cache;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EntityKeyCache {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final MemcacheService memcache;
	private final Cache<String, Key> cache;

	@Inject
	public EntityKeyCache(MemcacheService memcache) {
		this.memcache = memcache;
		this.cache = CacheBuilder.newBuilder()
	    .maximumSize(1000)
	    .build();
	}
	

	/**
	 * @param identifier
	 * @param clazz
	 * @return
	 */
	private String buildCacheKey(String identifier, Class<?> clazz) {
		return com.googlecode.objectify.Key.getKind(clazz) + "_" + identifier;
	}
	
	/**
	 * @param <T>
	 * @param identifier
	 * @param clazz
	 * @return
	 */
	private <T> String buildCacheKey(String identifier, com.googlecode.objectify.Key<T> key) {
		return key.getKind() + "_" + identifier;
	}
	
	/**
	 * Get a datastore key saved in memcache or instance memory under a specified identifier
	 * 
	 * @param identifier unique identifier for the entity
	 * @param clazz Class of the entity
	 * @return {@link com.googlecode.objectify.Key} or <code>null</code>, if not cached.
	 */
	public <T> com.googlecode.objectify.Key<T> get(String identifier, Class<T> clazz) {
		checkArgument(!Strings.isNullOrEmpty(identifier), "identifier cannot be null or empty");
		
		String cacheKey = buildCacheKey(identifier, clazz); 
		
		// Get the raw key saved under this identifier
		// 1. try from memory
		// 2. call memcache
		Key datastoreKey = cache.getIfPresent(cacheKey);
		
		if(datastoreKey == null) {
			logger.debug("not in memory. try memcache ...");
			datastoreKey =  (com.google.appengine.api.datastore.Key) memcache.get(cacheKey);
		}
		
		if(datastoreKey != null) {
			return com.googlecode.objectify.Key.typed(datastoreKey);
		}
		else {
			return null;
		}
	}
	
	/**
	 * @param identifier unique identifier for an entity
	 * @param key datastore {@link Key} of this entity
	 * @return <code>true</code> if key was saved in memcache
	 */
	public <T> boolean put(String identifier, com.googlecode.objectify.Key<T> key) {
		checkArgument(!Strings.isNullOrEmpty(identifier), "identifier cannot be null or empty");
		checkNotNull(key, "key cannot be null");
		
		String cacheKey = buildCacheKey(identifier, key);
		logger.debug("cacheKey={}, key={}", cacheKey, key);
		
		Key datastoreKey = key.getRaw();
		// save to memcache
		boolean added = memcache.put(cacheKey, datastoreKey,null, SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
		if(added) {
			// save to memory, after 
			cache.put(cacheKey, datastoreKey);
		}
		
		return added;
	}

	public void invalidate(String identifier, Class<?> clazz) {
		String cacheKey = buildCacheKey(identifier, clazz);
		memcache.delete(cacheKey);
		cache.invalidate(cacheKey);
	}
}
