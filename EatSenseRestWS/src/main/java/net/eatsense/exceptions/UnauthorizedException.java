package net.eatsense.exceptions;

public class UnauthorizedException extends ServiceException {

	
	public UnauthorizedException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedException(String message, String errorKey,
			String... substitutions) {
		super(message, errorKey, substitutions);
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public UnauthorizedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
