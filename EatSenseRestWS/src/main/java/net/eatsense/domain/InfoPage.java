package net.eatsense.domain;

import javax.persistence.Embedded;

import net.eatsense.annotations.Translate;
import net.eatsense.representation.ImageDTO;

import com.google.appengine.api.images.ImagesServicePb.ImageData;
import com.google.common.base.Objects;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

public class InfoPage extends GenericEntity<InfoPage> {
	@Translate
	private String title;
	@Translate
	private String shortText;
	
	@Unindexed
	@Translate
	private String html;
	
	@Embedded
	@Unindexed
	private ImageDTO image;
	
	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		if(!Objects.equal(this.title, title)) {
			this.setDirty(true);
			this.title = title;
		}
	}


	public String getShortText() {
		return shortText;
	}


	public void setShortText(String shortText) {
		if(!Objects.equal(this.shortText, shortText)) {
			this.setDirty(true);
			this.shortText = shortText;
		}
	}


	public String getHtml() {
		return html;
	}


	public void setHtml(String html) {
		if(!Objects.equal(this.html, html)) {
			this.setDirty(true);
			this.html = html;
		}
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
		return Key.create(business, InfoPage.class, getId());
	}


	public Key<Business> getBusiness() {
		return business;
	}


	public void setBusiness(Key<Business> business) {
		if(!Objects.equal(this.business, business)) {
			this.setDirty(true);
			this.business = business;
		}
	}
}
