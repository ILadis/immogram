package immogram.webscraper;

import java.util.Collection;

import immogram.webdriver.WebDriver;

public interface WebScraper<E> {
	Collection<E> execute(WebDriver driver);
}
