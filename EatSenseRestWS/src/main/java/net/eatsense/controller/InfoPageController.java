package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.inject.Inject;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Query;

import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.InfoPage;
import net.eatsense.localization.LocalizationProvider;
import net.eatsense.persistence.InfoPageRepository;
import net.eatsense.representation.ImageDTO;
import net.eatsense.representation.InfoPageDTO;

/**
 * Handles creation and loading of info pages ("Gäste ABC")
 * 
 * @author Nils Weiher
 *
 */
public class InfoPageController {
	private final InfoPageRepository infoPageRepo;
	private final LocalizationProvider localizationProvider;
	private final ImageController imageCtrl;

	@Inject
	public InfoPageController(InfoPageRepository infoPageRepo, ImageController imageCtrl, LocalizationProvider localizationProvider) {
		super();
		this.infoPageRepo = infoPageRepo;
		this.imageCtrl = imageCtrl;
		this.localizationProvider = localizationProvider;
	}
	
	public List<InfoPageDTO> getAll(Key<Business> businessKey) {
		checkNotNull(businessKey, "businessKey was null");
		List<InfoPage> infoPages;
		
		Locale locale = localizationProvider.getAcceptableLanguage();
		if(locale.getLanguage().equals("*")) {
			infoPages = infoPageRepo.getByParent(businessKey);
		}
		else {
			infoPages = infoPageRepo.getByParent(businessKey, locale);
		}
		
		ArrayList<InfoPageDTO> infoPageDtos = new ArrayList<InfoPageDTO>();
		
		for (InfoPage infoPage : infoPages) {
			infoPageDtos.add(new InfoPageDTO(infoPage));
		}
		
		return infoPageDtos;
	}
	
	public InfoPage get(Key<Business> businessKey, Long id) {
		try {
			return infoPageRepo.getById(businessKey, id);
		} catch (NotFoundException e) {
			
			throw new net.eatsense.exceptions.NotFoundException("Could not find entity with id: "+id,e);
		}
	}
	
	public InfoPageDTO create(Key<Business> businessKey, InfoPageDTO infoPageData) {
		checkNotNull(businessKey, "businessKey was null");
		checkNotNull(infoPageData, "infoPageData was null");
		
		InfoPage infoPage = infoPageRepo.newEntity();
		infoPage.setBusiness(businessKey);
		
		return update(infoPage, infoPageData);
	}
	
	public InfoPageDTO update(InfoPage infoPage, InfoPageDTO infoPageData) {
		checkNotNull(infoPage, "infoPage was null");
		checkNotNull(infoPageData, "infoPageData was null");
		
		Locale locale = localizationProvider.getContentLanguage();
		
		infoPage.setHtml(infoPageData.getHtml());
		infoPage.setShortText(infoPageData.getShortText());
		infoPage.setTitle(infoPageData.getTitle());
		
		if(infoPage.isDirty()) {			
			infoPageRepo.saveOrUpdate(infoPage);
		}
		else if (locale != null) {
			infoPageRepo.saveOrUpdate(infoPage, locale);
		}
		
		return new InfoPageDTO(infoPage);
	}
	
	public ImageDTO updateImage(Account account, InfoPage infoPage, ImageDTO imageData) {
		return imageCtrl.updateImages(account, infoPage.getImages(), imageData).getUpdatedImage();
	}
}
