package net.eatsense.representation;

public class ErrorDTO {
	private String errorKey;
	private String[] substitutions;
	
	public ErrorDTO(String errorKey, String... substitutions)	{
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
