package immogram.feed.http;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import immogram.task.TaskManager;

class ListTasksHandler implements HttpHandler {

	private final URI endpoint;
	private final TaskManager taskManager;

	protected ListTasksHandler(URI endpoint, TaskManager taskManager) {
		this.endpoint = endpoint;
		this.taskManager = taskManager;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		var headers = exchange.getResponseHeaders();
		var body = exchange.getResponseBody();

		try {
			var tasks = taskManager.listTasks();
			var json = JsonBuilders.forTasks(endpoint, tasks);

			headers.add("Content-Type", "application/json");
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
