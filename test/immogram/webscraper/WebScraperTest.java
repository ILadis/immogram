package immogram.webscraper;

import java.util.Collection;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;

import immogram.Link;
import immogram.webdriver.WebDriver;
import immogram.webscraper.WebScraperProvider.WebScraperFactory;
import immogram.webscraper.WebScraperProvider.WebScraperSource;

@ExtendWith(WebDriverExtension.class)
class WebScraperTest {

	@ParameterizedTest
	@WebScraperSource({ ImmoscoutWebScraper.class, ImmoweltWebScraper.class, EbayWebScraper.class })
	void test(WebScraperFactory<Collection<Link>> factory, WebDriver driver) {
		var term = System.getProperty("term");
		var scraper = factory.createNew(term);

		var links = scraper.execute(driver);

		for (var link : links) {
			var output = String.format("%s (%s)", link.title(), link.href());
			System.out.println(output);
		}
	}

}
