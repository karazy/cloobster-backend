package net.eatsense.domain.translation;

import net.eatsense.domain.InfoPage;
import net.eatsense.domain.TranslatedEntity;

public class InfoPageT extends TranslatedEntity<InfoPage> {
	private String html;
	private String shortText;
	private String title;

	@Override
	public InfoPage applyTranslation(InfoPage entity) {
		entity.setHtml(html);
		entity.setShortText(shortText);
		entity.setTitle(title);
		
		return entity;
	}

	@Override
	public InfoPage setFieldsFromEntity(InfoPage entity) {
		this.setHtml(entity.getHtml());
		this.setShortText(entity.getShortText());
		this.setTitle(entity.getTitle());
		
		return entity;
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
}
