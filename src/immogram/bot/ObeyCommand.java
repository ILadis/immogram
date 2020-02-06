package immogram.bot;

import java.util.function.Supplier;

import immogram.telegram.CallbackQuery;
import immogram.telegram.Command;
import immogram.telegram.Handler;
import immogram.telegram.TelegramApi;
import immogram.telegram.TextMessage;

class ObeyCommand extends Command {

	private final Messages messages;
	private Integer chatId, userId;

	public ObeyCommand(Messages messages) {
		super("/start");
		this.messages = messages;
	}

	public Handler wrap(Handler command) {
		return new ObeyingHandler(command);
	}

	public Supplier<Integer> chatId() {
		return () -> chatId;
	}

	@Override
	protected void execute(TelegramApi telegram, TextMessage message) {
		if (isFromObeyingUser(message)) {
			obeyUserAndChat(message);

			var response = message.response(messages.obeyingChat());
			telegram.sendTextMessage(response);
		}
	}

	private boolean isFromObeyingUser(TextMessage message) {
		return userId == null || userId.equals(message.userId());
	}

	private boolean isFromObeyingChat(TextMessage message) {
		return userId != null && userId.equals(message.userId())
				|| chatId != null && chatId.equals(message.chatId());
	}

	private void obeyUserAndChat(TextMessage message) {
		userId = message.userId();
		chatId = message.chatId();
	}

	public class ObeyingHandler extends Handler {

		private final Handler delegate;

		public ObeyingHandler(Handler delegate) {
			this.delegate = delegate;
		}

		@Override
		public void handle(TelegramApi telegram, TextMessage message) {
			if (isFromObeyingChat(message)) {
				delegate.handle(telegram, message);
			}
		}

		@Override
		public void handle(TelegramApi telegram, CallbackQuery callback) {
			delegate.handle(telegram, callback);
		}
	}

}
