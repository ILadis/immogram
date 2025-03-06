package immogram.webscraper;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;

import immogram.Link;
import immogram.SearchQuery;
import immogram.webdriver.WebDriver;
import immogram.webscraper.WebScraperProvider.WebScraperFactory;
import immogram.webscraper.WebScraperProvider.WebScraperSource;

@ExtendWith(WebDriverExtension.class)
class WebScraperTest {

	@ParameterizedTest
	@WebScraperSource({ ImmoscoutWebScraper.class, ImmoweltWebScraper.class, EbayWebScraper.class })
	void test(WebScraperFactory<Collection<Link>> factory, WebDriver driver) {
		var city = System.getProperty("city");
		var queries = List.of(
				SearchQuery.forRentingAppartment(city),
				SearchQuery.forBuyingAppartment(city));

		for (var query : queries) {
			var scraper = factory.createNew(query);
			var links = scraper.execute(driver);

			for (var link : links) {
				var output = String.format("%s (%s)", link.title(), link.href());
				System.out.println(output);
			}
		}
	}
}
