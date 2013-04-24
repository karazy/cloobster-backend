package net.eatsense.representation;

public class ManagementUserDTO {
	private final String email;
	private final boolean awesome;
	private final String environment;
	
	/**
	 * Create new object
	 * 
	 * @param email
	 * @param awesome
	 */
	public ManagementUserDTO(String email, boolean awesome, String environment) {
		super();
		this.email = email;
		this.awesome = awesome;
		this.environment = environment;
	}
	
	public String getEmail() {
		return email;
	}
	
	public boolean isAwesome() {
		return awesome;
	}

	public String getEnvironment() {
		return environment;
	}
}
