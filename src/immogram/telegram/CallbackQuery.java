package immogram.telegram;

import java.util.Optional;

public class CallbackQuery {

	private final String id;
	private final String data;
	private final TextMessage message;

	public CallbackQuery(String id, String data, TextMessage message) {
		this.id = id;
		this.data = data;
		this.message = message;
	}

	public String id() {
		return id;
	}

	public Optional<String> data() {
		return Optional.ofNullable(data);
	}

	public Optional<TextMessage> message() {
		return Optional.ofNullable(message);
	}
}
