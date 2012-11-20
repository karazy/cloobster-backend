package net.eatsense.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import net.eatsense.controller.ImageController.UpdateImagesResult;
import net.eatsense.domain.Account;
import net.eatsense.domain.Business;
import net.eatsense.domain.InfoPage;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.localization.LocalizationProvider;
import net.eatsense.persistence.InfoPageRepository;
import net.eatsense.representation.ImageDTO;
import net.eatsense.representation.InfoPageDTO;
import net.eatsense.templates.Template;
import net.eatsense.templates.TemplateRepository;
import net.eatsense.validation.ValidationHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import com.google.appengine.api.images.ImagesServicePb.ImageData;
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
	@Mock
	private ValidationHelper validator;
	
	private PolicyFactory sanitizer;

	@Before
	public void setUp() throws Exception {
		
		sanitizer = Sanitizers.BLOCKS.and(Sanitizers.FORMATTING);
		ctrl = new InfoPageController(infoPageRepo, imageCtrl, localeProvider, validator, sanitizer);
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
	public void testGetAllLanguageAwareWithAcceptAll() throws Exception {
		when(localeProvider.getAcceptableLanguage()).thenReturn(new Locale("*"));
		ctrl.getAll(businessKey);
		
		verify(infoPageRepo).getByParent(businessKey);
	}
	
	@Test
	public void testGetAllLanguageAwareWithLocale() throws Exception {
		Locale locale = new Locale("de");
		when(localeProvider.getAcceptableLanguage()).thenReturn(locale);
		ctrl.getAll(businessKey);
		
		verify(infoPageRepo).getByParent(businessKey, locale);
	}
	
	@Test
	public void testGet() throws Exception {
		Long id = 1l;
		ctrl.get(businessKey, id );
		
		verify(infoPageRepo).getById(businessKey, id);
	}
	
	@Test
	public void testGetUnknown() throws Exception {
		Long id = 1l;
		when(infoPageRepo.getById(businessKey, id)).thenThrow(com.googlecode.objectify.NotFoundException.class);
		
		thrown.expect(NotFoundException.class);
		ctrl.get(businessKey, id );
		
		verify(infoPageRepo).getById(businessKey, id);
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
		verify(infoPageRepo, never()).saveOrUpdate(infoPage);
	}
	
	@Test
	public void testUpdateImageWasDirty() throws Exception {
		Account account = mock(Account.class);
		
		InfoPage infoPage = mock(InfoPage.class);
		ImageDTO imageData = mock(ImageDTO.class);
		ImageDTO updatedImageData = mock(ImageDTO.class);
		UpdateImagesResult result = mock(UpdateImagesResult.class);
		
		when(result.isDirty()).thenReturn(true);
		
		when(result.getUpdatedImage()).thenReturn(updatedImageData );
		when(imageCtrl.updateImages(account, infoPage.getImages(), imageData)).thenReturn(result );
		
		ImageDTO updatedImage = ctrl.updateImage(account, infoPage, imageData);
		
		verify(infoPage).setImages(result.getImages());
		verify(infoPageRepo).saveOrUpdate(infoPage);
		
		assertThat(updatedImage, is(updatedImageData));
	}
	
	@Test
	public void testUpdateImageNoChange() throws Exception {
		Account account = mock(Account.class);
		
		InfoPage infoPage = mock(InfoPage.class);
		ImageDTO imageData = mock(ImageDTO.class);
		UpdateImagesResult result = mock(UpdateImagesResult.class);
		
		when(result.isDirty()).thenReturn(false);
		
		when(result.getUpdatedImage()).thenReturn(imageData );
		when(imageCtrl.updateImages(account, infoPage.getImages(), imageData)).thenReturn(result );
		
		ImageDTO updatedImage = ctrl.updateImage(account, infoPage, imageData);
		
		verify(infoPage, never()).setImages(result.getImages());
		verify(infoPageRepo,never()).saveOrUpdate(infoPage);
		
		assertThat(updatedImage, is(imageData));
	}
	
	@Test
	public void testDelete() throws Exception {
		Long id = 1l;
		@SuppressWarnings("unchecked")
		Key<InfoPage> key = mock(Key.class);
		
		when(infoPageRepo.getKey(businessKey, id)).thenReturn(key );
		
		ctrl.delete(businessKey, id );
		
		verify(infoPageRepo).deleteWithTranslation(key);
	}
	
	
	public InfoPageDTO getTestInfoPageData() {
		InfoPageDTO dto = new InfoPageDTO();
		
		dto.setHtml("Test html");
		dto.setShortText("short text");
		dto.setTitle("Title");
		
		return dto;
	}
}
