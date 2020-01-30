package immogram.bot;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import immogram.task.Task;
import immogram.telegram.Command;
import immogram.telegram.TelegramApi;
import immogram.telegram.TextMessage;

public class TasksCommand extends Command {

	private final List<Task<Void, Collection<String>>> tasks;
	private final Timer timer;
	private TimerTask task;
	private Duration period;

	public TasksCommand() {
		super("/tasks");
		this.tasks = new ArrayList<>();
		this.timer = new Timer();
		this.period = Duration.ofMinutes(5);
	}

	public void add(Task<Void, Collection<String>> task) {
		this.tasks.add(task);
	}

	public void setPeriod(Duration period) {
		this.period = period;
	}

	@Override
	protected void execute(TelegramApi telegram, TextMessage message) {
		if (task == null) {
			telegram.sendTextMessage(message.response("Scheduling tasks!"));
			task = createTimerTask(telegram, message);
			scheduleTimerTask(task, period);
		}
	}

	private void runTask(Task<Void, Collection<String>> task, TelegramApi telegram, TextMessage message) {
		var texts = task.execute(null);

		for (var text : texts) {
			var response = toTextMessage(message, text);
			telegram.sendTextMessage(response);
		}
	}

	private TextMessage toTextMessage(TextMessage message, String text) {
		var response = message.response(text);
		response.enableMarkdown();
		return response;
	}

	private TimerTask createTimerTask(TelegramApi telegram, TextMessage message) {
		var tasks = new ArrayList<>(this.tasks);
		return new TimerTask() {
			@Override
			public void run() {
				for (var task : tasks) {
					runTask(task, telegram, message);
				}
			}
		};
	}

	private void scheduleTimerTask(TimerTask task, Duration period) {
		timer.scheduleAtFixedRate(task, 0, period.toMillis());
	}

}
