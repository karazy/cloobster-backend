package net.eatsense.exceptions;


public class ServiceException extends RuntimeException {
	
	String errorKey;
	String[] substitutions;

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

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public ServiceException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ServiceException(String message, String errorKey, String... substitutions) {
		super(message);
		this.errorKey = errorKey;
		this.substitutions = substitutions;
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public ServiceException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public ServiceException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ServiceException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
}
