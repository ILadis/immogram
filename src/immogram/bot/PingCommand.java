package immogram.bot;

import immogram.telegram.Command;
import immogram.telegram.TelegramApi;
import immogram.telegram.TextMessage;

class PingCommand extends Command {

	public PingCommand() {
		super("/ping");
	}

	@Override
	protected void execute(TelegramApi telegram, TextMessage message) {
		telegram.sendTextMessage(message.response("/pong"));
	}

	public static class PongCommand extends Command {

		public PongCommand() {
			super("/pong");
		}

		@Override
		protected void execute(TelegramApi telegram, TextMessage message) {
			telegram.sendTextMessage(message.response("/ping"));
		}
	}

}
