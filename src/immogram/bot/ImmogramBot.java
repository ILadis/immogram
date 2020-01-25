package immogram.bot;

import immogram.bot.ScraperCommand.ScraperTask;
import immogram.telegram.PollingBot;
import immogram.telegram.TelegramApi;

public class ImmogramBot extends PollingBot {

	private final ObeyCommand obey;
	private final ScraperCommand scraper;

	public ImmogramBot(TelegramApi telegram) {
		super(telegram);
		this.obey = new ObeyCommand();
		this.scraper = new ScraperCommand();
		registerAll(obey, obey.wrap(scraper));
	}

	public void addScraperTask(ScraperTask task) {
		scraper.addTask(task);
	}
}
