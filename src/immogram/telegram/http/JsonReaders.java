package immogram.telegram.http;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonObject;

import immogram.telegram.TextMessage;
import immogram.telegram.Update;

class JsonReaders {

	static String forErrorDescription(InputStream in) {
		var reader = Json.createReader(in);

		return reader.readObject()
				.getString("description");
	}

	static List<Update<TextMessage>> forTextMessageUpdates(InputStream in) {
		var reader = Json.createReader(in);

		var updates = reader.readObject()
				.getJsonArray("result");

		return updates.getValuesAs(JsonReaders::toTextMessage);
	}

	private static Update<TextMessage> toTextMessage(JsonObject object) {
		var updateId = object.getInt("update_id");

		var message = object.getJsonObject("message");

		var chatId = message.getJsonObject("chat").getInt("id");
		var userId = message.getJsonObject("from").getInt("id");
		var text = Optional.ofNullable(message.getString("text", null));

		return new Update<>(updateId, new TextMessage(chatId, userId, text));
	}

}
