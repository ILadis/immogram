package immogram.webscraper;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import immogram.Link;
import immogram.SearchQuery;
import immogram.webdriver.By;
import immogram.webdriver.Session;
import immogram.webdriver.WebDriver;
import immogram.webscraper.utils.Retry;

public class EbayWebScraper implements WebScraper<Collection<Link>> {

	private final URI index;
	private final SearchQuery query;

	public EbayWebScraper(SearchQuery query) {
		this.query = query;
		this.index = URI.create("https://www.kleinanzeigen.de");
	}

	private static String searchPageFor(SearchQuery query) {
		return switch(query.marketing()) {
			case RENT -> switch(query.realEstate()) {
				case APARTMENT -> "/s-wohnung-mieten/c203";
				case HOUSE     -> "/s-haus-mieten/c205";
			};
			case BUY -> switch(query.realEstate()) {
				case APARTMENT -> "/s-wohnung-kaufen/c196";
				case HOUSE     -> "/s-haus-kaufen/c208";
			};
		};
	}

	@Override
	public Collection<Link> execute(WebDriver driver) {
		try (var session = Session.createNew(driver)) {
			session.navigateTo(index.resolve(searchPageFor(query)));

			closeOverlays(session);
			submitSearch(session);

			var links = new LinkedHashSet<Link>();
			do {
				addAllApartmentsOnPage(session, links);
			} while (gotoNextPage(session));

			return links;
		}
	}

	private void closeOverlays(Session session) {
		Retry.suppress(8, () -> {
			var banner = session.findElement(By.cssSelector("#consentBanner"));

			var deny = banner.findElement(By.cssSelector("[data-testid=gdpr-banner-decline]"));
			deny.click();
		});

		Retry.suppress(3, () -> {
			var overlay = session.findElement(By.cssSelector(".login-overlay:not(.is-hidden)"));

			var close = overlay.findElement(By.cssSelector(".overlay-close"));
			close.click();
		});
	}

	private void submitSearch(Session session) {
		var input = session.findElement(By.cssSelector("#site-search-area"));
		input.sendKeys(query.city());

		var submit = session.findElement(By.cssSelector("#site-search-submit"));
		submit.click();
	}

	private void addAllApartmentsOnPage(Session session, Set<Link> links) {
		var elements = session.findElements(By.cssSelector("#srchrslt-adtable .aditem"));

		for (var element : elements) {
			var id = element.attr("data-adid");
			var href = index.resolve("/s-anzeige/" + id);

			var title = element.findElement(By.cssSelector("h2"));

			var link = Link.newBuilder()
					.title(title.text())
					.href(href)
					.seen(Instant.now())
					.build();

			links.add(link);
		}
	}

	private boolean gotoNextPage(Session session) {
		try {
			var next = session.findElement(By.cssSelector("#srchrslt-pagination .pagination-next"));
			next.click();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
