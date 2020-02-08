package immogram.telegram;

import java.util.Optional;

public class TextMessage {

	private final Integer id;
	private final Integer chatId;
	private final Integer userId;
	private final String text;
	private InlineKeyboard replyKeyboard;

	public TextMessage(Integer id, Integer chatId, Integer userId, String text) {
		this.id = id;
		this.chatId = chatId;
		this.userId = userId;
		this.text = text;
	}

	public Optional<Integer> id() {
		return Optional.ofNullable(id);
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
		return new TextMessage(null, chatId, 0, text);
	}

	public TextMessage response(String text, InlineKeyboard replyKeyboard) {
		var message = new TextMessage(null, chatId, 0, text);
		message.replyKeyboard = replyKeyboard;
		return message;
	}

	public TextMessage edit(String text) {
		return new TextMessage(id, chatId, userId, text);
	}

	public TextMessage edit(String text, InlineKeyboard replyKeyboard) {
		var message = new TextMessage(id, chatId, userId, text);
		message.replyKeyboard = replyKeyboard;
		return message;
	}
}
