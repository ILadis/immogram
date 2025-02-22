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
	private String password;
	private final TaskManager tasks;
	private final LinkRepository links;
	private final ScreenshotRepository screenshots;

	public JsonFeedHttpServer(TaskManager tasks, LinkRepository links, ScreenshotRepository screenshots) {
		this.tasks = tasks;
		this.links = links;
		this.screenshots = screenshots;
	}

	@Override
	public void setAdminPassword(String password) {
		this.password = password;
	}

	@Override
	public void start(InetSocketAddress address, URI root) throws IOException {
		if (server != null) {
			throw new IOException("Server is already running");
		}

		server = HttpServer.create(address, 5);
		var authenticator = new AdminAuthenticator(() -> password);

		var feed = server.createContext(root.resolve("./feed").getPath());
		feed.setHandler(new JsonFeedHandler(root, links));

		var screenshot = server.createContext(root.resolve("./screenshots").getPath());
		screenshot.setHandler(new ScreenshotsHandler(screenshots));

		var listTasks = server.createContext(root.resolve("./tasks").getPath());
		listTasks.setHandler(new ListTasksHandler(root, tasks));
		listTasks.setAuthenticator(authenticator);

		var hooks = server.createContext(root.resolve("./hooks").getPath());
		hooks.setHandler(new TaskHooksHandler(tasks));
		listTasks.setAuthenticator(authenticator);

		server.createContext("/", exchange -> exchange.sendResponseHeaders(404, -1));
		server.start();
	}

}
