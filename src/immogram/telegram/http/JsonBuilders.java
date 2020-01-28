package immogram.telegram.http;

import java.time.Duration;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonObject;

import immogram.telegram.TextMessage;

class JsonBuilders {

	static JsonObject forUpdate(Duration timeout, Optional<Integer> offset) {
		var json = Json.createObjectBuilder()
				.add("limit", 20)
				.add("timeout", timeout.getSeconds())
				.add("allowed_updates", Json.createArrayBuilder()
						.add("message")
						.build());

		if (offset.isPresent()) {
			json.add("offset", offset.get());
		}

		return json.build();
	}

	static JsonObject forTextMessage(TextMessage message) {
		var json = Json.createObjectBuilder()
				.add("chat_id", message.chatId())
				.add("text", message.text().get());

		if (message.isMarkdownEnabled()) {
			json.add("parse_mode", "markdown");
		}

		return json.build();
	}

}
