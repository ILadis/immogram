package immogram.webscraper;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import immogram.Link;
import immogram.webdriver.By;
import immogram.webdriver.Session;
import immogram.webdriver.WebDriver;

public class EbayWebScraper implements WebScraper<Link> {

	private final URI index;
	private final String city;

	public EbayWebScraper(String city) {
		this.city = city;
		this.index = URI.create("https://www.ebay-kleinanzeigen.de");
	}

	@Override
	public Collection<Link> execute(WebDriver driver) {
		try (var session = Session.createNew(driver)) {
			session.navigateTo(index.resolve("/s-wohnung-mieten/c203"));

			submitSearch(session, city);

			var links = new LinkedHashSet<Link>();
			do {
				addAllApartmentsOnPage(session, links);
			} while (gotoNextPage(session));

			return links;
		}
	}

	private void submitSearch(Session session, String city) {
		var input = session.findElement(By.cssSelector("#site-search-area"));
		input.sendKeys(city);

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
