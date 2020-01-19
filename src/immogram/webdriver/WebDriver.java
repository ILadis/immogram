package immogram.webdriver;

import java.net.URI;
import java.util.List;

import immogram.webdriver.Session.Id;

public interface WebDriver {
	Session.Id newSession();
	void closeSession(Session.Id sessionId);

	void navigateTo(Session.Id sessionId, URI url);

	void executeScript(Id sessionId, String script);

	Element.Id findElement(Session.Id sessionId, By selector);
	List<Element.Id> findElements(Session.Id sessionId, By selector);

	Element.Id findElementFromElement(Session.Id sessionId, Element.Id elementId, By selector);
	List<Element.Id> findElementsFromElement(Session.Id sessionId, Element.Id elementId, By selector);

	String elementText(Session.Id sessionId, Element.Id elementId);
	String elementAttr(Session.Id sessionId, Element.Id elementId, String name);
	void elementClick(Session.Id sessionId, Element.Id elementId);
	void elementSendKeys(Session.Id sessionId, Element.Id elementId, String text);
}
