package net.eatsense.localization;

import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.HttpHeaders;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.core.HttpContext;

public class LocalizationProvider {
	private final Provider<HttpContext> httpContextProvider;

	@Inject
	public LocalizationProvider(Provider<HttpContext> httpContextProvider) {
		super();
		this.httpContextProvider = httpContextProvider;
	}
	
	/**
	 * Get the preferred language for the current request.
	 *  
	 * @return a locale with the preffered language
	 */
	public Locale getAcceptableLanguage() {
		return httpContextProvider.get().getRequest().getAcceptableLanguages().get(0);
	}
	
	/**
	 * Get all acceptable languages for the current request.
	 * {@link HttpHeaders#getAcceptableLanguages()}
	 * 
	 * @return a read-only list of acceptable locales
	 */
	public List<Locale> getAcceptableLanguages() {
		return httpContextProvider.get().getRequest().getAcceptableLanguages();
	}
	
	/**
	 * 
	 * @return locale for the content of the current request or null
	 */
	public Locale getContentLanguage() {
		return httpContextProvider.get().getRequest().getLanguage();
	}
}
