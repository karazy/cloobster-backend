package net.eatsense.configuration;

import javax.persistence.Transient;

import net.eatsense.domain.GenericEntity;

import com.googlecode.objectify.Key;

public class WhiteLabelConfiguration extends GenericEntity<WhiteLabelConfiguration> {

	@Override
	@Transient
	public Key<WhiteLabelConfiguration> getKey() {
		return getKey(getId());
	}
	
	public static Key<WhiteLabelConfiguration> getKey(long id) {
		return new Key<WhiteLabelConfiguration>(WhiteLabelConfiguration.class, id);
	}

//	private String name;
//	private String key;
//	private String iosUrl;
//	private String androidUrl;
//	private String desktopUrl;
//	/**
//	 * @return the name
//	 */
//	public String getName() {
//		return name;
//	}
//	/**
//	 * @param name the name to set
//	 */
//	public void setName(String name) {
//		this.name = name;
//	}
//	/**
//	 * @return the key
//	 */
//	public String getKey() {
//		return key;
//	}
//	/**
//	 * @param key the key to set
//	 */
//	public void setKey(String key) {
//		this.key = key;
//	}
//	/**
//	 * @return the iosUrl
//	 */
//	public String getIosUrl() {
//		return iosUrl;
//	}
//	/**
//	 * @param iosUrl the iosUrl to set
//	 */
//	public void setIosUrl(String iosUrl) {
//		this.iosUrl = iosUrl;
//	}
//	/**
//	 * @return the androidUrl
//	 */
//	public String getAndroidUrl() {
//		return androidUrl;
//	}
//	/**
//	 * @param androidUrl the androidUrl to set
//	 */
//	public void setAndroidUrl(String androidUrl) {
//		this.androidUrl = androidUrl;
//	}
//	/**
//	 * @return the desktopUrl
//	 */
//	public String getDesktopUrl() {
//		return desktopUrl;
//	}
//	/**
//	 * @param desktopUrl the desktopUrl to set
//	 */
//	public void setDesktopUrl(String desktopUrl) {
//		this.desktopUrl = desktopUrl;
//	}
	
	

}
