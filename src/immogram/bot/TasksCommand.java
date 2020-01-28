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

	private final Timer timer;
	private final List<Task<Void, Collection<String>>> tasks;

	public TasksCommand() {
		super("/tasks");
		this.timer = new Timer();
		this.tasks = new ArrayList<>();
	}

	public void addTask(Task<Void, Collection<String>> task) {
		this.tasks.add(task);
	}

	@Override
	protected void execute(TelegramApi telegram, TextMessage message) {
		telegram.sendTextMessage(message.response("Scheduling tasks!"));
		var task = createTimerTask(telegram, message);
		scheduleTimerTask(task, Duration.ofMinutes(5));
	}

	private void runTasks(TelegramApi telegram, TextMessage message) {
		for (var task : tasks) {
			var texts = task.execute(null);

			for (var text : texts) {
				var response = toTextMessage(message, text);
				telegram.sendTextMessage(response);
			}
		}
	}

	private TextMessage toTextMessage(TextMessage message, String text) {
		var response = message.response(text);
		response.enableMarkdown();
		return response;
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

}
