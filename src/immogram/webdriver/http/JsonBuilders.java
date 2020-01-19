package immogram.webdriver.http;

import java.net.URI;

import javax.json.Json;
import javax.json.JsonObject;

import immogram.webdriver.By;

class JsonBuilders {

	static JsonObject forUrl(URI url) {
		return Json.createObjectBuilder()
				.add("url", url.toString())
				.build();
	}

	static JsonObject forScript(String script) {
		return Json.createObjectBuilder()
				.add("args", Json.createArrayBuilder())
				.add("script", script.toString())
				.build();
	}

	static JsonObject forSelector(By selector) {
		return Json.createObjectBuilder()
				.add("using", selector.locator().toString())
				.add("value", selector.value())
				.build();
	}

	static JsonObject forText(String text) {
		return Json.createObjectBuilder()
				.add("text", text.toString())
				.build();
	}

}
