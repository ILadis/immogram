package immogram.webscraper;

import java.net.URI;

import immogram.Screenshot;
import immogram.webdriver.By;
import immogram.webdriver.Session;
import immogram.webdriver.WebDriver;

public class ScreenshotWebScraper implements WebScraper<Screenshot> {

	private final URI target;

	public ScreenshotWebScraper(URI target) {
		this.target = target;
	}

	@Override
	public Screenshot execute(WebDriver driver) {
		try (var session = Session.createNew(driver)) {
			session.navigateTo(target);

			var body = session.findElement(By.cssSelector("body"));
			var screenshot = body.takeScreenshot();

			return Screenshot.newBuilder()
					.href(target)
					.bitmap(screenshot)
					.build();
		}
	}
}
