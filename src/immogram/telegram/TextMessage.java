package immogram.telegram;

import java.util.Optional;

public class TextMessage {

	private final Integer chatId;
	private final Optional<String> text;

	public TextMessage(Integer chatId, Optional<String> text) {
		this.chatId = chatId;
		this.text = text;
	}

	public Integer chatId() {
		return chatId;
	}

	public Optional<String> text() {
		return text;
	}
}
