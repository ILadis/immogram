package immogram.task;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class TaskManager {

	private final List<ManagedTask<?>> tasks;
	private final Timer timer;

	public TaskManager() {
		this.tasks = new ArrayList<>();
		this.timer = createTimer();
	}

	public <O> ManagedTask<O> register(String alias, Task<Void, O> task) {
		var managed = new ManagedTask<>(alias, task);
		this.tasks.add(managed);
		return managed;
	}

	public List<ManagedTask<?>> listAll() {
		return Collections.unmodifiableList(tasks);
	}

	private static Timer createTimer() {
		var timer = new Timer();
		var task = createTimerTask(() -> { });
		timer.schedule(task, Integer.MAX_VALUE, Integer.MAX_VALUE);
		return timer;
	}

	private static TimerTask createTimerTask(Runnable runnable) {
		return new TimerTask() {
			@Override
			public void run() {
				runnable.run();
			}
		};
	}

	public class ManagedTask<O> implements Task<Void, O> {

		private final String alias;
		private final Task<Void, O> delegate;
		private TimerTask task;
		private Duration runPeriod;
		private Instant lastRunTimestamp;
		private Exception lastRunException;

		public ManagedTask(String alias, Task<Void, O> delegate) {
			this.alias = alias;
			this.delegate = delegate;
		}

		public String alias() {
			return alias;
		}

		@Override
		public O execute(Void input) {
			lastRunException = null;
			lastRunTimestamp = Instant.now();

			try {
				return delegate.execute(null);
			} catch (Exception e) {
				lastRunException = e;
				throw e;
			}
		}

		public void schedule(Duration period) {
			if (task == null) {
				task = createTimerTask(() -> execute(null));
				runPeriod = period;
				timer.scheduleAtFixedRate(task, 0, period.toMillis());
			}
		}

		public void cancel() {
			if (task != null) {
				task.cancel();
				runPeriod = null;
				task = null;
			}
		}

		public Optional<Instant> lastRunTimestamp() {
			return Optional.ofNullable(lastRunTimestamp);
		}

		public Optional<Exception> lastRunException() {
			return Optional.ofNullable(lastRunException);
		}

		public Optional<Duration> runPeriod() {
			return Optional.ofNullable(runPeriod);
		}

		public boolean isScheduled() {
			return task != null;
		}
	}
}
