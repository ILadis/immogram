package immogram.telegram;

public class Update<M> {

	private final Integer id;
	private final M message;

	public Update(Integer id, M message) {
		this.id = id;
		this.message = message;
	}

	public Integer id() {
		return id;
	}

	public M message() {
		return message;
	}
}
