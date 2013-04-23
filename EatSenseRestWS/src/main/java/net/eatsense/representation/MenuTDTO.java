package net.eatsense.representation;

import net.eatsense.domain.translation.MenuT;

public class MenuTDTO {
	private String lang;
	private String title;
	private String description;
	
	public MenuTDTO() {
	}
	
	public MenuTDTO(MenuT menuT) {
		if(menuT == null)
			return;
		lang = menuT.getLang();
		title = menuT.getTitle();
		description = menuT.getDescription();
	}
	
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
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
