package net.eatsense.exceptions;

public class NotFoundException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotFoundException() {
		super();
	}

	public NotFoundException(String message, String errorKey,
			String... substitutions) {
		super(message, errorKey, substitutions);
	}

	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(Throwable cause) {
		super(cause);
	}
	
}
