package immogram.webscraper.utils;

import java.time.Duration;
import java.util.Optional;

import immogram.Exceptions;

public class Retry {

	public static Optional<Exception> suppress(int times, Runnable action) {
		var delay = Duration.ofSeconds(1);
		return suppress(times, delay, action);
	}

	public static Optional<Exception> suppress(int times, Duration delay, Runnable action) {
		var exception = (Exception) null;

		while (times > 0) {
			try {
				action.run();
				return Optional.empty();
			} catch (Exception e) {
				exception = e;
				times--;
			}

			try {
				Thread.sleep(delay.toMillis());
			} catch (InterruptedException e) {
				Exceptions.throwUnchecked(exception);
			}
		}

		// suppress exception if any occured
		return Optional.ofNullable(exception);
	}

}
