package net.eatsense.exceptions;

public class ApiVersionException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ApiVersionException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ApiVersionException(String message, String errorKey,
			String... substitutions) {
		super(message, errorKey, substitutions);
		// TODO Auto-generated constructor stub
	}

	public ApiVersionException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public ApiVersionException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ApiVersionException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
}
