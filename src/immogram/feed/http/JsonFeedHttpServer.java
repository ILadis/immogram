package immogram.feed.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;

import com.sun.net.httpserver.HttpServer;

import immogram.feed.FeedServer;
import immogram.repository.LinkRepository;
import immogram.repository.ScreenshotRepository;
import immogram.task.TaskManager;

public class JsonFeedHttpServer implements FeedServer {

	private HttpServer server;
	private final TaskManager tasks;
	private final LinkRepository links;
	private final ScreenshotRepository screenshots;

	public JsonFeedHttpServer(TaskManager tasks, LinkRepository links, ScreenshotRepository screenshots) {
		this.tasks = tasks;
		this.links = links;
		this.screenshots = screenshots;
	}

	@Override
	public void start(InetSocketAddress address, URI endpoint) throws IOException {
		if (server != null) {
			throw new IOException("Server is already running");
		}

		server = HttpServer.create(address, 5);

		var feed = server.createContext(endpoint.resolve("./feed").getPath());
		feed.setHandler(new JsonFeedHandler(endpoint, links));
		// TODO context.setAuthenticator(null);

		var screenshot = server.createContext(endpoint.resolve("./screenshots").getPath());
		screenshot.setHandler(new ScreenshotsHandler(screenshots));

		var listTasks = server.createContext(endpoint.resolve("./tasks").getPath());
		listTasks.setHandler(new ListTasksHandler(endpoint, tasks));

		var hooks = server.createContext(endpoint.resolve("./hooks").getPath());
		hooks.setHandler(new TaskHooksHandler(tasks));

		server.createContext("/", exchange -> exchange.sendResponseHeaders(404, -1));
		server.start();
	}

}
