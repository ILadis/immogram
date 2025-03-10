package immogram.feed.http;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import immogram.Link;
import immogram.repository.LinkRepository;

class JsonFeedHandler implements HttpHandler {

	private final URI endpoint;
	private final LinkRepository repository;

	protected JsonFeedHandler(URI endpoint, LinkRepository repository) {
		this.endpoint = endpoint;
		this.repository = repository;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		var uri = exchange.getRequestURI();
		var headers = exchange.getResponseHeaders();
		var body = exchange.getResponseBody();

		try {
			var tags = tagsFromQuery(uri);

			var title = new StringBuilder("Immogram Json Feed");
			tags.ifPresent(tag -> title.append(" (").append(tag).append(")"));

			var latest = repository.findLastSeen().map(Link::seen);
			var links = tags.map(repository::findByTag).orElseGet(repository::findAll);
			var json = JsonBuilders.forLinks(title.toString(), endpoint, links);

			headers.add("Content-Type", "application/json");
			if (latest.isPresent()) {
				headers.add("Last-Modified", JsonBuilders.dateFormatter.format(latest.get()));
			}

			exchange.sendResponseHeaders(200, 0);
			body.write(json.toString().getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			e.printStackTrace();
			exchange.sendResponseHeaders(500, -1);
		} finally {
			body.close();
		}
	}

	private Optional<String> tagsFromQuery(URI uri) {
		var query = Objects.requireNonNullElse(uri.getQuery(), "");

		var pattern = Pattern.compile("tag=([^&#]+)");
		var matcher = pattern.matcher(query);

		if (matcher.find()) {
			var tag = URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8);
			return Optional.of(tag);
		}

		return Optional.empty();
	}
}