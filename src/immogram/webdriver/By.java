package immogram.webdriver;

public class By {

	public static By cssSelector(String value) {
		return new By(Locator.CSS_SELECTOR, value);
	}

	private final Locator locator;
	private final String value;

	private By(Locator locator, String value) {
		this.locator = locator;
		this.value = value;
	}

	public Locator locator() {
		return locator;
	}

	public String value() {
		return value;
	}

	public static enum Locator {
		CSS_SELECTOR("css selector"), TAG_NAME("tag name");

		private final String value;

		private Locator(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}
	}
}
