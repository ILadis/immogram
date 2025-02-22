package immogram;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

import immogram.bot.ImmogramBot;
import immogram.feed.FeedServer;
import immogram.feed.http.JsonFeedHttpServer;
import immogram.repository.LinkRepository;
import immogram.repository.ScreenshotRepository;
import immogram.task.LinkToText;
import immogram.task.Retry;
import immogram.task.SaveAndFilter;
import immogram.task.SaveScreenshot;
import immogram.task.ScrapeWeb;
import immogram.task.SendTextMessages;
import immogram.task.Task;
import immogram.task.TaskFactory;
import immogram.task.TaskManager;
import immogram.telegram.TelegramApi;
import immogram.telegram.http.HttpTelegramApi;
import immogram.webdriver.WebDriver;
import immogram.webdriver.http.HttpWebDriver;
import immogram.webscraper.EbayWebScraper;
import immogram.webscraper.ImmoscoutWebScraper;
import immogram.webscraper.ImmoweltWebScraper;
import immogram.webscraper.ScreenshotWebScraper;
import immogram.webscraper.WebScraper;

public class Bootstrap {

	private Bootstrap() { }

	private Locale messagesLocale;
	private Connection sqlConnection;
	private LinkRepository linkRepository;
	private ScreenshotRepository screenshotRepository;
	private TaskManager taskManager;
	private WebDriver webDriver;
	private FeedServer feedServer;
	private TelegramApi telegramApi;
	private ImmogramBot immogramBot;
	private HttpClient httpClient;

	public LinkRepository linkRepository() {
		if (linkRepository == null) {
			linkRepository = LinkRepository.openNew(sqlConnection);
		}
		return linkRepository;
	}

	public ScreenshotRepository screenshotRepository() {
		if (screenshotRepository == null) {
			screenshotRepository = ScreenshotRepository.openNew(sqlConnection);
		}
		return screenshotRepository;
	}

	public TaskManager taskManager() {
		if (taskManager == null) {
			taskManager = new TaskManager();
		}
		return taskManager;
	}

	public WebDriver webDriver() {
		return webDriver;
	}

	public FeedServer feedServer() {
		if (feedServer == null) {
			feedServer = new JsonFeedHttpServer(taskManager(), linkRepository(), screenshotRepository());
		}
		return feedServer;
	}

	public TelegramApi telegramApi() {
		return telegramApi;
	}

	public ImmogramBot immogramBot() {
		if (immogramBot == null) {
			immogramBot = new ImmogramBot(telegramApi, taskManager(), messagesLocale);
		}
		return immogramBot;
	}

	public TaskFactory<String, Void, Void> immoscoutScraperTask() {
		return term -> scraperTask(new ImmoscoutWebScraper(term)).pipe(_ -> null);
	}

	public TaskFactory<String, Void, Void> immoscoutBotScraperTask() {
		return term -> sendMessages(scraperTask(new ImmoscoutWebScraper(term)));
	}

	public TaskFactory<String, Void, Void> immoweltScraperTask() {
		return term -> scraperTask(new ImmoweltWebScraper(term)).pipe(_ -> null);
	}

	public TaskFactory<String, Void, Void> immoweltBotScraperTask() {
		return term -> sendMessages(scraperTask(new ImmoweltWebScraper(term)));
	}

	public TaskFactory<String, Void, Void> ebayScraperTask() {
		return term -> scraperTask(new EbayWebScraper(term)).pipe(_ -> null);
	}

	public TaskFactory<String, Void, Void> ebayBotScraperTask() {
		return term -> sendMessages(scraperTask(new EbayWebScraper(term)));
	}

	private Task<Void, Collection<Link>> scraperTask(WebScraper<Collection<Link>> scraper) {
		return new Retry<>(5, Duration.ofMillis(100),
				      new ScrapeWeb<>(webDriver(), scraper))
				.pipe(new SaveAndFilter<>(linkRepository(), Link::href))
				.pipe(new SaveScreenshot<>(screenshotRepository(), webDriver(), ScreenshotWebScraper::new, Link::href));
	}

	private Task<Void, Void> sendMessages(Task<Void, Collection<Link>> task) {
		return task.pipe(new LinkToText()).pipe(new SendTextMessages(telegramApi(), immogramBot().obeyingChat()));
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

		private Builder() { }

		public Builder messagesLocale(Locale locale) {
			messagesLocale = locale;
			return this;
		}

		public Builder jdbcUrl(String url) throws Throwable {
			sqlConnection = DriverManager.getConnection(url);
			return this;
		}

		public Builder webDriverEndpoint(String uri) throws Throwable {
			return webDriverEndpoint(uri, null, true);
		}

		public Builder webDriverEndpoint(String uri, String profile, boolean headless) throws Throwable {
			var root = URI.create(uri);
			webDriver = new HttpWebDriver(httpClient(), root, Optional.ofNullable(profile), headless);
			return this;
		}

		public Builder feedServer(String host, int port, String uri) throws Throwable {
			var address = new InetSocketAddress(InetAddress.getByName(host), port);
			feedServer = new JsonFeedHttpServer(taskManager(), linkRepository(), screenshotRepository());
			feedServer.start(address, URI.create(uri));
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
