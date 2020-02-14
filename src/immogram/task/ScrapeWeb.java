package immogram.task;

import immogram.webdriver.WebDriver;
import immogram.webscraper.WebScraper;

public class ScrapeWeb<E> implements Task<Void, E> {

	private final WebDriver driver;
	private final WebScraper<E> scraper;

	public ScrapeWeb(WebDriver driver, WebScraper<E> scraper) {
		this.driver = driver;
		this.scraper = scraper;
	}

	@Override
	public E execute(Void input) {
		return scraper.execute(driver);
	}
}
