package top.lcmatrix.util.permano;

public class NoPermissionException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7047098002468131657L;

	public NoPermissionException() {
		super();
	}

	public NoPermissionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NoPermissionException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoPermissionException(String message) {
		super(message);
	}

	public NoPermissionException(Throwable cause) {
		super(cause);
	}
}
