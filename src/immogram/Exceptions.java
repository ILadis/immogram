package immogram;

public class Exceptions {

	@SuppressWarnings("unchecked")
	private static <T extends Exception> void throwAsUnchecked(Throwable exception) throws T {
		throw (T) exception;
	}

	public static <R> R throwUnchecked(Throwable exception) {
		Exceptions.<RuntimeException>throwAsUnchecked(exception);
		return null;
	}

}
