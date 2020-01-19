package immogram.webscraper;

import immogram.webdriver.WebDriver;

public interface WebScraper<R> {
	R execute(WebDriver driver);
}
