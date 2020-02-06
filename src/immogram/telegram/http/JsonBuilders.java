package immogram.telegram.http;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import immogram.telegram.CallbackQuery;
import immogram.telegram.InlineKeyboard;
import immogram.telegram.TextMessage;

class JsonBuilders {

	static JsonObject forUpdate(Duration timeout, Optional<Integer> offset) {
		var json = Json.createObjectBuilder()
				.add("limit", 20)
				.add("timeout", timeout.getSeconds())
				.add("allowed_updates", Json.createArrayBuilder()
						.add("message")
						.add("callback_query")
						.build());

		if (offset.isPresent()) {
			json.add("offset", offset.get());
		}

		return json.build();
	}

	static JsonObject forTextMessage(TextMessage message) {
		var json = Json.createObjectBuilder()
				.add("chat_id", message.chatId())
				.add("text", message.text().get())
				.add("parse_mode", "markdown");

		var keyboard = message.replyKeyboard();
		if (keyboard.isPresent()) {
			json.add("reply_markup", Json.createObjectBuilder()
					.add("inline_keyboard", forInlineKeyboard(keyboard.get())));
		}

		return json.build();
	}

	static JsonObject forCallbackQuery(CallbackQuery callback) {
		var json = Json.createObjectBuilder()
				.add("callback_query_id", callback.id());

		return json.build();
	}

	static JsonArray forInlineKeyboard(InlineKeyboard keyboard) {
		var json = Json.createArrayBuilder();

		for (var buttons : keyboard.buttons()) {
			json.add(forInlineKeyboardButtons(buttons));
		}

		return json.build();
	}

	static JsonArray forInlineKeyboardButtons(List<InlineKeyboard.Button> buttons) {
		var json = Json.createArrayBuilder();

		for (var button : buttons) {
			json.add(Json.createObjectBuilder()
					.add("text", button.text())
					.add("callback_data", button.data().toString()));
		}

		return json.build();
	}

}
