package immogram.webscraper;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import immogram.Exceptions;
import immogram.Link;
import immogram.SearchQuery;
import immogram.webdriver.By;
import immogram.webdriver.Session;
import immogram.webdriver.WebDriver;
import immogram.webscraper.utils.Retry;

public class ImmoweltWebScraper implements WebScraper<Collection<Link>> {

	private final URI index;
	private final SearchQuery query;

	public ImmoweltWebScraper(SearchQuery query) {
		this.query = query;
		this.index = URI.create("https://www.immowelt.de");
	}

	@Override
	public Collection<Link> execute(WebDriver driver) {
		try (var session = Session.createNew(driver)) {
			session.navigateTo(index);

			closeOverlays(session);
			submitSearch(session);

			Retry.suppress(3, () -> waitForResultsPage(session))
					.ifPresent(Exceptions::throwUnchecked);

			var links = new LinkedHashSet<Link>();
			do {
				Retry.suppress(3, () -> addAllApartmentsOnPage(session, links))
						.ifPresent(Exceptions::throwUnchecked);
			} while (gotoNextPage(session));

			return links;
		}
	}

	private void closeOverlays(Session session) {
		try {
			var dialog = session.findElement(By.cssSelector("#usercentrics-root"));
			var root = dialog.shadowRoot();

			var customize = root.waitForElement(By.cssSelector("[data-testid^=uc-customize]"), Duration.ofSeconds(16));
			customize.click();

			var deny = root.waitForElement(By.cssSelector("[data-testid^=uc-deny]"), Duration.ofSeconds(8));
			deny.click();
		} catch (Exception e) {
			// nothing to reject
		}
	}

	private void submitSearch(Session session) {
		try {
			var button = session.findElement(By.cssSelector("[data-testid=wlhp-hero] button"));
			button.click();
		} catch (Exception e) {
			// no need to reset search if button is not present
		}

		var tab = switch (query.marketing()) {
			case BUY  -> session.findElement(By.cssSelector("[data-key=BUY]"));
			case RENT -> session.findElement(By.cssSelector("[data-key=RENT]"));
		};
		tab.click();

		var input = session.findElement(By.cssSelector("[data-testid^=refiner-form] input"));
		input.sendKeys(query.city());

		var option = session.waitForElement(By.cssSelector("[aria-label=Empfehlungen] li"), Duration.ofSeconds(8));
		option.click();
	}

	private void waitForResultsPage(Session session) {
		var title = session.waitForElement(By.cssSelector("[data-testid^=serp-title]"), Duration.ofSeconds(8));
		var text = title.text();

		for (var term : query.terms()) {
			if (!text.contains(term)) {
				throw new IllegalStateException("Page title '" + text + "' does not contain search term '" + term + "'");
			}
		}
	}

	private void addAllApartmentsOnPage(Session session, Set<Link> links) {
		var elements = session.findElements(By.cssSelector("[data-testid^=serp-core] a[data-testid^=card]"));

		for (var element : elements) {
			var title = element.attr("title");

			var path = URI.create(element.attr("href")).getPath();
			var href = index.resolve(path);

			var link = Link.newBuilder()
					.title(title)
					.tag(query.city())
					.tag(query.marketing().name())
					.tag(query.realEstate().name())
					.href(href)
					.seen(Instant.now())
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
