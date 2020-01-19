package immogram.webdriver;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class Session {

	public static class Id {
		private String value;

		public Id(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	final WebDriver driver;
	final Session.Id sessionId;

	public static Session createNew(WebDriver driver) {
		var sessionId = driver.newSession();
		return new Session(driver, sessionId);
	}

	Session(WebDriver driver, Session.Id id) {
		this.driver = driver;
		this.sessionId = id;
	}

	public Session.Id id() {
		return sessionId;
	}

	public void navigateTo(URI url) {
		driver.navigateTo(sessionId, url);
	}

	public void executeScript(String script) {
		driver.executeScript(sessionId, script);
	}

	public Element waitForElement(By selector, Duration timeout) {
		var sleep = Duration.ofSeconds(1);
		var exception = new RuntimeException();

		while (!timeout.isNegative()) {
			try {
				return findElement(selector);
			} catch (RuntimeException e) {
				exception = e;
				timeout = timeout.minus(sleep);
			}

			try {
				Thread.sleep(sleep.toMillis());
			} catch (InterruptedException e) {
				throw exception;
			}
		}

		throw exception;
	}

	public Element findElement(By selector) {
		return toElement(driver.findElement(sessionId, selector));
	}

	private Element toElement(Element.Id elementId) {
		return new Element(driver, sessionId, elementId);
	}

	public List<Element> findElements(By selector) {
		return toElements(driver.findElements(sessionId, selector));
	}

	private List<Element> toElements(List<Element.Id> elementIds) {
		return elementIds.stream()
				.map(elementId -> toElement(elementId))
				.collect(Collectors.toList());
	}

	public void close() {
		driver.closeSession(sessionId);
	}
}
