package immogram.telegram;

public abstract class Handler {
	public void handle(TelegramApi telegram, TextMessage message) { }
	public void handle(TelegramApi telegram, CallbackQuery callback) { }
}
