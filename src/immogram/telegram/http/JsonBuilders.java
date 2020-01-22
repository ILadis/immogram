package immogram.telegram.http;

import java.time.Duration;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonObject;

import immogram.telegram.TextMessage;

class JsonBuilders {

	static JsonObject forUpdate(Duration timeout, Optional<Integer> offset) {
		var body = Json.createObjectBuilder()
				.add("limit", 20)
				.add("timeout", timeout.getSeconds())
				.add("allowed_updates", Json.createArrayBuilder()
						.add("message")
						.build());

		if (offset.isPresent()) {
			body.add("offset", offset.get());
		}

		return body.build();
	}

	static JsonObject forTextMessage(TextMessage message) {
		return Json.createObjectBuilder()
				.add("chat_id", message.chatId())
				.add("text", message.text().get())
				.build();
	}

}
