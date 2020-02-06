package immogram;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Exceptions {

	@SuppressWarnings("unchecked")
	private static <T extends Exception> void throwAsUnchecked(Throwable exception) throws T {
		throw (T) exception;
	}

	public static <R> R throwUnchecked(Throwable exception) {
		Exceptions.<RuntimeException>throwAsUnchecked(exception);
		return null;
	}

	public static String stackTraceOf(Throwable exception) {
		StringWriter writer = new StringWriter();
		PrintWriter printer = new PrintWriter(writer);
		exception.printStackTrace(printer);
		return writer.toString();
	}

}
