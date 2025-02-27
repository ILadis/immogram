package immogram.webdriver.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.json.JsonStructure;

import immogram.Exceptions;
import immogram.webdriver.By;
import immogram.webdriver.Element;
import immogram.webdriver.Session;
import immogram.webdriver.Session.Id;
import immogram.webdriver.ShadowRoot;
import immogram.webdriver.WebDriver;

/* For available web driver endpoints see:
 *   https://w3c.github.io/webdriver/#endpoints
 *
 * See geckodriver for firefox specific implementation details:
 *   https://firefox-source-docs.mozilla.org/testing/geckodriver/index.html
 */
public class HttpWebDriver implements WebDriver {

	private final HttpClient client;
	private final URI root;
	private final List<String> capabilities = new LinkedList<>();

	public HttpWebDriver(HttpClient client, URI root) {
		this(client, root, Optional.empty(), true);
	}

	public HttpWebDriver(HttpClient client, URI root, Optional<String> profile, boolean headless) {
		this.client = client;
		this.root = root;

		if (headless) {
			capabilities.add("-headless");
		}

		if (profile.isPresent()) {
			capabilities.add("--profile");
			capabilities.add(profile.get());
		}
	}

	@Override
	public Session.Id newSession() {
		var uri = root.resolve("/session");
		var body = JsonBuilders.forCapabilitiesArgs(capabilities.toArray(String[]::new));

		var request = HttpRequest.newBuilder()
				.uri(uri)
				.POST(asJson(body));

		return execute(request, JsonReaders::forSessionId);
	}

	@Override
	public void closeSession(Session.Id sessionId) {
		var uri = root.resolve("/session/" + sessionId);
		var request = HttpRequest.newBuilder()
				.uri(uri)
				.DELETE();

		execute(request);
	}

	@Override
	public void navigateTo(Session.Id sessionId, URI url) {
		var uri = root.resolve("/session/" + sessionId + "/url");
		var body = JsonBuilders.forUrl(url);

		var request = HttpRequest.newBuilder()
				.uri(uri)
				.POST(asJson(body));

		execute(request);
	}

	@Override
	public void executeScript(Session.Id sessionId, String script) {
		var uri = root.resolve("/session/" + sessionId + "/execute/sync");
		var body = JsonBuilders.forScript(script);

		var request = HttpRequest.newBuilder()
				.uri(uri)
				.POST(asJson(body));

		execute(request);
	}

	@Override
	public Element.Id findElement(Session.Id sessionId, By selector) {
		var uri = root.resolve("/session/" + sessionId + "/element");
		var body = JsonBuilders.forSelector(selector);

		var request = HttpRequest.newBuilder()
				.uri(uri)
				.POST(asJson(body));

		return execute(request, JsonReaders::forElementId);
	}

	@Override
	public List<Element.Id> findElements(Session.Id sessionId, By selector) {
		var uri = root.resolve("/session/" + sessionId + "/elements");
		var body = JsonBuilders.forSelector(selector);

		var request = HttpRequest.newBuilder()
				.uri(uri)
				.POST(asJson(body));

		return execute(request, JsonReaders::forElementIds);
	}

	@Override
	public Element.Id findElementFromElement(Session.Id sessionId, Element.Id elementId, By selector) {
		var uri = root.resolve("/session/" + sessionId + "/element/" + elementId + "/element");
		var body = JsonBuilders.forSelector(selector);

		var request = HttpRequest.newBuilder()
				.uri(uri)
				.POST(asJson(body));

		return execute(request, JsonReaders::forElementId);
	}

	@Override
	public List<Element.Id> findElementsFromElement(Session.Id sessionId, Element.Id elementId, By selector) {
		var uri = root.resolve("/session/" + sessionId + "/element/" + elementId + "/elements");
		var body = JsonBuilders.forSelector(selector);

		var request = HttpRequest.newBuilder()
				.uri(uri)
				.POST(asJson(body));

		return execute(request, JsonReaders::forElementIds);
	}

	@Override
	public ShadowRoot.Id elementShadowRoot(Session.Id sessionId, Element.Id elementId) {
		var uri = root.resolve("/session/" + sessionId + "/element/" + elementId + "/shadow");
		var request = HttpRequest.newBuilder()
				.uri(uri)
				.GET();

		return execute(request, JsonReaders::forShadowRootId);
	}

	@Override
	public Element.Id findElementFromShadowRoot(Id sessionId, ShadowRoot.Id shadowId, By selector) {
		var uri = root.resolve("/session/" + sessionId + "/shadow/" + shadowId + "/element");
		var body = JsonBuilders.forSelector(selector);

		var request = HttpRequest.newBuilder()
				.uri(uri)
				.POST(asJson(body));

		return execute(request, JsonReaders::forElementId);
	}

	@Override
	public List<Element.Id> findElementsFromShadowRoot(Id sessionId, ShadowRoot.Id shadowId, By selector) {
		var uri = root.resolve("/session/" + sessionId + "/shadow/" + shadowId + "/elements");
		var body = JsonBuilders.forSelector(selector);

		var request = HttpRequest.newBuilder()
				.uri(uri)
				.POST(asJson(body));

		return execute(request, JsonReaders::forElementIds);
	}

	@Override
	public String elementText(Session.Id sessionId, Element.Id elementId) {
		var uri = root.resolve("/session/" + sessionId + "/element/" + elementId + "/text");
		var request = HttpRequest.newBuilder()
				.uri(uri)
				.GET();

		return execute(request, JsonReaders::forTextValue);
	}

	@Override
	public String elementAttr(Session.Id sessionId, Element.Id elementId, String name) {
		var uri = root.resolve("/session/" + sessionId + "/element/" + elementId + "/attribute/" + name);
		var request = HttpRequest.newBuilder()
				.uri(uri)
				.GET();

		return execute(request, JsonReaders::forTextValue);
	}

	@Override
	public void elementClick(Session.Id sessionId, Element.Id elementId) {
		var uri = root.resolve("/session/" + sessionId + "/element/" + elementId + "/click");
		var request = HttpRequest.newBuilder()
				.uri(uri)
				.POST(emptyJson());

		execute(request);
	}

	@Override
	public void elementSendKeys(Session.Id sessionId, Element.Id elementId, String text) {
		var uri = root.resolve("/session/" + sessionId + "/element/" + elementId + "/value");
		var body = JsonBuilders.forText(text);

		var request = HttpRequest.newBuilder()
				.uri(uri)
				.POST(asJson(body));

		execute(request);
	}

	@Override
	public ByteBuffer elementScreenshot(Id sessionId, immogram.webdriver.Element.Id elementId) {
		var uri = root.resolve("/session/" + sessionId + "/element/" + elementId + "/screenshot");

		var request = HttpRequest.newBuilder()
				.uri(uri)
				.GET();

		return execute(request, JsonReaders::forBase64Value);
	}

	private static BodyPublisher emptyJson() {
		return BodyPublishers.ofString("{ }");
	}

	private static BodyPublisher asJson(JsonStructure body) {
		return BodyPublishers.ofString(body.toString());
	}

	private <T> T execute(HttpRequest.Builder request) {
		return execute(request, stream -> null);
	}

	private <T> T execute(HttpRequest.Builder request, Function<InputStream, T> reader) {
		HttpResponse<InputStream> response;

		try {
			response = client.send(request.build(), BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {
			return Exceptions.throwUnchecked(e);
		}

		if (response.statusCode() < 400) {
			return reader.apply(response.body());
		}

		var message = JsonReaders.forErrorMessage(response.body());
		return Exceptions.throwUnchecked(new Exception(message));
	}
}
