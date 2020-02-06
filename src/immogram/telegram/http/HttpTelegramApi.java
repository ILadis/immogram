package immogram.telegram.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.json.JsonStructure;

import immogram.Exceptions;
import immogram.telegram.CallbackQuery;
import immogram.telegram.TelegramApi;
import immogram.telegram.TextMessage;
import immogram.telegram.Update;

public class HttpTelegramApi implements TelegramApi {

	private final HttpClient client;
	private final URI root;
	private final String token;

	public HttpTelegramApi(HttpClient client, URI root, String token) {
		this.client = client;
		this.root = root;
		this.token = token;
	}

	@Override
	public List<Update<?>> pollUpdates(Duration timeout, Optional<Integer> offset) {
		var body = JsonBuilders.forUpdate(timeout, offset);
		var uri = uriForMethod("getUpdates");

		var request = HttpRequest.newBuilder()
				.uri(uri)
				.header("Content-Type", "application/json")
				.POST(asJson(body));

		return execute(request, JsonReaders::forUpdates);
	}

	@Override
	public void sendTextMessage(TextMessage message) {
		var body = JsonBuilders.forTextMessage(message);
		var uri = uriForMethod("sendMessage");

		var request = HttpRequest.newBuilder()
				.uri(uri)
				.header("Content-Type", "application/json")
				.POST(asJson(body));

		execute(request);
	}

	@Override
	public void answerCallbackQuery(CallbackQuery callback) {
		var body = JsonBuilders.forCallbackQuery(callback);
		var uri = uriForMethod("answerCallbackQuery");

		var request = HttpRequest.newBuilder()
				.uri(uri)
				.header("Content-Type", "application/json")
				.POST(asJson(body));

		execute(request);
	}

	private URI uriForMethod(String method) {
		return root.resolve("/bot" + token + "/" + method);
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
			return Exceptions.throwUnchecked(e);
		}

		if (response.statusCode() < 400) {
			return reader.apply(response.body());
		}

		var message = JsonReaders.forErrorDescription(response.body());
		return Exceptions.throwUnchecked(new Exception(message));
	}

}
