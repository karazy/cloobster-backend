package net.eatsense.domain;

import javax.validation.constraints.NotNull;

/**
 * Used to load nickname parts from database and combine them to a full
 * nickname.
 * 
 * @author Frederik Reifschneider
 * 
 */
public class Nickname extends GenericEntity {

	@NotNull
	private String fragment;

	@NotNull
	private NicknameType type;
	
	@NotNull
	private Gender gender;

	public String getFragment() {
		return fragment;
	}

	public void setFragment(String fragment) {
		this.fragment = fragment;
	}

	public NicknameType getType() {
		return type;
	}

	public void setType(NicknameType type) {
		this.type = type;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}
	
	

}
