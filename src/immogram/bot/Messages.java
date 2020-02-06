package immogram.bot;

import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.ResourceBundle;

import immogram.Exceptions;
import immogram.task.TaskManager.ManagedTask;

class Messages {

	private final ResourceBundle bundle;
	private final DateTimeFormatter dateFormatter;
	private final DateTimeFormatter timeFormatter;

	public Messages(Locale locale) {
		this.bundle = ResourceBundle.getBundle("immogram.bot.Messages", locale);
		this.dateFormatter = DateTimeFormatter.ofPattern(bundle.getString("dateFormat"), locale).withZone(ZoneId.systemDefault());
		this.timeFormatter = DateTimeFormatter.ofPattern(bundle.getString("timeFormat"), locale).withZone(ZoneId.systemDefault());
	}

	public String obeyingChat() {
		return bundle.getString("obeyingChat");
	}

	public String taskListing() {
		return bundle.getString("taskListing");
	}

	public String taskScheduled(ManagedTask<?> task) {
		var pattern = bundle.getString("taskScheduled");
		return MessageFormat.format(pattern, task.alias());
	}

	public String taskCancelled(ManagedTask<?> task) {
		var pattern = bundle.getString("taskCancelled");
		return MessageFormat.format(pattern, task.alias());
	}

	public String taskWithException(Exception exception) {
		var trace = Exceptions.stackTraceOf(exception);
		var pattern = bundle.getString("taskWithException");
		return MessageFormat.format(pattern, trace);
	}

	public String taskWithoutException(ManagedTask<?> task) {
		var pattern = bundle.getString("taskWithoutException");
		return MessageFormat.format(pattern, task.alias());
	}

	public String taskStatus(ManagedTask<?> task) {
		var pattern = bundle.getString("taskStatus");
		return MessageFormat.format(pattern, task.alias(),
				taskIsScheduled(task),
				taskHasRunTimestamp(task),
				taskHasRunException(task));
	}

	public String scheduleOrCancelTask() {
		return bundle.getString("scheduleOrCancelTask");
	}

	public String showLastRunException() {
		return bundle.getString("showLastRunException");
	}

	private String taskIsScheduled(ManagedTask<?> task) {
		var period = task.runPeriod();
		if (period.isEmpty()) {
			return bundle.getString("taskIsNotScheduled");
		} else {
			var pattern = bundle.getString("taskIsScheduled");
			return MessageFormat.format(pattern, period.get().toHours());
		}
	}

	private String taskHasRunTimestamp(ManagedTask<?> task) {
		var timestamp = task.lastRunTimestamp();
		if (timestamp.isEmpty()) {
			return bundle.getString("taskHasNoRunTimestamp");
		} else {
			var pattern = bundle.getString("taskHasRunTimestamp");
			return MessageFormat.format(pattern, dateFormat(timestamp.get()), timeFormat(timestamp.get()));
		}
	}

	private String taskHasRunException(ManagedTask<?> task) {
		var exception = task.lastRunException();
		if (exception.isEmpty()) {
			return bundle.getString("taskHasNoException");
		} else {
			return bundle.getString("taskHasException");
		}
	}

	private String dateFormat(TemporalAccessor temporal) {
		return dateFormatter.format(temporal);
	}

	private String timeFormat(TemporalAccessor temporal) {
		return timeFormatter.format(temporal);
	}
}
