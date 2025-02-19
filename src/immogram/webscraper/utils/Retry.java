package immogram.webscraper.utils;

import java.time.Duration;

import immogram.Exceptions;

public class Retry {

	public static void suppress(int times, Runnable action) {
		var sleep = Duration.ofSeconds(1);
		var exception = new Exception();

		while (times > 0) {
			try {
				action.run();
				return;
			} catch (Exception e) {
				exception = e;
				times--;
			}

			try {
				Thread.sleep(sleep.toMillis());
			} catch (InterruptedException e) {
				Exceptions.throwUnchecked(exception);
			}
		}

		// suppress exception if any occured
	}

}
