package net.eatsense.exceptions;

public class ValidationException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ValidationException() {
		super();
	}

	public ValidationException(String message, String errorKey,
			String... substitutions) {
		super(message, errorKey, substitutions);
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(Throwable cause) {
		super(cause);
	}
}
