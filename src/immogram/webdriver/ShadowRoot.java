package immogram.webdriver;

import java.util.List;
import java.util.stream.Collectors;

public class ShadowRoot implements WaitForElement {

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
	private final ShadowRoot.Id shadowId;

	protected ShadowRoot(WebDriver driver, Session.Id sessionId, ShadowRoot.Id shadowId) {
		this.driver = driver;
		this.sessionId = sessionId;
		this.shadowId = shadowId;
	}

	public ShadowRoot.Id id() {
		return shadowId;
	}

	@Override
	public Element findElement(By selector) {
		return toElement(driver.findElementFromShadowRoot(sessionId, shadowId, selector));
	}

	private Element toElement(Element.Id elementId) {
		return new Element(driver, sessionId, elementId);
	}

	public List<Element> findElements(By selector) {
		return toElements(driver.findElementsFromShadowRoot(sessionId, shadowId, selector));
	}

	private List<Element> toElements(List<Element.Id> elementIds) {
		return elementIds.stream()
				.map(elementId -> toElement(elementId))
				.collect(Collectors.toList());
	}
}
