package immogram.feed.http;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import immogram.Screenshot;
import immogram.repository.ScreenshotRepository;

class ScreenshotsHandler implements HttpHandler {

	private final ScreenshotRepository repository;

	protected ScreenshotsHandler(ScreenshotRepository repository) {
		this.repository = repository;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		var path = exchange.getRequestURI().getPath();
		var headers = exchange.getResponseHeaders();
		var body = exchange.getResponseBody();

		try {
			var file = new File(path);

			var decoder = Base64.getUrlDecoder();
			var name = new String(decoder.decode(file.getName()));
			var uri = URI.create(name);

			var screenshot = repository.findBy(uri).map(Screenshot::bitmap);

			if (screenshot.isEmpty()) {
				exchange.sendResponseHeaders(404, -1);
			} else {
				headers.add("Content-Type", "image/png");
				exchange.sendResponseHeaders(200, screenshot.get().capacity());
				body.write(screenshot.get().array());
			}
		} catch (Exception e) {
			e.printStackTrace();
			exchange.sendResponseHeaders(500, 0);
		} finally {
			body.close();
		}
	}
}