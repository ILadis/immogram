package immogram.webdriver;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

public class Element implements WaitForElement {

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

	private final WebDriver driver;
	private final Session.Id sessionId;
	private final Element.Id elementId;

	protected Element(WebDriver driver, Session.Id sessionId, Element.Id elementId) {
		this.driver = driver;
		this.sessionId = sessionId;
		this.elementId = elementId;
	}

	public Element.Id id() {
		return elementId;
	}

	public ShadowRoot shadowRoot() {
		return toShadowRoot(driver.elementShadowRoot(sessionId, elementId));
	}

	private ShadowRoot toShadowRoot(ShadowRoot.Id shadowId) {
		return new ShadowRoot(driver, sessionId, shadowId);
	}

	@Override
	public Element findElement(By selector) {
		return toElement(driver.findElementFromElement(sessionId, elementId, selector));
	}

	private Element toElement(Element.Id elementId) {
		return new Element(driver, sessionId, elementId);
	}

	public List<Element> findElements(By selector) {
		return toElements(driver.findElementsFromElement(sessionId, elementId, selector));
	}

	private List<Element> toElements(List<Element.Id> elementIds) {
		return elementIds.stream()
				.map(elementId -> toElement(elementId))
				.collect(Collectors.toList());
	}

	public String text() {
		return driver.elementText(sessionId, elementId);
	}

	public String attr(String name) {
		return driver.elementAttr(sessionId, elementId, name);
	}

	public void click() {
		driver.elementClick(sessionId, elementId);
	}

	public void sendKeys(String text) {
		driver.elementSendKeys(sessionId, elementId, text);
	}

	public ByteBuffer takeScreenshot() {
		return driver.elementScreenshot(sessionId, elementId);
	}
}
