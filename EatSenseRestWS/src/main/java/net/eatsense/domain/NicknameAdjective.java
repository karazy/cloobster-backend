package net.eatsense.domain;

import javax.validation.constraints.NotNull;

import net.eatsense.domain.embedded.Gender;

/**
 * Used to load nickname adjectives from database and combine them to a full
 * nickname.
 * 
 * @author Frederik Reifschneider
 * 
 */
public class NicknameAdjective extends GenericEntity {

	@NotNull
	private String fragment;

	@NotNull
	private String lang;
	
	@NotNull
	private Gender gender;
	
	

	public String getFragment() {
		return fragment;
	}

	public void setFragment(String fragment) {
		this.fragment = fragment;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}
	
	

}
