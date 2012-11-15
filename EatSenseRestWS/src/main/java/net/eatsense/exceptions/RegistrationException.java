package net.eatsense.exceptions;

public class RegistrationException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RegistrationException() {
		super();
	}

	public RegistrationException(String message, String errorKey,
			String... substitutions) {
		super(message, errorKey, substitutions);
	}

	public RegistrationException(String message, Throwable cause) {
		super(message, cause);
	}

	public RegistrationException(String message) {
		super(message);
	}

	public RegistrationException(Throwable cause) {
		super(cause);
	}

}
