package immogram.telegram.http;

import java.io.InputStream;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import immogram.telegram.CallbackQuery;
import immogram.telegram.TextMessage;
import immogram.telegram.Update;

class JsonReaders {

	static String forErrorDescription(InputStream in) {
		var reader = Json.createReader(in);

		return reader.readObject()
				.getString("description");
	}

	static List<Update<?>> forUpdates(InputStream in) {
		var reader = Json.createReader(in);

		var updates = reader.readObject()
				.getJsonArray("result");

		return updates.getValuesAs(JsonReaders::toUpdate);
	}

	private static Update<?> toUpdate(JsonObject object) {
		var updateId = object.getInt("update_id");

		if (object.containsKey("message")) {
			return new Update<>(updateId, toTextMessage(object));
		} else {
			return new Update<>(updateId, toCallbackQuery(object));
		}
	}

	private static TextMessage toTextMessage(JsonObject object) {
		var message = object.getJsonObject("message");

		var id = message.getInt("message_id");
		var chatId = message.getJsonObject("chat").getInt("id");
		var userId = message.getJsonObject("from").getInt("id");
		var text = message.getString("text", null);

		return new TextMessage(id, chatId, userId, text);
	}

	private static CallbackQuery toCallbackQuery(JsonObject object) {
		var callback = object.getJsonObject("callback_query");

		var id = callback.getString("id");
		var data = callback.getString("data", null);

		if (callback.containsKey("message")) {
			return new CallbackQuery(id, data, toTextMessage(callback));
		} else {
			return new CallbackQuery(id, data, null);
		}
	}

}
