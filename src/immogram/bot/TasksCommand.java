package immogram.bot;

import java.time.Duration;

import immogram.task.TaskManager;
import immogram.task.TaskManager.ManagedTask;
import immogram.telegram.CallbackQuery;
import immogram.telegram.Command;
import immogram.telegram.InlineKeyboard;
import immogram.telegram.TelegramApi;
import immogram.telegram.TextMessage;

class TasksCommand extends Command {

	private final Messages messages;
	private final TaskManager manager;
	private InlineKeyboard keyboard;

	public TasksCommand(Messages messages, TaskManager manager) {
		super("/tasks");
		this.messages = messages;
		this.manager = manager;
	}

	@Override
	public void handle(TelegramApi telegram, CallbackQuery callback) {
		telegram.answerCallbackQuery(callback);
		if (keyboard != null) {
			keyboard.actionOf(callback).ifPresent(action -> action.run());
		}
	}

	@Override
	protected void execute(TelegramApi telegram, TextMessage message) {
		keyboard = newTasksKeyboard(telegram, message);
		var response = message.response(messages.taskListing(), keyboard);
		telegram.sendTextMessage(response);
	}

	private InlineKeyboard newTasksKeyboard(TelegramApi telegram, TextMessage message) {
		var keyboard = InlineKeyboard.newBuilder();

		for (var task : manager.listAll()) {
			keyboard.addRow().addButton(task.alias(), showTaskStatus(telegram, message, task));
		}

		return keyboard.build();
	}

	private Runnable showTaskStatus(TelegramApi telegram, TextMessage message, ManagedTask<?> task) {
		return () -> {
			keyboard = newStatusKeyboard(telegram, message, task);
			var response = message.response(messages.taskStatus(task), keyboard);
			telegram.sendTextMessage(response);
		};
	}

	private InlineKeyboard newStatusKeyboard(TelegramApi telegram, TextMessage message, ManagedTask<?> task) {
		return InlineKeyboard.newBuilder()
				.addRow()
				.addButton(messages.scheduleOrCancelTask(), scheduleOrCancelTask(telegram, message, task))
				.addButton(messages.showLastRunException(), showTaskException(telegram, message, task))
				.build();
	}

	private Runnable showTaskException(TelegramApi telegram, TextMessage message, ManagedTask<?> task) {
		return () -> {
			var exception = task.lastRunException();
			if (exception.isPresent()) {
				var response = message.response(messages.taskWithException(exception.get()));
				telegram.sendTextMessage(response);
			} else {
				var response = message.response(messages.taskWithoutException(task));
				telegram.sendTextMessage(response);
			}
		};
	}

	private Runnable scheduleOrCancelTask(TelegramApi telegram, TextMessage message, ManagedTask<?> task) {
		return () -> {
			if (task.isScheduled()) {
				task.cancel();
				var response = message.response(messages.taskCancelled(task));
				telegram.sendTextMessage(response);
			} else {
				task.schedule(Duration.ofHours(3));
				var response = message.response(messages.taskScheduled(task));
				telegram.sendTextMessage(response);
			}
		};
	}

}
