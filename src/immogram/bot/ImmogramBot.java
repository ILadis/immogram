package immogram.bot;

import java.util.Locale;
import java.util.function.Supplier;

import immogram.bot.PingCommand.PongCommand;
import immogram.task.TaskManager;
import immogram.telegram.PollingBot;
import immogram.telegram.TelegramApi;

public class ImmogramBot extends PollingBot {

	private final Messages messages;
	private final TaskManager taskManager;
	private final ObeyCommand obeyCommand;

	public ImmogramBot(TelegramApi telegram, Locale locale) {
		super(telegram);
		this.messages = new Messages(locale);
		this.taskManager = new TaskManager();
		this.obeyCommand = new ObeyCommand(messages);
		registerCommands();
	}

	private void registerCommands() {
		registerAll(obeyCommand,
				obeyCommand.wrap(new TasksCommand(messages, taskManager)),
				obeyCommand.wrap(new CreateTaskCommand(messages, taskManager)),
				obeyCommand.wrap(new PingCommand()),
				obeyCommand.wrap(new PongCommand()));
	}

	public TaskManager taskManager() {
		return taskManager;
	}

	public Supplier<Integer> obeyingChat() {
		return obeyCommand.chatId();
	}
}
