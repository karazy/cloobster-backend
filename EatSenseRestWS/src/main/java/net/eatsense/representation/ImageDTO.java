package net.eatsense.representation;

public class ImageDTO {
	
	/**
	 * String identifier of a {@link com.google.appengine.api.blobstore.BlobKey}
	 */
	String blobKey;
	
	/**
	 * At the moment we only use 'logo', 'scrapbook1,scrapbook2, etc.
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
	public void setBlobKey(String blobKey) {
		this.blobKey = blobKey;
	}
	/**
	 * @return {@link #id}
	 */
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * @return {@link #url}
	 */
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
