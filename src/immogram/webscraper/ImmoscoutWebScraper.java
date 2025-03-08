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

public class ImmoscoutWebScraper implements WebScraper<Collection<Link>> {

	private final URI index;
	private final SearchQuery query;

	public ImmoscoutWebScraper(SearchQuery query) {
		this.query = query;
		this.index = URI.create("https://www.immobilienscout24.de");
	}

	private static String searchPageFor(SearchQuery query) {
		return switch(query.marketing()) {
			case RENT -> switch(query.realEstate()) {
				case APARTMENT -> "/wohnen/mietwohnungen.html";
				case HOUSE     -> "/wohnen/haus-mieten.html";
			};
			case BUY -> switch(query.realEstate()) {
				case APARTMENT -> "/wohnen/eigentumswohnung.html";
				case HOUSE     -> "/wohnen/haus-kaufen.html";
			};
		};
	}

	@Override
	public Collection<Link> execute(WebDriver driver) {
		try (var session = Session.createNew(driver)) {
			session.navigateTo(index.resolve(searchPageFor(query)));

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
		var input = session.findElement(By.cssSelector(".oss-location"));
		input.sendKeys(query.city());

		var option = session.waitForElement(By.cssSelector(".ui-autocomplete .ui-menu-item"), Duration.ofSeconds(8));
		option.click();

		var select = session.findElement(By.cssSelector(".oss-radius"));
		select.click();

		var item = session.findElement(By.cssSelector(".oss-radius-menu [data-value=Km5]"));
		item.click();

		var button = session.findElement(By.cssSelector(".oss-submit-search"));
		button.click();
	}

	private void waitForResultsPage(Session session) {
		var title = session.waitForElement(By.cssSelector("[data-testid=ResultListHeadline]"), Duration.ofSeconds(8));
		var text = title.text();

		for (var term : query.terms()) {
			if (!text.contains(term)) {
				throw new IllegalStateException("Page title '" + text + "' does not contain search term '" + term + "'");
			}
		}
	}

	private void addAllApartmentsOnPage(Session session, Set<Link> links) {
		var elements = session.findElements(By.cssSelector(".result-list-entry__brand-title-container[data-exp-referrer=RESULT_LIST_LISTING]"));

		for (var element : elements) {
			var title = element.text()
					.replaceFirst("^NEU", "")
					.replaceAll("[\\t\\r\\n]", "");

			var path = URI.create(element.attr("href")).getPath();
			var href = index.resolve(path);

			var link = Link.newBuilder()
					.title(title)
					.href(href)
					.seen(Instant.now())
					.build();

			links.add(link);
		}
	}


	private boolean gotoNextPage(Session session) {
		try {
			var next = session.findElement(By.cssSelector("[aria-label^=Nächste]:not([disabled])"));
			next.click();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
