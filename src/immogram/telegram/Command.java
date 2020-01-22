package immogram.telegram;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Command implements Handler<TextMessage> {

	private final String prefix;
	private final Runner runner;

	public Command(String prefix, Runner runner) {
		this.prefix = prefix;
		this.runner = runner;
	}

	@Override
	public void handle(TelegramApi telegram, TextMessage message) {
		var command = message.text().orElse("");

		if (command.startsWith(prefix)) {
			var text = command.substring(prefix.length());
			var reply = runner.run(text);
			reply.thenAccept(sendReplyTo(telegram, message));
		}
	}

	private Consumer<String> sendReplyTo(TelegramApi telegram, TextMessage message) {
		return reply -> telegram.sendTextMessage(new TextMessage(message.chatId(), Optional.of(reply)));
	}

	public static interface Runner {
		CompletableFuture<String> run(String text);
	}

}
