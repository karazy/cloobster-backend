package net.eatsense.exceptions;

public class RegistrationException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RegistrationException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RegistrationException(String message, String errorKey,
			String... substitutions) {
		super(message, errorKey, substitutions);
		// TODO Auto-generated constructor stub
	}

	public RegistrationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public RegistrationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public RegistrationException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
