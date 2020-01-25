package immogram.bot;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import immogram.telegram.Command;
import immogram.telegram.TelegramApi;
import immogram.telegram.TextMessage;

public class ScraperCommand extends Command {

	private final Timer timer;
	private final List<ScraperTask> tasks;

	public ScraperCommand() {
		super("/scrape");
		this.timer = new Timer();
		this.tasks = new ArrayList<>();
	}

	public void addTask(ScraperTask task) {
		this.tasks.add(task);
	}

	@Override
	protected void execute(TelegramApi telegram, TextMessage message) {
		telegram.sendTextMessage(message.response("Scheduling scrapers!"));
		var task = createTimerTask(telegram, message);
		scheduleTimerTask(task, Duration.ofMinutes(5));
	}

	private void runTasks(TelegramApi telegram, TextMessage message) {
		for (var task : tasks) {
			var results = task.run();
			for (var result : results) {
				telegram.sendTextMessage(message.response(result));
			}
		}
	}

	private TimerTask createTimerTask(TelegramApi telegram, TextMessage message) {
		return new TimerTask() {
			@Override
			public void run() {
				runTasks(telegram, message);
			}
		};
	}

	private void scheduleTimerTask(TimerTask task, Duration period) {
		timer.scheduleAtFixedRate(task, 0, period.toMillis());
	}

	public static interface ScraperTask {
		Collection<String> run();
	}

}
