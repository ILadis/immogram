package immogram.bot;

import java.time.Duration;
import java.util.function.Consumer;

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

	public TasksCommand(Messages messages, TaskManager manager) {
		super("/tasks");
		this.messages = messages;
		this.manager = manager;
	}

	@Override
	protected void execute(TelegramApi telegram, TextMessage message) {
		var text = messages.taskListing();
		var keyboard = newListingKeyboard(telegram);
		telegram.sendTextMessage(message.response(text, keyboard));
	}

	private Consumer<CallbackQuery> showListing(TelegramApi telegram) {
		return callback -> {
			var message = callback.message().get();
			var text = messages.taskListing();
			var keyboard = newListingKeyboard(telegram);
			telegram.editTextMessage(message.edit(text, keyboard));
		};
	}

	private InlineKeyboard newListingKeyboard(TelegramApi telegram) {
		var keyboard = InlineKeyboard.newBuilder();
		for (var task : manager.listTasks()) {
			keyboard.addRow().addButton(task.alias(), showStatus(telegram, task));
		}
		return keyboard.build();
	}

	private Consumer<CallbackQuery> showStatus(TelegramApi telegram, ManagedTask task) {
		return callback -> {
			var message = callback.message().get();
			var text = messages.taskStatus(task);
			var keyboard = newStatusKeyboard(telegram, task);
			telegram.editTextMessage(message.edit(text, keyboard));
		};
	}

	private InlineKeyboard newStatusKeyboard(TelegramApi telegram, ManagedTask task) {
		return InlineKeyboard.newBuilder()
				.addRow()
				.addButton(messages.scheduleOrCancelTask(), scheduleOrCancel(telegram, task))
				.addButton(messages.showLastRunException(), showException(telegram, task))
				.addRow()
				.addButton(messages.taskBackToListing(), showListing(telegram))
				.build();
	}

	private Consumer<CallbackQuery> showException(TelegramApi telegram, ManagedTask task) {
		return callback -> {
			var message = callback.message().get();
			var exception = task.lastRunException();
			if (exception.isPresent()) {
				var text = messages.taskWithException(task);
				var keyboard = newBackToStatusKeyboard(telegram, task);
				telegram.editTextMessage(message.edit(text, keyboard));
			} else {
				var text = messages.taskWithoutException(task);
				var keyboard = newBackToStatusKeyboard(telegram, task);
				telegram.editTextMessage(message.edit(text, keyboard));
			}
		};
	}

	private Consumer<CallbackQuery> scheduleOrCancel(TelegramApi telegram, ManagedTask task) {
		return callback -> {
			var message = callback.message().get();
			if (task.isScheduled()) {
				task.cancel();
				var text = messages.taskCancelled(task);
				var keyboard = newBackToStatusKeyboard(telegram, task);
				telegram.editTextMessage(message.edit(text, keyboard));
			} else {
				task.schedule(Duration.ofHours(3));
				var text = messages.taskScheduled(task);
				var keyboard = newBackToStatusKeyboard(telegram, task);
				telegram.editTextMessage(message.edit(text, keyboard));
			}
		};
	}

	private InlineKeyboard newBackToStatusKeyboard(TelegramApi telegram, ManagedTask task) {
		return InlineKeyboard.newBuilder()
				.addRow()
				.addButton(messages.taskBackToStatus(), showStatus(telegram, task))
				.build();
	}

}
