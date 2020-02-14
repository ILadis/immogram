package immogram.webscraper;

import immogram.webdriver.WebDriver;

public interface WebScraper<E> {
	E execute(WebDriver driver);
}
