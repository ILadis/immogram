package immogram.bot;

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
	}

	public TasksCommand tasks() {
		return tasks;
	}
}
