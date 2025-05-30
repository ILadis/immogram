package immogram.task;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class TaskManager {

	private final List<ManagedTaskFactory<?>> factories;
	private final List<ManagedTask> tasks;
	private final Timer timer;

	public TaskManager() {
		this.factories = new ArrayList<>();
		this.tasks = new ArrayList<>();
		this.timer = createTimer();
	}

	public <P> ManagedTaskFactory<P> register(String alias, TaskFactory<P, Void, Void> factory) {
		var managed = new ManagedTaskFactory<>(alias, factory);
		factories.add(managed);
		return managed;
	}

	@SuppressWarnings("unchecked")
	public <P> List<ManagedTaskFactory<P>> listFactories(Class<P> param) {
		List<ManagedTaskFactory<P>> factories = new LinkedList<>();

		for (ManagedTaskFactory<?> managed : this.factories) {
			try {
				Class<?> factory = managed.delegate.getClass();
				factory.getDeclaredMethod("create", param);
				factories.add((ManagedTaskFactory<P>) managed);
			} catch (NoSuchMethodException | SecurityException e) {
				continue; // factory param type does not match
			}
		}

		return Collections.unmodifiableList(factories);
	}

	public ManagedTask register(String alias, Task<Void, Void> task) {
		var managed = new ManagedTask(alias, task);
		this.tasks.add(managed);
		return managed;
	}

	public void remove(ManagedTask task) {
		task.cancel();
		this.tasks.remove(task);
	}

	public Optional<ManagedTask> taskByAlias(String alias) {
		return tasks.stream().filter(task -> Objects.equals(task.alias, alias)).findFirst();
	}

	public List<ManagedTask> listTasks() {
		return Collections.unmodifiableList(tasks);
	}

	private static Timer createTimer() {
		var timer = new Timer();
		var task = createTimerTask(() -> { });
		timer.scheduleAtFixedRate(task, Integer.MAX_VALUE, Integer.MAX_VALUE);
		return timer;
	}

	private static TimerTask createTimerTask(Runnable runnable) {
		return new TimerTask() {
			public @Override void run() {
				runnable.run();
			}
		};
	}

	public class ManagedTaskFactory<P> extends TaskFactory<P, Void, Void> {

		private final String alias;
		private final TaskFactory<P, Void, Void> delegate;

		private ManagedTaskFactory(String alias, TaskFactory<P, Void, Void> delegate) {
			this.alias = alias;
			this.delegate = delegate;
		}

		public String alias() {
			return alias;
		}

		@Override
		public ManagedTask create(P term) {
			var task = delegate.create(term);
			return register(alias + " - " + term, task);
		}
	}

	public class ManagedTask implements Task<Void, Void> {

		private final String alias;
		private final Task<Void, Void> delegate;
		private TimerTask task;
		private Duration runPeriod;
		private Instant lastRunTimestamp;
		private Exception lastRunException;

		private ManagedTask(String alias, Task<Void, Void> delegate) {
			this.alias = alias;
			this.delegate = delegate;
		}

		public String alias() {
			return alias;
		}

		@Override
		public Void execute(Void input) {
			lastRunException = null;
			lastRunTimestamp = Instant.now();

			try {
				return delegate.execute(null);
			} catch (Exception e) {
				lastRunException = e;
				return null;
			}
		}

		public void schedule(Duration period) {
			schedule(Duration.ZERO, period);
		}

		public void schedule(Duration delay, Duration period) {
			if (task == null) {
				task = createTimerTask(() -> execute(null));
				runPeriod = period;
				timer.scheduleAtFixedRate(task, delay.toMillis(), period.toMillis());
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
