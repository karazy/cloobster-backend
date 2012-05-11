package net.eatsense.exceptions;

public class CheckInFailureException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CheckInFailureException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CheckInFailureException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public CheckInFailureException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public CheckInFailureException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public CheckInFailureException(String message, String errorKey,
			String... substitutions) {
		super(message, errorKey, substitutions);
		// TODO Auto-generated constructor stub
	}

}
