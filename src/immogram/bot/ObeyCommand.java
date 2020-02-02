package immogram.bot;

import immogram.telegram.Command;
import immogram.telegram.Handler;
import immogram.telegram.TelegramApi;
import immogram.telegram.TextMessage;

public class ObeyCommand extends Command {

	private Integer chatId, userId;

	public ObeyCommand() {
		super("/start");
	}

	public Handler<TextMessage> wrap(Handler<TextMessage> command) {
		return new ObeyingHandler(command);
	}

	@Override
	protected void execute(TelegramApi telegram, TextMessage message) {
		if (isFromObeyingUser(message)) {
			obeyUserAndChat(message);
			telegram.sendTextMessage(message.response("I'll obey this user/chat now!"));
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

	public class ObeyingHandler implements Handler<TextMessage> {

		private final Handler<TextMessage> delegate;

		public ObeyingHandler(Handler<TextMessage> delegate) {
			this.delegate = delegate;
		}

		@Override
		public void handle(TelegramApi telegram, TextMessage message) {
			if (isFromObeyingChat(message)) {
				delegate.handle(telegram, message);
			}
		}
	}

}
