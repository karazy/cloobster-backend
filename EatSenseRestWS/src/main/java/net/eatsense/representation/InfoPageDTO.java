package net.eatsense.representation;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

import net.eatsense.domain.InfoPage;
import net.eatsense.domain.translation.InfoPageT;

public class InfoPageDTO {
	private Long id;
	
	private String title;
	private String shortText;
	private String html;
	private String url;
	private Date createdOn;
	private Date date;
	private String imageUrl;
	private ImageDTO image;
	private boolean hideInDashboard;
	
	private Map<String, InfoPageTDTO> translations;
	
	public ImageDTO getImage() {
		return image;
	}

	public void setImage(ImageDTO image) {
		this.image = image;
	}

	public InfoPageDTO() {
	}
	
	public InfoPageDTO(InfoPage infoPage) {
		if(infoPage == null)
			return;
		
		id = infoPage.getId();
		title = infoPage.getTitle();
		shortText = infoPage.getShortText();
		html = infoPage.getHtml();
		image = (infoPage.getImages() != null && !infoPage.getImages().isEmpty())
				? infoPage.getImages().get(0)
				: null;
		imageUrl = (image != null) ? image.getUrl()
				: null;
		
		hideInDashboard = infoPage.isHideInDashboard();
		url = infoPage.getUrl();
		createdOn = infoPage.getCreatedOn();
		date = infoPage.getDate();
	}
	
	public InfoPageDTO(InfoPage infoPage, Map<Locale, InfoPageT> translations) {
		this(infoPage);
		if(infoPage == null)
			return;
		if(translations != null) {
			this.translations = Maps.newHashMap();
			for (Entry<Locale, InfoPageT> translationEntry : translations.entrySet()) {
				this.translations.put(translationEntry.getKey().getLanguage(), new InfoPageTDTO(translationEntry.getValue()));
			}
		}
	}
	
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
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isHideInDashboard() {
		return hideInDashboard;
	}

	public void setHideInDashboard(boolean hideInDashboard) {
		this.hideInDashboard = hideInDashboard;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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
		this.date = date;
	}

	public Map<String, InfoPageTDTO> getTranslations() {
		return translations;
	}

	public void setTranslations(Map<String, InfoPageTDTO> translations) {
		this.translations = translations;
	}	
}
