package immogram.feed.http;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import immogram.Link;
import immogram.repository.LinkRepository;

class JsonFeedHandler implements HttpHandler {

	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZone(ZoneId.of("GMT"));

	private final URI endpoint;
	private final LinkRepository repository;

	protected JsonFeedHandler(URI endpoint, LinkRepository repository) {
		this.endpoint = endpoint;
		this.repository = repository;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		var headers = exchange.getResponseHeaders();
		var body = exchange.getResponseBody();

		try {
			var latest = repository.findLastSeen().map(Link::seen);
			var links = repository.findAll();
			var json = JsonBuilders.forLinks("Immogram Json Feed", endpoint, links);

			headers.add("Content-Type", "application/json");
			if (latest.isPresent()) {
				headers.add("Last-Modified", formatter.format(latest.get()));
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
}