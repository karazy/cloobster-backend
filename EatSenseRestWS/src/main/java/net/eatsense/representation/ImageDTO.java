package net.eatsense.representation;

import com.google.appengine.api.blobstore.BlobKey;

/**
 * Represents an image file uploaded to the {@link com.google.appengine.api.blobstore.BlobstoreService}.
 * Only used as an embedded list of images on other entities. The {@link #id} should be unique per image list.
 * @author Nils Weiher
 * 
 */
public class ImageDTO {
	
	/**
	 * String identifier of a {@link com.google.appengine.api.blobstore.BlobKey}
	 */
	String blobKey;
	
	/**
	 * At the moment we only use 'logo', 'scrapbook1','scrapbook2', etc.
	 */
	String id;
	
	/**
	 * Serving url of the image represented,
	 * this should be a public url that can be used anywhere
	 * to link to the image resource.
	 */
	String url;
	
	/**
	 * @return {@link #blobKey}
	 */
	public String getBlobKey() {
		return blobKey;
	}
	/**
	 * @param blobKey - {@link #blobKey}
	 */
	public void setBlobKey(String blobKey) {
		this.blobKey = blobKey;
	}
	/**
	 * @return {@link #id}
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id {@link BlobKey}
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * @return {@link #url}
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url - {@link #url}
	 */
	public void setUrl(String url) {
		this.url = url;
	}
}
