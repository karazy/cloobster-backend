package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;
import net.eatsense.domain.CustomerProfile;
import net.eatsense.persistence.CustomerProfileRepository;
import net.eatsense.representation.CustomerProfileDTO;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

public class ProfileController {
		
	private final CustomerProfileRepository profileRepo;
	
	@Inject
	public ProfileController(CustomerProfileRepository profileRepo) {
		super();
		this.profileRepo = profileRepo;
	}
	
	/**
	 * @param profileKey
	 * @return CustomerProfile with this key.
	 * @throws NotFoundException
	 */
	public CustomerProfile get(Key<CustomerProfile> profileKey) throws NotFoundException {
		try {
			return profileRepo.getByKey(profileKey);
		} catch (NotFoundException e) {
			throw new net.eatsense.exceptions.NotFoundException();
		}
	}
	
	/**
	 * Create customer profile entity with data if supplied.
	 * 
	 * @param profileData
	 * @return
	 */
	public CustomerProfile createCustomerProfile(Optional<CustomerProfileDTO> profileData) {
		CustomerProfile profile = profileRepo.newEntity();
		
		if(profileData.isPresent()) {
			profile.setNickname(profileData.get().getNickname());
		}
		
		profileRepo.saveOrUpdate(profile);
		
		return profile;
	}
	
	/**
	 * Load the profile from the store and calls
	 * {@link #updateCustomerProfile(CustomerProfile, CustomerProfileDTO)}.
	 * 
	 * @param profileKey
	 * @param profileData
	 * @return updated profile
	 */
	public CustomerProfile updateCustomerProfile(Key<CustomerProfile> profileKey, CustomerProfileDTO profileData) {
		checkNotNull(profileKey, "profileKey was null");
		
		return updateCustomerProfile(get(profileKey), profileData);
	}

	/**
	 * Update the customer profile with new data.
	 * 
	 * @param profile
	 * @param profileData
	 * @return
	 */
	public CustomerProfile updateCustomerProfile(CustomerProfile profile, CustomerProfileDTO profileData) {
		checkNotNull(profile, "profile was null");
		checkNotNull(profileData, "profileData was null");
		
		profile.setNickname(profileData.getNickname());
		
		if(profile.isDirty()) {
			profileRepo.saveOrUpdate(profile);
		}
		
		return profile;
	}
}
