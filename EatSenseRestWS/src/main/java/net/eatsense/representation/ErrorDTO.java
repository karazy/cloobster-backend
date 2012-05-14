package net.eatsense.representation;

public class ErrorDTO {
	private String errorKey;
	private String message;
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	private String[] substitutions;
	
	
	public ErrorDTO() {
		super();
	}

	public ErrorDTO(String errorKey, String message, String... substitutions)	{
		this.message = message;
		this.errorKey = errorKey;
		this.substitutions = substitutions;
	}
	
	public String getErrorKey() {
		return errorKey;
	}
	public void setErrorKey(String errorKey) {
		this.errorKey = errorKey;
	}
	public String[] getSubstitutions() {
		return substitutions;
	}
	public void setSubstitutions(String[] substitutions) {
		this.substitutions = substitutions;
	}
}
