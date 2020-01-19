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
import java.util.List;
import java.util.function.Function;

import javax.json.JsonStructure;

import immogram.webdriver.By;
import immogram.webdriver.Element;
import immogram.webdriver.Session;
import immogram.webdriver.WebDriver;

public class HttpWebDriver implements WebDriver {

	private final HttpClient client;
	private final URI root;

	public HttpWebDriver(HttpClient client, URI root) {
		this.client = client;
		this.root = root;
	}

	@Override
	public Session.Id newSession() {
		var uri = root.resolve("/session");
		var request = HttpRequest.newBuilder()
				.uri(uri)
				.POST(emptyJson());

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
	public String elementText(Session.Id sessionId, Element.Id elementId) {
		var uri = root.resolve("/session/" + sessionId + "/element/" + elementId + "/text");
		var request = HttpRequest.newBuilder()
				.uri(uri)
				.GET();

		return execute(request, JsonReaders::forValue);
	}

	@Override
	public String elementAttr(Session.Id sessionId, Element.Id elementId, String name) {
		var uri = root.resolve("/session/" + sessionId + "/element/" + elementId + "/attribute/" + name);
		var request = HttpRequest.newBuilder()
				.uri(uri)
				.GET();

		return execute(request, JsonReaders::forValue);
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

	private static BodyPublisher emptyJson() {
		return BodyPublishers.ofString("{ }");
	}

	private static BodyPublisher asJson(JsonStructure body) {
		return BodyPublishers.ofString(body.toString());
	}

	private <T> T execute(HttpRequest.Builder request) {
		return execute(request, (in) -> null);
	}

	private <T> T execute(HttpRequest.Builder request, Function<InputStream, T> reader) {
		HttpResponse<InputStream> response;

		try {
			response = client.send(request.build(), BodyHandlers.ofInputStream());
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}

		if (response.statusCode() < 400) {
			return reader.apply(response.body());
		}

		var exception = JsonReaders.forError(response.body());
		throw exception;
	}
}
