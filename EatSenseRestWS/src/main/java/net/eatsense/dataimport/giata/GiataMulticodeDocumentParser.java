package net.eatsense.dataimport.giata;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.eatsense.exceptions.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.appengine.api.datastore.GeoPt;

public class GiataMulticodeDocumentParser extends DefaultHandler {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private SAXParser saxParser;
	private GiataPropertyData data;
	private String characters;
	private boolean parsingPhone;
	private float latitude;
	private float longitude;

	public GiataMulticodeDocumentParser() {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		
		try {
			saxParser = factory.newSAXParser();
		} catch (Exception e) {
			logger.error("error creating SAXParser", e);
			throw new ServiceException(e);
		}
	}
	
	public GiataPropertyData readFromStream(InputStream inputStream) throws SAXException, IOException {
		saxParser.parse(inputStream, this);
		
		return data;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		parsingPhone = false;
		
		if(qName.equals("property")) {
			data = new GiataPropertyData();
			data.setGiataId(Long.parseLong(attributes.getValue("giataId")));
		}
		else if(qName.equals("phone") && attributes.getValue("tech").equals("voice")) {
			parsingPhone = true;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(data != null) {
			if(qName.equals("name")) {
				data.setName(characters);				
			}
			else if(qName.equals("city")) {
				data.setCity(characters);
			}
			else if(qName.equals("street")) {
				data.setAddress(characters);			
			}
			else if(qName.equals("streetNumber")) {
				data.setAddress(data.getAddress() + " " + characters);
			}
			else if(qName.equals("postalCode")) {
				data.setPostalCode(characters);
			}
			else if(parsingPhone && qName.equals("phone")) {
				data.setPhone(characters);
			}
			else if(qName.equals("email")) {
				data.setEmail(characters);
			}
			else if(qName.equals("url")) {
				data.setUrl(characters);
			}
			else if(qName.equals("latitude")) {
				latitude = Float.parseFloat(characters);
			}
			else if(qName.equals("longitude")) {
				longitude = Float.parseFloat(characters);
			}
			else if(qName.equals("geoCode")) {
				
				try {
					data.setGeoCode(new GeoPt(latitude, longitude));
				} catch (IllegalArgumentException e) {
					logger.warn("Unable to parse geoCode value");
				}
			}
			
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		characters = new String(ch, start, length); 
	}
}
