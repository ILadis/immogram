package immogram.bot;

import java.util.Collection;

import immogram.task.Task;
import immogram.telegram.PollingBot;
import immogram.telegram.TelegramApi;

public class ImmogramBot extends PollingBot {

	private final ObeyCommand obey;
	private final TasksCommand scraper;

	public ImmogramBot(TelegramApi telegram) {
		super(telegram);
		this.obey = new ObeyCommand();
		this.scraper = new TasksCommand();
		registerAll(obey, obey.wrap(scraper));
	}

	public void addTask(Task<Void, Collection<String>> task) {
		scraper.addTask(task);
	}
}
