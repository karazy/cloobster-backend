package net.eatsense.configuration;

import static com.google.common.base.Preconditions.checkState;

import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.persistence.Transient;

import net.eatsense.domain.FeedbackForm;

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
	
	@Embedded
	private SpotPurePDFConfiguration spotPurePdfConfiguration;

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
		checkState(repository != null, "no respository set");
		
		repository.save(this);
	}

	public SpotPurePDFConfiguration getSpotPurePdfConfiguration() {
		return spotPurePdfConfiguration;
	}

	public void setSpotPurePdfConfiguration(SpotPurePDFConfiguration spotPurePdfConfiguration) {
		this.spotPurePdfConfiguration = spotPurePdfConfiguration;
	}
}
