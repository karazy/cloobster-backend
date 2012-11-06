package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Query;

import net.eatsense.domain.Business;
import net.eatsense.domain.InfoPage;
import net.eatsense.persistence.InfoPageRepository;
import net.eatsense.representation.InfoPageDTO;

/**
 * Handles creation and loading of info pages ("Gäste ABC")
 * 
 * @author Nils Weiher
 *
 */
public class InfoPageController {
	private final InfoPageRepository infoPageRepo;

	public InfoPageController(InfoPageRepository infoPageRepo) {
		super();
		this.infoPageRepo = infoPageRepo;
	}
	
	public List<InfoPageDTO> getAll(Key<Business> businessKey) {
		checkNotNull(businessKey, "businessKey was null");
		
		Query<InfoPage> infoPageQuery = infoPageRepo.query().ancestor(businessKey);
		ArrayList<InfoPageDTO> infoPageDtos = new ArrayList<InfoPageDTO>();
		
		for (InfoPage infoPage : infoPageQuery) {
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
	
	public InfoPageDTO update(InfoPage infoPage, InfoPageDTO infoPageData) {
		checkNotNull(infoPage, "infoPage was null");
		checkNotNull(infoPageData, "infoPageData was null");
		
		infoPage.setHtml(infoPageData.getHtml());
		infoPage.setShortText(infoPage.getShortText());
		infoPage.setTitle(infoPage.getShortText());
		
		if(infoPage.isDirty()) {
			infoPageRepo.saveOrUpdate(infoPage);
		}
		
		return new InfoPageDTO(infoPage);
	}
}
