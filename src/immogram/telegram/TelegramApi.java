package immogram.telegram;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface TelegramApi {
	List<Update<TextMessage>> pollTextMessageUpdates(Duration timeout, Optional<Integer> offset);
	void sendTextMessage(TextMessage message);
}
