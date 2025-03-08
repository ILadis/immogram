package immogram.webdriver;

public class WebDriverException extends RuntimeException {

	private static final long serialVersionUID = -2338028628578591950L;

	public WebDriverException(String message) {
		super(message);
	}

	public WebDriverException(String message, Throwable cause) {
		super(message, cause);
	}

}
