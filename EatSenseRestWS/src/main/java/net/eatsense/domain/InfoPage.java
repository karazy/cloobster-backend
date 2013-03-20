package net.eatsense.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.Embedded;

import net.eatsense.representation.ImageDTO;

import com.google.common.base.Objects;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

public class InfoPage extends GenericEntity<InfoPage> {
	
	public enum InfoPageType {
		STATIC,
		LINK
	}

	private String title;
	private String shortText;
	
	@Unindexed
	private String html;
	
	@Unindexed
	private String url;
	
	private InfoPageType type;
	
	private Date createdOn;
	
	private Date date;
	
	@Embedded
	@Unindexed
	private List<ImageDTO> images;


	@Parent
	private Key<Business> business;
	
	private boolean hideInDashboard;
	
	@Override
	public Key<InfoPage> getKey() {
		return Key.create(business, InfoPage.class, getId());
	}	
	
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

	public Key<Business> getBusiness() {
		return business;
	}


	public void setBusiness(Key<Business> business) {
		if(!Objects.equal(this.business, business)) {
			this.setDirty(true);
			this.business = business;
		}
	}


	public List<ImageDTO> getImages() {
		return images;
	}


	public void setImages(List<ImageDTO> images) {
		this.images = images;
	}

	public boolean isHideInDashboard() {
		return hideInDashboard;
	}

	public void setHideInDashboard(boolean hideInDashboard) {
		if(!Objects.equal(this.hideInDashboard, hideInDashboard)) {
			this.setDirty(true);
			this.hideInDashboard = hideInDashboard;
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		if(!Objects.equal(this.url, url)) {
			this.setDirty(true);
			this.url = url;
		}
	}

	public InfoPageType getType() {
		return type;
	}

	public void setType(InfoPageType type) {
		if(!Objects.equal(this.type, type)) {
			this.setDirty(true);
			this.type = type;
		}
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		if(!Objects.equal(this.date, date)) {
			this.setDirty(true);
			this.date = date;
		}
	}
}
