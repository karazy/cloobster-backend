package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import net.eatsense.controller.ImageController.UpdateImagesResult;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.InfoPage;
import net.eatsense.localization.LocalizationProvider;
import net.eatsense.persistence.InfoPageRepository;
import net.eatsense.representation.ImageDTO;
import net.eatsense.representation.InfoPageDTO;
import net.eatsense.validation.ValidationHelper;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

/**
 * Handles creation and loading of info pages ("Gäste ABC")
 * with translated content.
 * 
 * @author Nils Weiher
 *
 */
public class InfoPageController {
	private final InfoPageRepository infoPageRepo;
	private final LocalizationProvider localizationProvider;
	private final ImageController imageCtrl;
	private final ValidationHelper validator;
	private final PolicyFactory sanitizer;

	@Inject
	public InfoPageController(InfoPageRepository infoPageRepo, ImageController imageCtrl, LocalizationProvider localizationProvider, ValidationHelper validator, PolicyFactory sanitizer) {
		super();
		this.sanitizer = sanitizer;
		this.validator = validator;
		this.infoPageRepo = infoPageRepo;
		this.imageCtrl = imageCtrl;
		this.localizationProvider = localizationProvider;
	}
	
	/**
	 * Get all InfoPage entities in the language specified via the current request.
	 * 
	 * @param businessKey
	 * @return
	 */
	public List<InfoPageDTO> getAll(Key<Business> businessKey) {
		Locale locale = localizationProvider.getAcceptableLanguage();
		if(locale.getLanguage().equals("*")) {
			return getAll(businessKey, Optional.<Locale>absent());
		}
		else {
			return getAll(businessKey, Optional.of(locale));
		}
		
	}
	
	/**
	 * Retrieve all InfoPage entities for a specific parent Business.
	 * 
	 * @param businessKey Key of the business.
	 * @param optLocale Optional Locale, if present load the specified translation
	 * @return List of all InfoPage entities for this business.
	 */
	public List<InfoPageDTO> getAll(Key<Business> businessKey, Optional<Locale> optLocale) {
		checkNotNull(businessKey, "businessKey was null");
		List<InfoPage> infoPages;
		
		if(optLocale.isPresent()) {
			infoPages = infoPageRepo.getByParent(businessKey, optLocale.get());
		}
		else {
			infoPages = infoPageRepo.getByParent(businessKey);
		}
		
		ArrayList<InfoPageDTO> infoPageDtos = new ArrayList<InfoPageDTO>();
		
		for (InfoPage infoPage : infoPages) {
			infoPageDtos.add(new InfoPageDTO(infoPage));
		}
		
		return infoPageDtos;
	}
	
	/**
	 * @param businessKey
	 * @param id
	 * @return InfoPage entity from the datastore.
	 */
	public InfoPage get(Key<Business> businessKey, Long id) {
		try {
			return infoPageRepo.getById(businessKey, id);
		} catch (NotFoundException e) {
			
			throw new net.eatsense.exceptions.NotFoundException("Could not find entity with id: "+id,e);
		}
	}
	
	/**
	 * Create and save new InfoPage entity with data.
	 * 
	 * @param businessKey
	 * @param infoPageData
	 * @return
	 */
	public InfoPageDTO create(Key<Business> businessKey, InfoPageDTO infoPageData) {
		checkNotNull(businessKey, "businessKey was null");
		checkNotNull(infoPageData, "infoPageData was null");
		
		InfoPage infoPage = infoPageRepo.newEntity();
		infoPage.setBusiness(businessKey);
			
		return update(infoPage, infoPageData);
	}
	
	/**
	 * Update InfoPage entity with new data.
	 * Also writes translation based on the locale of the data.
	 * 
	 * @param infoPage
	 * @param infoPageData
	 * @return
	 */
	public InfoPageDTO update(InfoPage infoPage, InfoPageDTO infoPageData) {
		checkNotNull(infoPage, "infoPage was null");
		checkNotNull(infoPageData, "infoPageData was null");
		
		validator.validate(infoPageData);
		
		Locale locale = localizationProvider.getContentLanguage();
		
		infoPage.setHtml(sanitizer.sanitize(infoPageData.getHtml()));
		
		infoPage.setShortText(infoPageData.getShortText());
		infoPage.setTitle(infoPageData.getTitle());
		
		if(locale != null) {
			if(infoPage.getId() == null) {
				infoPageRepo.saveOrUpdate(infoPage);
			}
			infoPageRepo.saveOrUpdateTranslation(infoPage, locale);
		}
		else if(infoPage.isDirty()){
			infoPageRepo.saveOrUpdate(infoPage);
		}
		
		return new InfoPageDTO(infoPage);
	}
	
	/**
	 * @param account that uploaded the image
	 * @param infoPage 
	 * @param imageData
	 * @return
	 */
	public ImageDTO updateImage(Account account, InfoPage infoPage, ImageDTO imageData) {
		checkNotNull(account, "account was null");
		checkNotNull(infoPage, "infoPage was null");
		checkNotNull(imageData, "imageData was null");
		
		// For the moment we only have one image per info page.
		// Always override this image.
		imageData.setId("image");
		
		UpdateImagesResult result = imageCtrl.updateImages(account, infoPage.getImages(), imageData);
		
		if(result.isDirty()) {
			infoPage.setImages(result.getImages());
			infoPageRepo.saveOrUpdate(infoPage);
		}
		
		return result.getUpdatedImage();
	}
	
	public boolean removeImage(InfoPage infoPage) {
		checkNotNull(infoPage, "infoPage was null");
		
		UpdateImagesResult result = imageCtrl.removeImage("image", infoPage.getImages());
		
		if(result.isDirty()) {
			infoPage.setImages(result.getImages());
			infoPageRepo.saveOrUpdate(infoPage);
		}
		
		return result.isDirty();
	}
	
	/**
	 * Delete InfoPage and all translations.
	 * 
	 * @param businessKey Parent key
	 * @param id for the InfoPage entity to delete
	 */
	public void delete(Key<Business> businessKey, Long id) {
		infoPageRepo.deleteWithTranslation(infoPageRepo.getKey(businessKey, id));
	}
}
