package immogram.telegram;

public interface Handler<M> {
	void handle(TelegramApi telegram, M message);
}
