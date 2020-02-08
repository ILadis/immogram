package immogram.task;

import java.util.Collection;
import java.util.function.Supplier;

import immogram.telegram.TelegramApi;
import immogram.telegram.TextMessage;

public class SendTextMessages implements Task<Collection<String>, Void> {

	private final TelegramApi telegram;
	private final Supplier<Integer> chatId;

	public SendTextMessages(TelegramApi telegram, Supplier<Integer> chatId) {
		this.telegram = telegram;
		this.chatId = chatId;
	}

	@Override
	public Void execute(Collection<String> input) {
		for (var text : input) {
			var message = new TextMessage(chatId.get(), text);
			telegram.sendTextMessage(message);
		}
		return null;
	}

}
