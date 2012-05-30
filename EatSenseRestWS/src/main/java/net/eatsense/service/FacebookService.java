package net.eatsense.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.eatsense.exceptions.ServiceException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class FacebookService {
	private URLFetchService urlFetchService;

	@Inject
	public FacebookService(URLFetchService urlFetchService) {
		super();
		this.urlFetchService = urlFetchService;
	}
	
	public JSONObject getMe(String accessToken) throws ServiceException {
		checkArgument(!Strings.nullToEmpty(accessToken).isEmpty(), "accessToken was null or empty");
		
		try {
			HTTPResponse response = urlFetchService.fetch(new HTTPRequest(
					new URL("https://graph.facebook.com/me?access_token=" + accessToken),
					HTTPMethod.GET,
					FetchOptions.Builder.validateCertificate()
					));
			switch (response.getResponseCode()) {
			case 200:
				String responseText = new String (response.getContent(), "UTF-8");
				JSONObject jsonMe;
				try {
					jsonMe = new JSONObject(responseText);
				} catch (JSONException e) {
					throw new ServiceException("error parsing facebook response", e);
				}
				return jsonMe;
			case 400:
				throw new IllegalArgumentException("accessToken was invalid");
			default:
				throw new ServiceException("unable to connect to facebook api");
			}
			
		} catch (MalformedURLException e) {
			throw new ServiceException("error in facebook api url");
		} catch (IOException e) {
			throw new ServiceException("unable to read facebook response");
		}
	}
}
