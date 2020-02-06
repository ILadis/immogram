package immogram.telegram;

import java.util.Optional;

public class TextMessage {

	private final Integer chatId;
	private final Integer userId;
	private final String text;
	private InlineKeyboard replyKeyboard;

	public TextMessage(Integer chatId, Integer userId, String text) {
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
		return Optional.ofNullable(text);
	}

	public Optional<InlineKeyboard> replyKeyboard() {
		return Optional.ofNullable(replyKeyboard);
	}

	public TextMessage response(String text) {
		return new TextMessage(chatId, 0, text);
	}

	public TextMessage response(String text, InlineKeyboard replyKeyboard) {
		var message = new TextMessage(chatId, 0, text);
		message.replyKeyboard = replyKeyboard;
		return message;
	}
}
