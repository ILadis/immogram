package immogram.task;

public abstract class TaskFactory<P, I, O> {
	public abstract Task<I, O> create(P param);
}
