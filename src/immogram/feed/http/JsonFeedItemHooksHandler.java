package immogram.feed.http;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import immogram.Link;
import immogram.repository.LinkRepository;
import immogram.repository.ScreenshotRepository;

class JsonFeedItemHooksHandler implements HttpHandler {

	private final LinkRepository links;
	private final ScreenshotRepository screenshots;

	protected JsonFeedItemHooksHandler(LinkRepository repository, ScreenshotRepository screenshots) {
		this.links = repository;
		this.screenshots = screenshots;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		var uri = exchange.getRequestURI();

		try {
			var file = new File(uri.getPath());

			var action = file.getName();
			var id = file.getParentFile().getName();

			var link = findLinkByFeedItemId(id);

			if (link.isEmpty()) {
				exchange.sendResponseHeaders(404, -1);
			} else {
				switch (action) {
					case "delete" -> deleteFeedItem(link.get());
				};

				exchange.sendResponseHeaders(200, -1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			exchange.sendResponseHeaders(500, -1);
		}
	}

	private Optional<Link> findLinkByFeedItemId(String id) {
		var iterator = links.findAll();
		while (iterator.hasNext()) {
			var link = iterator.next();

			if (JsonBuilders.forLinkId(link).getString().equals(id)) {
				return Optional.of(link);
			}
		}

		return Optional.empty();
	}

	private boolean deleteFeedItem(Link link) {
		links.delete(link);
		screenshots.deleteBy(link.href());
		return true;
	}

}
