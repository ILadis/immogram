package immogram.feed.http;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Supplier;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpExchange;

class AdminAuthenticator extends Authenticator {

	private final Supplier<String> password;

	protected AdminAuthenticator(Supplier<String> password) {
		this.password = password;
	}

	@Override
	public Result authenticate(HttpExchange exchange) {
		return basic.authenticate(exchange);
	}

	private boolean verify(String input) {
		String passwd = password.get();
		if (passwd == null || passwd.isEmpty()) {
			return false;
		}

		return Objects.equals(passwd, input);
	}

	private final BasicAuthenticator basic = new BasicAuthenticator("admin", StandardCharsets.UTF_8) {
		public @Override boolean checkCredentials(String username, String password) {
			return Objects.equals(username, "admin") && verify(password);
		}
	};

}
