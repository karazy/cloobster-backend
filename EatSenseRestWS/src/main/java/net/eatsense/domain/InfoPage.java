package net.eatsense.domain;

import javax.persistence.Embedded;

import net.eatsense.representation.ImageDTO;

import com.google.appengine.api.images.ImagesServicePb.ImageData;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

public class InfoPage extends GenericEntity<InfoPage> {
	
	private String title;
	private String shortText;
	
	@Unindexed
	private String html;
	
	@Embedded
	@Unindexed
	private ImageDTO image;
	
	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getShortText() {
		return shortText;
	}


	public void setShortText(String shortText) {
		this.shortText = shortText;
	}


	public String getHtml() {
		return html;
	}


	public void setHtml(String html) {
		this.html = html;
	}


	public ImageDTO getImage() {
		return image;
	}


	public void setImage(ImageDTO image) {
		this.image = image;
	}


	@Parent
	private Key<Business> business;
	

	@Override
	public Key<InfoPage> getKey() {
		return new Key<InfoPage>(InfoPage.class, getId());
	}


	public Key<Business> getBusiness() {
		return business;
	}


	public void setBusiness(Key<Business> business) {
		this.business = business;
	}
}
