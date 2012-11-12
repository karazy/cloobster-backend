package net.eatsense.representation;

import net.eatsense.domain.InfoPage;

public class InfoPageDTO {
	private Long id;
	private String title;
	private String shortText;
	private String html;
	private String imageUrl;
	
	public InfoPageDTO() {
	}
	
	public InfoPageDTO(InfoPage infoPage) {
		if(infoPage != null) {
			id = infoPage.getId();
			title = infoPage.getTitle();
			shortText = infoPage.getShortText();
			html = infoPage.getHtml();
			imageUrl = infoPage.getImage() != null ? infoPage.getImage().getUrl() : null;
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
}