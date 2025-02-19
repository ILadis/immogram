package immogram.webscraper;

import java.lang.ProcessBuilder.Redirect;
import java.net.URI;
import java.net.http.HttpClient;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;

import immogram.webdriver.WebDriver;
import immogram.webdriver.http.HttpWebDriver;

class WebDriverExtension extends TypeBasedParameterResolver<WebDriver> implements AfterAllCallback, BeforeAllCallback {

	private Process geckodriver;
	private HttpClient client;
	private URI uri;

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		geckodriver = new ProcessBuilder("geckodriver")
				.redirectOutput(Redirect.INHERIT)
				.redirectError(Redirect.INHERIT)
				.start();
		client = HttpClient.newHttpClient();
		uri = URI.create("http://localhost:4444");
	}

	@Override
	public WebDriver resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return new HttpWebDriver(client, uri, false);
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		geckodriver.destroy();
	}

}
