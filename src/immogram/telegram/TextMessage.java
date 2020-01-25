package immogram.telegram;

import java.util.Optional;

public class TextMessage {

	private final Integer chatId;
	private final Integer userId;
	private final Optional<String> text;

	public TextMessage(Integer chatId, Integer userId, Optional<String> text) {
		this.chatId = chatId;
		this.userId = userId;
		this.text = text;
	}

	public Integer chatId() {
		return chatId;
	}

	public Integer userId() {
		return userId;
	}

	public Optional<String> text() {
		return text;
	}

	public TextMessage response(String text) {
		return new TextMessage(chatId, null, Optional.of(text));
	}
}
