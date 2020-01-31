package immogram;

import java.net.URI;
import java.net.http.HttpClient;
import java.sql.DriverManager;
import java.util.Collection;

import immogram.repository.LinkRepository;
import immogram.task.LinkToText;
import immogram.task.SaveAndFilter;
import immogram.task.ScrapeWeb;
import immogram.task.TaskFactory;
import immogram.telegram.TelegramApi;
import immogram.telegram.http.HttpTelegramApi;
import immogram.webdriver.WebDriver;
import immogram.webdriver.http.HttpWebDriver;
import immogram.webscraper.EbayWebScraper;
import immogram.webscraper.ImmoweltWebScraper;

public class Bootstrap {

	private LinkRepository linkRepository;
	private WebDriver webDriver;
	private TelegramApi telegramApi;
	private HttpClient httpClient;

	public LinkRepository linkRepository() {
		return linkRepository;
	}

	public WebDriver webDriver() {
		return webDriver;
	}

	public TelegramApi telegramApi() {
		return telegramApi;
	}

	public TaskFactory<String, Void, Collection<String>> immoweltScraperTask() {
		return term -> new ScrapeWeb<>(webDriver, new ImmoweltWebScraper(term))
				.pipe(new SaveAndFilter<>(linkRepository, Link::href))
				.pipe(new LinkToText());
	}

	public TaskFactory<String, Void, Collection<String>> ebayScraperTask() {
		return term -> new ScrapeWeb<>(webDriver, new EbayWebScraper(term))
				.pipe(new SaveAndFilter<>(linkRepository, Link::href))
				.pipe(new LinkToText());
	}

	private HttpClient httpClient() {
		if (httpClient == null) {
			httpClient = HttpClient.newHttpClient();
		}
		return httpClient;
	}

	public static Builder newBuilder() {
		var target = new Bootstrap();
		return target.new Builder();
	}

	public class Builder {
		public Builder jdbcUrl(String url) throws Throwable {
			var conn = DriverManager.getConnection(url);
			linkRepository = LinkRepository.openNew(conn);
			return this;
		}

		public Builder webDriverEndpoint(String uri) throws Throwable {
			var root = URI.create(uri);
			webDriver = new HttpWebDriver(httpClient(), root);
			return this;
		}

		public Builder telegramApiTokenAndEndpoint(String token, String uri) throws Throwable {
			var root = URI.create(uri);
			telegramApi = new HttpTelegramApi(httpClient(), root, token);
			return this;
		}

		public Bootstrap build() {
			return Bootstrap.this;
		}
	}

}
