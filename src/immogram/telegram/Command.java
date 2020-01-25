package immogram.telegram;

public class Command implements Handler<TextMessage> {

	private final String prefix;
	private final Runner runner;

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
		var command = message.text().orElse("");

		if (command.startsWith(prefix)) {
			runner.run(telegram, message);
		}
	}

	protected void execute(TelegramApi telegram, TextMessage message) { }

	public static interface Runner {
		void run(TelegramApi telegram, TextMessage message);
	}

}
