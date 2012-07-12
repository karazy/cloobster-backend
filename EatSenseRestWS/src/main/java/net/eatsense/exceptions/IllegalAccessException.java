package net.eatsense.exceptions;

public class IllegalAccessException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IllegalAccessException() {
		super();
	}

	public IllegalAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalAccessException(String message) {
		super(message);
	}

	public IllegalAccessException(Throwable cause) {
		super(cause);
	}

	public IllegalAccessException(String message, String errorKey,
			String... substitutions) {
		super(message, errorKey, substitutions);
	}

	
}
