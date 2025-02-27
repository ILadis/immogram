package immogram.feed.http;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import immogram.Exceptions;
import immogram.Link;
import immogram.task.TaskManager.ManagedTask;

class JsonBuilders {

	static final Base64.Encoder urlEncoder = Base64.getUrlEncoder().withoutPadding();
	static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZone(ZoneId.of("GMT"));

	static JsonObject forLinks(String title, URI endpoint, Iterator<Link> links) {
		var json = Json.createArrayBuilder();

		while (links.hasNext()) {
			json.add(forLink(endpoint, links.next()));
		}

		return Json.createObjectBuilder()
				.add("version", "https://jsonfeed.org/version/1.1")
				.add("title", title)
				.add("items", json)
				.build();
	}

	static JsonObject forLink(URI endpoint, Link link) {
		var id = Integer.toHexString(link.href().hashCode());
		var encodedUrl = urlEncoder.encodeToString(link.href().toString().getBytes(StandardCharsets.UTF_8));

		return Json.createObjectBuilder()
				.add("id", id)
				.add("title", link.title())
				.add("date_published", link.seen().toString())
				.add("image", endpoint.resolve("./screenshots/" + encodedUrl).toString())
				.add("url", link.href().toString())
				.build();
	}

	static JsonArray forTasks(URI endpoint, List<ManagedTask> tasks) {
		var json = Json.createArrayBuilder();

		for (ManagedTask task : tasks) {
			json.add(forTask(endpoint, task));
		}

		return json.build();
	}

	static JsonValue forTask(URI endpoint, ManagedTask task) {
		var alias = urlEncoder.encodeToString(task.alias().getBytes(StandardCharsets.UTF_8));
		var period = task.runPeriod().map(String::valueOf);
		var lastRunTimestamp = task.lastRunTimestamp().map(String::valueOf);
		var lastRunException = task.lastRunException().map(Exceptions::stackTraceOf);

		return Json.createObjectBuilder()
				.add("alias", task.alias())
				.add("scheduled", task.isScheduled())
				.add("run_period", period.orElse("never"))
				.add("last_run_timestamp", lastRunTimestamp.orElse("never"))
				.add("last_run_exception", lastRunException.orElse("none"))
				.add("execute_hook", endpoint.resolve("./hooks/" + alias + "/execute").toString())
				.add("schedule_hook", endpoint.resolve("./hooks/" + alias + "/schedule?hours=3").toString())
				.add("cancel_hook", endpoint.resolve("./hooks/" + alias + "/cancel").toString())
				.build();
	}

}
