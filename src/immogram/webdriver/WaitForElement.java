package immogram.webdriver;

import java.time.Duration;

import immogram.Exceptions;

public interface WaitForElement {

	Element findElement(By selector);

	default Element waitForElement(By selector, Duration timeout) {
		var sleep = Duration.ofSeconds(1);
		var exception = new Exception();

		while (!timeout.isNegative()) {
			try {
				return findElement(selector);
			} catch (Exception e) {
				exception = e;
				timeout = timeout.minus(sleep);
			}

			try {
				Thread.sleep(sleep.toMillis());
			} catch (InterruptedException e) {
				Exceptions.throwUnchecked(exception);
			}
		}

		return Exceptions.throwUnchecked(exception);
	}

}
