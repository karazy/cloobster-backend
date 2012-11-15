package net.eatsense.exceptions;

public class ReadOnlyException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ReadOnlyException() {
		super();
	}

	public ReadOnlyException(String message, String errorKey,
			String... substitutions) {
		super(message, errorKey, substitutions);
	}

	public ReadOnlyException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReadOnlyException(String message) {
		super(message);
	}

	public ReadOnlyException(Throwable cause) {
		super(cause);
	}

}
