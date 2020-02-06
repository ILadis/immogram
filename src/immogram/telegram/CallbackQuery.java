package immogram.telegram;

import java.util.Optional;

public class CallbackQuery {

	private final String id;
	private final String data;

	public CallbackQuery(String id, String data) {
		this.id = id;
		this.data = data;
	}

	public String id() {
		return id;
	}

	public Optional<String> data() {
		return Optional.ofNullable(data);
	}
}
