package immogram.webdriver;

public class Error {
	private final String message;

	public Error(String message) {
		this.message = message;
	}

	public String message() {
		return message;
	}
}
