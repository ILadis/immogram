package immogram.bot;

import immogram.bot.PingCommand.PongCommand;
import immogram.telegram.PollingBot;
import immogram.telegram.TelegramApi;

public class ImmogramBot extends PollingBot {

	private final ObeyCommand obey;
	private final TasksCommand tasks;

	public ImmogramBot(TelegramApi telegram) {
		super(telegram);
		this.obey = new ObeyCommand();
		this.tasks = new TasksCommand();
		registerAll(obey, obey.wrap(tasks));
		register(obey.wrap(new PingCommand()));
		register(obey.wrap(new PongCommand()));
	}

	public TasksCommand tasks() {
		return tasks;
	}
}
