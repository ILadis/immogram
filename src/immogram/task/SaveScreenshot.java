package immogram.task;

import java.net.URI;
import java.util.Collection;
import java.util.function.Function;

import immogram.Screenshot;
import immogram.repository.Repository;
import immogram.webdriver.WebDriver;
import immogram.webscraper.WebScraper;

public class SaveScreenshot<E> implements Task<Collection<E>, Collection<E>> {

	private final Repository<URI, Screenshot> repo;
	private final WebDriver driver;
	private final Function<URI, WebScraper<Screenshot>> factory;
	private final Function<E, URI> urifier;

	public SaveScreenshot(Repository<URI, Screenshot> repo, WebDriver driver, Function<URI, WebScraper<Screenshot>> factory, Function<E, URI> urifier) {
		this.repo = repo;
		this.driver = driver;
		this.factory = factory;
		this.urifier = urifier;
	}

	@Override
	public Collection<E> execute(Collection<E> input) {
		for (var entity : input) {
			var uri = urifier.apply(entity);
			var scraper = factory.apply(uri);

			var screenshot = scraper.execute(driver);
			repo.save(screenshot);
		}

		return input;
	}
}
