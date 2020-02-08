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
	private InlineKeyboard keyboard;

	public TasksCommand(Messages messages, TaskManager manager) {
		super("/tasks");
		this.messages = messages;
		this.manager = manager;
	}

	@Override
	protected void execute(TelegramApi telegram, TextMessage message) {
		var text = messages.taskListing();
		message = message.response(text, newListingKeyboard(telegram));
		telegram.sendTextMessage(message);
		keyboard = message.replyKeyboard().orElse(null);
	}

	@Override
	public void handle(TelegramApi telegram, CallbackQuery callback) {
		telegram.answerCallbackQuery(callback);
		if (keyboard != null) {
			keyboard.actionOf(callback).ifPresent(action -> action.run());
		}
	}

	private Consumer<CallbackQuery> showListing(TelegramApi telegram) {
		return callback -> {
			var message = callback.message().get();
			var text = messages.taskListing();
			message = message.edit(text, newListingKeyboard(telegram));
			telegram.editTextMessage(message);
			keyboard = message.replyKeyboard().orElse(null);
		};
	}

	private InlineKeyboard newListingKeyboard(TelegramApi telegram) {
		var keyboard = InlineKeyboard.newBuilder();

		for (var task : manager.listAll()) {
			keyboard.addRow().addButton(task.alias(), showStatus(telegram, task));
		}

		return keyboard.build();
	}

	private Consumer<CallbackQuery> showStatus(TelegramApi telegram, ManagedTask<?> task) {
		return callback -> {
			var message = callback.message().get();
			var text = messages.taskStatus(task);
			message = message.edit(text, newStatusKeyboard(telegram, task));
			telegram.editTextMessage(message);
			keyboard = message.replyKeyboard().orElse(null);
		};
	}

	private InlineKeyboard newStatusKeyboard(TelegramApi telegram, ManagedTask<?> task) {
		return InlineKeyboard.newBuilder()
				.addRow()
				.addButton(messages.scheduleOrCancelTask(), scheduleOrCancel(telegram, task))
				.addButton(messages.showLastRunException(), showException(telegram, task))
				.addRow()
				.addButton(messages.taskBackToListing(), showListing(telegram))
				.build();
	}

	private Consumer<CallbackQuery> showException(TelegramApi telegram, ManagedTask<?> task) {
		return callback -> {
			var message = callback.message().get();
			var exception = task.lastRunException();
			if (exception.isPresent()) {
				var text = messages.taskWithException(task);
				var keyboard = newBackToStatusKeyboard(telegram, task);
				message = message.edit(text, keyboard);
			} else {
				var text = messages.taskWithoutException(task);
				var keyboard = newBackToStatusKeyboard(telegram, task);
				message = message.edit(text, keyboard);
			}
			telegram.editTextMessage(message);
			keyboard = message.replyKeyboard().orElse(null);
		};
	}

	private Consumer<CallbackQuery> scheduleOrCancel(TelegramApi telegram, ManagedTask<?> task) {
		return callback -> {
			var message = callback.message().get();
			if (task.isScheduled()) {
				task.cancel();
				var text = messages.taskCancelled(task);
				var keyboard = newBackToStatusKeyboard(telegram, task);
				message = message.edit(text, keyboard);
			} else {
				task.schedule(Duration.ofHours(3));
				var text = messages.taskScheduled(task);
				var keyboard = newBackToStatusKeyboard(telegram, task);
				message = message.edit(text, keyboard);
			}
			telegram.editTextMessage(message);
			keyboard = message.replyKeyboard().orElse(null);
		};
	}

	private InlineKeyboard newBackToStatusKeyboard(TelegramApi telegram, ManagedTask<?> task) {
		return InlineKeyboard.newBuilder()
				.addRow()
				.addButton(messages.taskBackToStatus(), showStatus(telegram, task))
				.build();
	}

}
