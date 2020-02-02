package immogram;

import java.net.URI;
import java.net.http.HttpClient;
import java.sql.DriverManager;
import java.time.Duration;
import java.util.Collection;

import immogram.bot.ImmogramBot;
import immogram.repository.LinkRepository;
import immogram.task.LinkToText;
import immogram.task.Retry;
import immogram.task.SaveAndFilter;
import immogram.task.ScrapeWeb;
import immogram.task.Task;
import immogram.task.TaskFactory;
import immogram.telegram.TelegramApi;
import immogram.telegram.http.HttpTelegramApi;
import immogram.webdriver.WebDriver;
import immogram.webdriver.http.HttpWebDriver;
import immogram.webscraper.EbayWebScraper;
import immogram.webscraper.ImmoweltWebScraper;
import immogram.webscraper.WebScraper;

public class Bootstrap {

	private LinkRepository linkRepository;
	private WebDriver webDriver;
	private TelegramApi telegramApi;
	private ImmogramBot immogramBot;
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

	public ImmogramBot immogramBot() {
		if (immogramBot == null) {
			immogramBot = new ImmogramBot(telegramApi);
		}
		return immogramBot;
	}

	public TaskFactory<String, Void, Collection<String>> immoweltScraperTask() {
		return term -> scraperTask(new ImmoweltWebScraper(term));
	}

	public TaskFactory<String, Void, Collection<String>> ebayScraperTask() {
		return term -> scraperTask(new EbayWebScraper(term));
	}

	private Task<Void, Collection<String>> scraperTask(WebScraper<Link> scraper) {
		return new Retry<>(5, Duration.ofMillis(100), new ScrapeWeb<>(webDriver, scraper))
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
