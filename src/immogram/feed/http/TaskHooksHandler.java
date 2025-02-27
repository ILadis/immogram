package immogram.feed.http;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Base64;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import immogram.task.TaskManager;

class TaskHooksHandler implements HttpHandler {

	private final TaskManager taskManager;

	protected TaskHooksHandler(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		var uri = exchange.getRequestURI();

		try {
			var file = new File(uri.getPath());
			var action = file.getName();

			var decoder = Base64.getUrlDecoder();
			var alias = new String(decoder.decode(file.getParentFile().getName()));

			var task = taskManager.taskByAlias(alias);

			if (task.isEmpty()) {
				exchange.sendResponseHeaders(404, -1);
			} else {
				switch (action) {
					case "execute"  -> task.get().execute(null);
					case "schedule" -> task.get().schedule(durationFromQuery(uri));
					case "cancel"   -> task.get().cancel();
				}

				exchange.sendResponseHeaders(200, -1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			exchange.sendResponseHeaders(500, -1);
		}
	}

	private Duration durationFromQuery(URI uri) {
		var query = uri.getQuery();

		var pattern = Pattern.compile("hours=(\\d+)");
		var matcher = pattern.matcher(query);

		if (matcher.find()) {
			var hours = Integer.parseInt(matcher.group(1));
			return Duration.ofHours(hours);
		}

		throw new IllegalArgumentException();
	}
}
