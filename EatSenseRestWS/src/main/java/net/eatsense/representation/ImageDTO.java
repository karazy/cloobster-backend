package net.eatsense.representation;

public class ImageDTO {
	String blobKey;
	/* At the moment we only use 'logo', 'scrapbook1,scrapbook2, etc. */
	String id;
	String url;
	
	public String getBlobKey() {
		return blobKey;
	}
	public void setBlobKey(String blobKey) {
		this.blobKey = blobKey;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
