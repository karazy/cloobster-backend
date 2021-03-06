package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.eatsense.controller.ImageController.UpdateImagesResult;
import net.eatsense.domain.Account;
import net.eatsense.domain.InfoPage;
import net.eatsense.domain.Business;
import net.eatsense.domain.translation.InfoPageT;
import net.eatsense.event.NewLocationEvent;
import net.eatsense.localization.LocalizationProvider;
import net.eatsense.persistence.InfoPageRepository;
import net.eatsense.persistence.LocalisedRepository.EntityWithTranlations;
import net.eatsense.representation.ImageDTO;
import net.eatsense.representation.InfoPageDTO;
import net.eatsense.representation.InfoPageTDTO;
import net.eatsense.service.FileServiceHelper;
import net.eatsense.templates.TemplateRepository;
import net.eatsense.validation.ValidationHelper;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.common.base.Optional;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;
import com.google.common.primitives.Bytes;
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
	private static final String INFOPAGE_IMAGE_PATH = "admin/img/templates/infopage_demo.jpg";

	private static final String INFOPAGE_TEMPLATE_NAME = "infopage-demo-de.json";
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final InfoPageRepository infoPageRepo;
	private final LocalizationProvider localizationProvider;
	private final ImageController imageCtrl;
	private final ValidationHelper validator;
	private final PolicyFactory sanitizer;
	private final TemplateController templateCtrl;
	private final ObjectMapper objectMapper;

	private final FileServiceHelper fileService;

	@Inject
	public InfoPageController(InfoPageRepository infoPageRepo, ImageController imageCtrl, LocalizationProvider localizationProvider, ValidationHelper validator, PolicyFactory sanitizer, TemplateController templateCtrl, ObjectMapper objectMapper, FileServiceHelper fileService) {
		super();
		this.sanitizer = sanitizer;
		this.validator = validator;
		this.infoPageRepo = infoPageRepo;
		this.imageCtrl = imageCtrl;
		this.localizationProvider = localizationProvider;
		this.templateCtrl = templateCtrl;
		this.objectMapper = objectMapper;
		this.fileService = fileService;
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
	 * @param businessKey
	 * @param id
	 * @return InfoPage entity from the datastore.
	 */
	public InfoPage get(Key<Business> businessKey, Long id, Locale locale) {
		try {
			return infoPageRepo.get(infoPageRepo.getKey(businessKey, id), locale);
		} catch (NotFoundException e) {
			
			throw new net.eatsense.exceptions.NotFoundException("Could not find entity with id: "+id,e);
		}
	}
	
	/**
	 * @param businessKey
	 * @param id
	 * @return InfoPage entity from the datastore.
	 */
	public InfoPageDTO getWithTranslations(Key<Business> businessKey, Long id, List<Locale> locales) {
		try {
			EntityWithTranlations<InfoPage, InfoPageT> compositeEntity = infoPageRepo.getWithTranslations(infoPageRepo.getKey(businessKey, id), locales);
			return new InfoPageDTO(compositeEntity.getEntity(), compositeEntity.getTranslations());
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
		infoPage.setId(infoPageRepo.allocateId(businessKey));
		infoPage.setCreatedOn(new Date());
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
		
		infoPage.setHtml(sanitizer.sanitize(infoPageData.getHtml()));
		
		infoPage.setShortText(infoPageData.getShortText());
		infoPage.setTitle(infoPageData.getTitle());
		infoPage.setHideInDashboard(infoPageData.isHideInDashboard());
		infoPage.setDate(infoPageData.getDate());
		infoPage.setUrl(infoPageData.getUrl());
		
		if(infoPageData.getTranslations() != null && !infoPageData.getTranslations().isEmpty()) {
			List<InfoPageT> translations = new ArrayList<InfoPageT>();
			for (InfoPageTDTO infoPageTDTO : infoPageData.getTranslations().values()) {
				InfoPageT translationEntity = new InfoPageT();
				translationEntity.setLang(infoPageTDTO.getLang());
				translationEntity.setHtml(infoPageTDTO.getHtml());
				translationEntity.setShortText(infoPageTDTO.getShortText());
				translationEntity.setTitle(infoPageTDTO.getTitle());
				
				translations.add(translationEntity);
			}
			
			infoPageRepo.saveWithTranslations(infoPage, translations);
			
			return new InfoPageDTO(infoPage, translations);
		}
		else {
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
		InfoPage infoPage;
		try {
			infoPage = infoPageRepo.getById(businessKey, id);
		} catch (NotFoundException e) {
			throw new net.eatsense.exceptions.NotFoundException();
		}
		
		imageCtrl.removeImage("image", infoPage.getImages());
		infoPageRepo.deleteWithTranslation(infoPage.getKey());
	}
	
	/**
	 * Handles basic Subscription creation before creation of a new Business in the store.
	 * 
	 * @param event
	 */
	@Subscribe
	public void handleNewLocationEvent(NewLocationEvent event) {
		logger.info("Adding german demo InfoPage to new location ...");
		
		String infoPageJson = templateCtrl.getAndReplace(INFOPAGE_TEMPLATE_NAME);
		
		if(infoPageJson == null) {
			logger.warn("No template \"{}\" found.", INFOPAGE_TEMPLATE_NAME);
			return;
		}
		
		InfoPageDTO infoPageData = null;
		try {
			infoPageData = objectMapper.readValue(infoPageJson, InfoPageDTO.class);
		} catch (Exception e) {
			logger.error("Unable to parse \"{}\" as JSON represation of InfoPageDTO",e);
			return;
		}
		Key<Business> locationKey = event.getLocation().getKey();
		InfoPage infoPage = infoPageRepo.newEntity();
		infoPage.setBusiness(locationKey);
		
		try {
			File demoImageFile = new File(INFOPAGE_IMAGE_PATH);
			byte[] bytes = Files.toByteArray(demoImageFile);
			String mimeType = URLConnection.guessContentTypeFromName(demoImageFile.getName());
			
			BlobKey demoImageBlobKey = fileService.saveNewBlob(demoImageFile.getName(), mimeType, bytes);
			String blobKeyString = demoImageBlobKey.getKeyString();
			
			infoPage.setImages(new ArrayList<ImageDTO>());
			infoPage.getImages().add(new ImageDTO(blobKeyString, "image", imageCtrl.createServingUrl(blobKeyString)));
		} catch (Exception e) {
			logger.error("Unable to read demo InfoPage file from path: " + INFOPAGE_IMAGE_PATH, e);
		}
		
		update(infoPage, infoPageData);
		
		logger.info("Created demo InfoPage for {}.", locationKey);
	}
}
