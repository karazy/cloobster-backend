package net.eatsense.exceptions;

public class DataConflictException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DataConflictException() {
		super();
	}

	public DataConflictException(String message, String errorKey,
			String... substitutions) {
		super(message, errorKey, substitutions);
	}

	public DataConflictException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataConflictException(String message) {
		super(message);
	}

	public DataConflictException(Throwable cause) {
		super(cause);
	}

}
