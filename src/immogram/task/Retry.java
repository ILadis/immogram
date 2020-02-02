package immogram.task;

import java.time.Duration;

import immogram.Exceptions;

public class Retry<I, O> implements Task<I, O> {

	private final Integer attempts;
	private final Duration backoff;
	private final Task<I, O> task;

	public Retry(Integer attempts, Duration backoff, Task<I, O> task) {
		this.attempts = attempts;
		this.backoff = backoff;
		this.task = task;
	}

	@Override
	public O execute(I input) {
		var retry = 0;

		while (true) try {
			var output = task.execute(input);
			return output;
		} catch (Exception e) {
			waitBackoff(++retry, e);
		}
	}

	private void waitBackoff(Integer retry, Exception cause) {
		if (retry > attempts) {
			Exceptions.throwUnchecked(cause);
		}

		var slots = Math.pow(2, retry.doubleValue()) - 1;
		var wait = Math.round(slots * backoff.toMillis());

		try {
			Thread.sleep(wait);
		} catch (InterruptedException e) {
			Exceptions.throwUnchecked(e);
		}
	}

}
