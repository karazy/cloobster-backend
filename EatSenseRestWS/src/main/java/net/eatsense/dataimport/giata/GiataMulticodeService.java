package net.eatsense.dataimport.giata;

import java.net.URL;

import net.eatsense.exceptions.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class GiataMulticodeService {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public final static String BASE_URL = "http://multicodes.giatamedia.com/webservice/rest/1.0/properties/";
	private final GiataMulticodeDocumentParser parser;

	@Inject
	public GiataMulticodeService(GiataMulticodeDocumentParser parser) {
		this.parser = parser;
		
	}
	
	public GiataPropertyData get(long giataId) {
		URL requestUrl = null;
		try {
			requestUrl = new URL(BASE_URL + String.valueOf(giataId));
			
			return parser.readFromStream(requestUrl.openStream());
		} catch (Exception e) {
			logger.error("unable to open and parse giata data. requestUrl={}",requestUrl);
			throw new ServiceException(e);
		}		
	}
}
