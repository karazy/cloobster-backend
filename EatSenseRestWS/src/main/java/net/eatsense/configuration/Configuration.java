package net.eatsense.configuration;

import javax.persistence.Id;
import javax.persistence.Transient;

import net.eatsense.domain.FeedbackForm;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;

@Cached
public class Configuration {
	
	@Id
	private String id;
	
	@Transient
	private ConfigurationRepository repository;
		
	public ConfigurationRepository getRepository() {
		return repository;
	}

	public void setRepository(ConfigurationRepository repository) {
		this.repository = repository;
	}

	protected Configuration() {		
		super();
		// Set default id.
		this.id = "default";
	}

	private Key<FeedbackForm> defaultFeedbackForm;

	public Key<FeedbackForm> getDefaultFeedbackForm() {
		return defaultFeedbackForm;
	}

	public void setDefaultFeedbackForm(Key<FeedbackForm> defaultFeedbackForm) {
		this.defaultFeedbackForm = defaultFeedbackForm;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void save() throws IllegalStateException {
		if(repository == null) {
			throw new IllegalStateException("No repository found.");
		}
		repository.save(this);
	}
}
