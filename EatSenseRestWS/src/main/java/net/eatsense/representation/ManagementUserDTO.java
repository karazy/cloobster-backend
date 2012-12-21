package net.eatsense.representation;

public class ManagementUserDTO {
	private final String email;
	private final boolean awesome;
	
	/**
	 * Create new object
	 * 
	 * @param email
	 * @param awesome
	 */
	public ManagementUserDTO(String email, boolean awesome) {
		super();
		this.email = email;
		this.awesome = awesome;
	}
	
	public String getEmail() {
		return email;
	}
	
	public boolean isAwesome() {
		return awesome;
	}
}
