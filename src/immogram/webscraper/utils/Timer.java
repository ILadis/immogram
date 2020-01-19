package immogram.webscraper.utils;

import java.time.Duration;

public class Timer {

	public static boolean sleepFor(Duration duration) {
		try {
			Thread.sleep(duration.toMillis());
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}

}
