package immogram.telegram;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class PollingBot {

	private final TelegramApi telegram;
	private final Set<Handler<?>> handlers;
	private Optional<Integer> offset;

	public PollingBot(TelegramApi telegram) {
		this.telegram = telegram;
		this.handlers = new LinkedHashSet<>();
		this.offset = Optional.empty();
	}

	public void register(Handler<?> handler) {
		handlers.add(handler);
	}

	public void pollUpdates(Duration timeout) {
		var updates = telegram.pollTextMessageUpdates(timeout, offset);

		for (var update : updates) {
			advanceOffset(update.id());
			invokeHandlers(update);
		}
	}

	private void advanceOffset(Integer updateId) {
		offset = Optional.of(updateId + 1);
	}

	private void invokeHandlers(Update<?> update) {
		for (var handler : handlers) {
			invokeIfCompatible(handler, update);
		}
	}

	private boolean invokeIfCompatible(Handler<?> handler, Update<?> update) {
		var cls = handler.getClass();
		var message = update.message().getClass();

		try {
			var method = cls.getDeclaredMethod("handle", TelegramApi.class, message);
			method.invoke(handler, telegram, update.message());
			return true;
		} catch (ReflectiveOperationException e) {
			return false;
		}
	}

}