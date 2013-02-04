package net.eatsense.restws.business;
import java.util.List;
import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;

import net.eatsense.auth.Role;
import net.eatsense.controller.InfoPageController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.representation.ImageDTO;
import net.eatsense.representation.InfoPageDTO;

/**
 * Resource class for handling infopages from the business side.
 * 
 * @author Nils Weiher
 *
 */
@Produces("application/json; charset=UTF-8")
public class InfoPagesResource {
	private final InfoPageController infoPageCtrl;
	private Business business;
	private Account account;
	
	@Inject
	public InfoPagesResource(InfoPageController infoPageCtrl) {
		super();
		this.infoPageCtrl = infoPageCtrl;
	}
	
	/**
	 * Retrieve language specific InfoPages for this business.
	 * 
	 * @param locale (optional) ISO 639-1 code to get a specific translation
	 * @return List of InfoPage transfer objects saved with the specified locale.
	 */
	@GET
	public List<InfoPageDTO> getInfoPages(@QueryParam("lang") Locale locale) {
		return infoPageCtrl.getAll(business.getKey(), Optional.fromNullable(locale));
	}
	
	/**
	 * @param id
	 * @param locale
	 * @return InfoPage transfer object for this specific language.
	 */
	@GET
	@Path("{infoPageId}")
	public InfoPageDTO getInfoPage(@PathParam("infoPageId") long id,@QueryParam("lang") Locale locale) {
		if(locale == null)
			return new InfoPageDTO(infoPageCtrl.get(business.getKey(), id));
		else
			return new InfoPageDTO(infoPageCtrl.get(business.getKey(), id , locale));
	}
	
	/**
	 * Create a new InfoPage entity
	 * 
	 * @param infoPageData content for the new InfoPage entity
	 * @return The created InfoPage
	 */
	@POST
	@Consumes("application/json; charset=UTF-8")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public InfoPageDTO createInfoPage(InfoPageDTO infoPageData) {
		return infoPageCtrl.create(business.getKey(), infoPageData);
	}
	
	/**
	 * Update an existing InfoPage entity
	 * 
	 * @param id of the entity to update
	 * @param infoPageData updated content for the specified entity 
	 * @return the updated InfoPage representation
	 */
	@PUT
	@Path("{id}")
	@Consumes("application/json; charset=UTF-8")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public InfoPageDTO updateInfoPage(@PathParam("id") Long id, InfoPageDTO infoPageData) {
		return infoPageCtrl.update(infoPageCtrl.get(business.getKey(), id), infoPageData);
	}
	
	/**
	 * Permanently delete an existing InfoPage entity 
	 * 
	 * @param id of the entity to delete
	 */
	@DELETE
	@Path("{id}")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public void deleteInfoPage(@PathParam("id") Long id) {
		infoPageCtrl.delete(business.getKey(), id);
	}
	
	/**
	 * Update or create the embedded image of the InfoPage entity
	 * 
	 * @param id of the InfoPage entity that should be updated
	 * @param imageData the data to update the embedded image entity
	 * @return the updated embedded image property
	 */
	@POST
	@Path("{id}/image")
	@Consumes("application/json; charset=UTF-8")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public ImageDTO saveImage(@PathParam("id") Long id, ImageDTO imageData) {
		return infoPageCtrl.updateImage(account, infoPageCtrl.get(business.getKey(), id), imageData);
	}
	
	@DELETE
	@Path("{id}/image")
	@RolesAllowed({Role.BUSINESSADMIN, Role.COMPANYOWNER})
	public void removeImage(@PathParam("id") Long id) {
		if(!infoPageCtrl.removeImage(infoPageCtrl.get(business.getKey(), id))) {
			throw new NotFoundException();
		}
	}

	public void setBusiness(Business business) {
		this.business = business;
	}

	public void setAccount(Account account) {
		this.account = account;
	}
}
