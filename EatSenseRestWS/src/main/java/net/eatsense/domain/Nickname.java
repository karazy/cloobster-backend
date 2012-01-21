package net.eatsense.domain;


public class Nickname extends GenericEntity{
	
	private String fragment;
	
	private NicknameType type;

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
	
	
	

}
