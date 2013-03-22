package net.eatsense.representation;

import net.eatsense.domain.translation.InfoPageT;

public class InfoPageTDTO {
	private String html;
	private String shortText;
	private String title;
	private String lang;
	
	public InfoPageTDTO() {
	}
	
	/**
	 * @param infoPageTranslation
	 */
	public InfoPageTDTO(InfoPageT infoPageTranslation) {
		if(infoPageTranslation == null)
			return;
		
		this.html = infoPageTranslation.getHtml();
		this.shortText = infoPageTranslation.getShortText();
		this.title = infoPageTranslation.getTitle();
		this.setLang(infoPageTranslation.getLang());
	}
	
	
	public String getHtml() {
		return html;
	}
	public void setHtml(String html) {
		this.html = html;
	}
	public String getShortText() {
		return shortText;
	}
	public void setShortText(String shortText) {
		this.shortText = shortText;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
	
	
}
