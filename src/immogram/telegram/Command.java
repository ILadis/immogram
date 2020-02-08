package immogram.telegram;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class Command extends Handler {

	private final String prefix;
	private final Runner runner;
	private InlineKeyboard keyboard;

	public Command(String prefix) {
		this.prefix = prefix;
		this.runner = this::execute;
	}

	public Command(String prefix, Runner runner) {
		this.prefix = prefix;
		this.runner = runner;
	}

	@Override
	public void handle(TelegramApi telegram, TextMessage message) {
		var wrapped = new WrappedTelegramApi(telegram);
		var command = message.text().orElse("");

		if (command.startsWith(prefix)) {
			runner.run(wrapped, message);
		}
	}

	@Override
	public void handle(TelegramApi telegram, CallbackQuery callback) {
		if (keyboard != null) {
			keyboard.actionOf(callback).ifPresent(action -> {
				telegram.answerCallbackQuery(callback);
				action.run();
			});
		}
	}

	protected void execute(TelegramApi telegram, TextMessage message) { }

	public static interface Runner {
		void run(TelegramApi telegram, TextMessage message);
	}

	class WrappedTelegramApi implements TelegramApi {

		private final TelegramApi delegate;

		private WrappedTelegramApi(TelegramApi delegate) {
			this.delegate = delegate;
		}

		@Override
		public List<Update<?>> pollUpdates(Duration timeout, Optional<Integer> offset) {
			return delegate.pollUpdates(timeout, offset);
		}

		@Override
		public void sendTextMessage(TextMessage message) {
			keyboard = message.replyKeyboard().orElse(null);
			delegate.sendTextMessage(message);
		}

		@Override
		public void editTextMessage(TextMessage message) {
			keyboard = message.replyKeyboard().orElse(null);
			delegate.editTextMessage(message);
		}

		@Override
		public void answerCallbackQuery(CallbackQuery callback) {
			delegate.answerCallbackQuery(callback);
		}
	}

}
