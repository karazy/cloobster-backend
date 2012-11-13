package net.eatsense.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import net.eatsense.domain.Business;
import net.eatsense.domain.InfoPage;
import net.eatsense.localization.LocalizationProvider;
import net.eatsense.persistence.InfoPageRepository;
import net.eatsense.representation.InfoPageDTO;
import net.eatsense.templates.Template;
import net.eatsense.templates.TemplateRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.googlecode.objectify.Key;

@RunWith(MockitoJUnitRunner.class)
public class InfoPageControllerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Mock
	private InfoPageRepository infoPageRepo;
	@Mock
	private ImageController imageCtrl;
	@Mock
	private LocalizationProvider localeProvider;
	
	private InfoPageController ctrl;
	@Mock
	private Key<Business> businessKey;

	@Before
	public void setUp() throws Exception {
		ctrl = new InfoPageController(infoPageRepo, imageCtrl, localeProvider);
		
	}
	
	@Test
	public void testCreateInfoPage() throws Exception {
		InfoPageDTO infoPageData = getTestInfoPageData();
		
		InfoPage newInfoPage = new InfoPage();
		when(infoPageRepo.newEntity()).thenReturn(newInfoPage );
						
		ctrl.create(businessKey, infoPageData );
		
		verify(infoPageRepo).saveOrUpdate(newInfoPage);
		assertThat(newInfoPage.getBusiness(), is(businessKey));
	}
	
	@Test
	public void testCreateInfoPageWithLocale() throws Exception {
		InfoPageDTO infoPageData = getTestInfoPageData();
		
		InfoPage newInfoPage = new InfoPage();
		when(infoPageRepo.newEntity()).thenReturn(newInfoPage );
		Locale locale = new Locale("de");
		when(localeProvider.getContentLanguage()).thenReturn(locale );
						
		ctrl.create(businessKey, infoPageData );
		
		verify(infoPageRepo).saveOrUpdate(newInfoPage);
		verify(infoPageRepo).saveOrUpdateTranslation(newInfoPage,locale);
	}
	
	@Test
	public void testUpdateInfoPageHtml() throws Exception {
		InfoPageDTO infoPageData = getTestInfoPageData();
		
		InfoPage infoPage = new InfoPage();
		
		infoPage.setBusiness(businessKey);
		infoPage.setHtml("New html");
		infoPage.setId(1l);
		infoPage.setShortText(infoPageData.getShortText());
		infoPage.setTitle(infoPageData.getTitle());
		infoPage.setDirty(false);
		ctrl.update(infoPage, infoPageData);
		
		verify(infoPageRepo).saveOrUpdate(infoPage);
		assertThat(infoPage.getHtml(), is(infoPageData.getHtml()));
	}
	
	@Test
	public void testUpdateInfoPageShortText() throws Exception {
		InfoPageDTO infoPageData = getTestInfoPageData();
		
		InfoPage infoPage = new InfoPage();
		
		infoPage.setBusiness(businessKey);
		infoPage.setHtml(infoPageData.getHtml());
		infoPage.setId(1l);
		infoPage.setShortText("Test");
		infoPage.setTitle(infoPageData.getTitle());
		infoPage.setDirty(false);
		ctrl.update(infoPage, infoPageData);
		
		verify(infoPageRepo).saveOrUpdate(infoPage);
		assertThat(infoPage.getShortText(), is(infoPageData.getShortText()));
	}
	
	@Test
	public void testUpdateInfoPageTitle() throws Exception {
		InfoPageDTO infoPageData = getTestInfoPageData();
		
		InfoPage infoPage = new InfoPage();
		
		infoPage.setBusiness(businessKey);
		infoPage.setHtml(infoPageData.getHtml());
		infoPage.setId(1l);
		infoPage.setShortText(infoPageData.getShortText());
		infoPage.setTitle("old title");
		infoPage.setDirty(false);
		ctrl.update(infoPage, infoPageData);
		
		verify(infoPageRepo).saveOrUpdate(infoPage);
		assertThat(infoPage.getTitle(), is(infoPageData.getTitle()));
	}
	
	@Test
	public void testUpdateInfoPageTranslation() throws Exception {
		Locale locale = new Locale("de");
		when(localeProvider.getContentLanguage()).thenReturn(locale );
		
		InfoPageDTO infoPageData = getTestInfoPageData();
		
		InfoPage infoPage = new InfoPage();
		
		infoPage.setBusiness(businessKey);
		infoPage.setHtml(infoPageData.getHtml());
		infoPage.setId(1l);
		infoPage.setShortText(infoPageData.getShortText());
		infoPage.setTitle(infoPageData.getTitle());
		infoPage.setDirty(false);
		ctrl.update(infoPage, infoPageData);
		
		verify(infoPageRepo).saveOrUpdateTranslation(infoPage, locale);
	}
	
	public InfoPageDTO getTestInfoPageData() {
		InfoPageDTO dto = new InfoPageDTO();
		
		dto.setHtml("Test html");
		dto.setShortText("short text");
		dto.setTitle("Title");
		
		return dto;
	}
}
