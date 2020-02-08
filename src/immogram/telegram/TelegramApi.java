package immogram.telegram;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface TelegramApi {
	List<Update<?>> pollUpdates(Duration timeout, Optional<Integer> offset);
	void sendTextMessage(TextMessage message);
	void editTextMessage(TextMessage message);
	void answerCallbackQuery(CallbackQuery callback);
}
