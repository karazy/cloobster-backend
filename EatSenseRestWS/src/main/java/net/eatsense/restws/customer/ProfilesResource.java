package net.eatsense.restws.customer;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.google.inject.Inject;

import net.eatsense.auth.Role;
import net.eatsense.controller.ProfileController;
import net.eatsense.domain.Account;
import net.eatsense.domain.CustomerProfile;
import net.eatsense.exceptions.IllegalAccessException;
import net.eatsense.representation.CustomerProfileDTO;

@Path("c/profiles")
@Produces("application/json; charset=utf-8")
public class ProfilesResource {
	private final HttpServletRequest servletRequest;
	private final ProfileController profileCtrl;
	private final Account account;
	
	@Inject
	public ProfilesResource(HttpServletRequest servletRequest,
			ProfileController profileCtrl) {
		super();
		this.servletRequest = servletRequest;
		this.account = (Account)servletRequest.getAttribute("net.eatsense.domain.Account");
		this.profileCtrl = profileCtrl;
	}
	
	/**
	 * @param id
	 * @return customer profile data
	 */
	@GET
	@Path("{profileId}")
	@RolesAllowed({Role.USER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public CustomerProfileDTO getProfile(@PathParam("profileId") long id){
		if(account.getCustomerProfile().getId() != id) {
			throw new IllegalAccessException("Can only read the own account's profile.");
		}

		return new CustomerProfileDTO(profileCtrl.get(CustomerProfile.getKey(id)));
	}

	/**
	 * Update customer profile of an account.
	 * 
	 * @param id
	 * @param profileData
	 * @return updated profileData
	 */
	@PUT
	@Path("{profileId}")
	@RolesAllowed({Role.USER, Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public CustomerProfileDTO updateProfile(@PathParam("profileId") long id, CustomerProfileDTO profileData) {
		if(account.getCustomerProfile().getId() != id) {
			throw new IllegalAccessException("Can only update the own account's profile.");
		}
		
		return new CustomerProfileDTO( profileCtrl.updateCustomerProfile(account.getCustomerProfile(), profileData));
	}
}
