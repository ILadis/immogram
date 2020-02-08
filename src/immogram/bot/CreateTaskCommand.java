package immogram.bot;

import java.util.function.Consumer;

import immogram.task.TaskManager;
import immogram.task.TaskManager.ManagedTaskFactory;
import immogram.telegram.CallbackQuery;
import immogram.telegram.Command;
import immogram.telegram.InlineKeyboard;
import immogram.telegram.TelegramApi;
import immogram.telegram.TextMessage;

class CreateTaskCommand extends Command {

	private final Messages messages;
	private final TaskManager manager;

	public CreateTaskCommand(Messages messages, TaskManager manager) {
		super("/newtask");
		this.messages = messages;
		this.manager = manager;
	}

	@Override
	protected void execute(TelegramApi telegram, TextMessage message) {
		var text = messages.factoryListing();
		var keyboard = newListingKeyboard(telegram);
		telegram.sendTextMessage(message.response(text, keyboard));
	}

	private InlineKeyboard newListingKeyboard(TelegramApi telegram) {
		var keyboard = InlineKeyboard.newBuilder();
		for (var factory : manager.listFactories()) {
			keyboard.addRow().addButton(factory.alias(), requestTerm(telegram, factory));
		}
		return keyboard.build();
	}

	private Consumer<CallbackQuery> requestTerm(TelegramApi telegram, ManagedTaskFactory factory) {
		return (callback) -> {
			var message = callback.message().get();
			var text = messages.factoryRequestTerm();
			telegram.editTextMessage(message.edit(text));
			onNextMessage(createNewTask(factory));
		};
	}

	private Command.Runner createNewTask(ManagedTaskFactory factory) {
		return (telegram, message) -> {
			var term = message.text().get();
			var task = factory.create(term);
			var text = messages.factoryTaskCreated(task);
			telegram.sendTextMessage(message.response(text));
		};
	}

}
