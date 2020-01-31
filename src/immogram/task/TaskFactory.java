package immogram.task;

public interface TaskFactory<P, I, O> {
	Task<I, O> create(P param);
}
