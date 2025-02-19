package immogram.webscraper;

import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import immogram.Link;
import immogram.webdriver.By;
import immogram.webdriver.Session;
import immogram.webdriver.WebDriver;

public class ImmoweltWebScraper implements WebScraper<Collection<Link>> {

	private final URI index;
	private final String city;

	public ImmoweltWebScraper(String city) {
		this.city = city;
		this.index = URI.create("https://www.immowelt.de");
	}

	@Override
	public Collection<Link> execute(WebDriver driver) {
		try (var session = Session.createNew(driver)) {
			session.navigateTo(index);

			closeOverlays(session);
			submitSearch(session, city);

			var links = new LinkedHashSet<Link>();
			do {
				addAllApartmentsOnPage(session, links);
			} while (gotoNextPage(session));

			return links;
		}
	}

	private void closeOverlays(Session session) {
		try {
			var dialog = session.findElement(By.cssSelector("#usercentrics-root"));
			var root = dialog.shadowRoot();

			var customize = root.waitForElement(By.cssSelector("[data-testid^=uc-customize]"), Duration.ofSeconds(8));
			customize.click();

			var deny = root.waitForElement(By.cssSelector("[data-testid^=uc-deny]"), Duration.ofSeconds(3));
			deny.click();
		} catch (Exception e) {
			// nothing to reject
		}
	}

	private void submitSearch(Session session, String city) {
		try {
			var button = session.findElement(By.cssSelector("[data-testid=wlhp-hero] button"));
			button.click();
		} catch (Exception e) {
			// no need to reset search
		}

		var tab = session.findElement(By.cssSelector("[data-key=RENT]"));
		tab.click();

		var input = session.findElement(By.cssSelector("[data-testid=refiner-form-test-id] input"));
		input.sendKeys(city);

		var option = session.waitForElement(By.cssSelector("[aria-label=Empfehlungen] li"), Duration.ofSeconds(3));
		option.click();
	}

	private void addAllApartmentsOnPage(Session session, Set<Link> links) {
		var elements = session.findElements(By.cssSelector("[data-testid^=serp-core] a"));

		for (var element : elements) {
			var title = element.attr("title");

			var path = URI.create(element.attr("href")).getPath();
			var href = index.resolve(path);

			var link = Link.newBuilder()
					.title(title)
					.href(href)
					.build();

			links.add(link);
		}
	}

	private boolean gotoNextPage(Session session) {
		try {
			var next = session.findElement(By.cssSelector("[aria-label^=nächste]"));
			next.click();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
