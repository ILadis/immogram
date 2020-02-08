package immogram.telegram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class InlineKeyboard {

	private InlineKeyboard() { }

	private Map<UUID, Consumer<CallbackQuery>> actions;
	private List<List<Button>> buttons;

	public List<List<Button>> buttons() {
		return buttons;
	}

	public Optional<Runnable> actionOf(CallbackQuery callback) {
		var data = callback.data();

		if (data.isEmpty()) {
			return Optional.empty();
		}

		var uuid = UUID.fromString(data.get());
		if (actions.containsKey(uuid)) {
			return Optional.of(() -> actions.get(uuid).accept(callback));
		}

		return Optional.empty();
	}

	public static class Button {

		private final String text;
		private final UUID data;

		private Button(String text, UUID data) {
			this.text = text;
			this.data = data;
		}

		public String text() {
			return text;
		}

		public UUID data() {
			return data;
		}
	}

	public static Builder newBuilder() {
		var target = new InlineKeyboard();
		target.actions = new HashMap<>();
		target.buttons = new ArrayList<>();
		return target.new Builder();
	}

	public class Builder {

		private Builder() { }

		private List<Button> row;

		public Builder addRow() {
			buttons.add(row = new ArrayList<>());
			return this;
		}

		public Builder addButton(String text, Consumer<CallbackQuery> action) {
			var data = UUID.randomUUID();
			var button = new Button(text, data);
			row.add(button);
			actions.put(data, action);
			return this;
		}

		public InlineKeyboard build() {
			return InlineKeyboard.this;
		}
	}
}
