package immogram.bot;

import java.util.function.Consumer;

import immogram.task.TaskManager;
import immogram.task.TaskManager.ManagedTask;
import immogram.telegram.CallbackQuery;
import immogram.telegram.Command;
import immogram.telegram.InlineKeyboard;
import immogram.telegram.TelegramApi;
import immogram.telegram.TextMessage;

class DeleteTaskCommand extends Command {

	private final Messages messages;
	private final TaskManager manager;

	public DeleteTaskCommand(Messages messages, TaskManager manager) {
		super("/deltask");
		this.messages = messages;
		this.manager = manager;
	}
	@Override
	protected void execute(TelegramApi telegram, TextMessage message) {
		var text = messages.taskListing();
		var keyboard = newListingKeyboard(telegram);
		telegram.sendTextMessage(message.response(text, keyboard));
	}

	private InlineKeyboard newListingKeyboard(TelegramApi telegram) {
		var keyboard = InlineKeyboard.newBuilder();
		for (var task : manager.listTasks()) {
			keyboard.addRow().addButton(task.alias(), deleteTask(telegram, task));
		}
		return keyboard.build();
	}
	private Consumer<CallbackQuery> deleteTask(TelegramApi telegram, ManagedTask task) {
		return (callback) -> {
			manager.remove(task);
			var message = callback.message().get();
			var text = messages.taskDeleted(task);
			telegram.editTextMessage(message.edit(text));
		};
	}

}
