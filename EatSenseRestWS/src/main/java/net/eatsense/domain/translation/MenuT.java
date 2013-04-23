package net.eatsense.domain.translation;

import net.eatsense.domain.Menu;
import net.eatsense.domain.TranslatedEntity;

import com.google.common.base.Strings;

public class MenuT extends TranslatedEntity<Menu> {
	private String description;
	private String title;

	@Override
	public Menu applyTranslation(Menu entity) {
		// Only override the values on the entity if we actually have content
		if(!Strings.isNullOrEmpty(description))
			entity.setDescription(description);
		if(!Strings.isNullOrEmpty(title))
			entity.setTitle(title);
		
		return entity;
	}

	@Override
	public Menu setFieldsFromEntity(Menu entity) {
		this.description = entity.getDescription();
		this.setTitle(entity.getTitle());
		
		return entity;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
