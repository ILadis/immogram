package immogram.task;

public interface Task<I, O> {
	O execute(I input);

	default <R> Task<I, R> pipe(Task<O, R> next) {
		return input -> next.execute(execute(input));
	}
}
