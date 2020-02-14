package immogram.webscraper;

import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import immogram.Link;
import immogram.webdriver.By;
import immogram.webdriver.Element;
import immogram.webdriver.Session;
import immogram.webdriver.WebDriver;

public class ImmonetWebScraper implements WebScraper<Collection<Link>> {

	private final String city;
	private final URI index;

	public ImmonetWebScraper(String city) {
		this.city = city;
		this.index = URI.create("https://www.immonet.de");
	}

	@Override
	public Collection<Link> execute(WebDriver driver) {
		try (var session = Session.createNew(driver)) {
			session.navigateTo(index.resolve("/wohnung-mieten.html"));

			submitSearch(session, city);
			adjustDistance(session);

			var links = new LinkedHashSet<Link>();
			while (addAllApartmentsOnPage(session, links) && gotoNextPage(session));

			return links;
		}
	}

	private void submitSearch(Session session, String city) {
		var input = session.findElement(By.cssSelector("#locationname"));
		input.sendKeys(city);

		var submit = session.findElement(By.cssSelector("#btn-int-hub-pages-suchen"));
		submit.click();
	}

	private void adjustDistance(Session session) {
		var dropdown = session.waitForElement(By.cssSelector("#lnkAroundSearch"), Duration.ofSeconds(5));
		dropdown.click();

		var item = session.findElement(By.cssSelector("li[data-radius='0']"));
		item.click();

		var submit = session.findElement(By.cssSelector("#suchenbutton"));
		submit.click();
	}

	private boolean addAllApartmentsOnPage(Session session, Set<Link> links) {
		var elements = session.findElements(By.cssSelector("#result-list-stage > div"));

		for (var element : elements) {
			var link = findLinkToApartment(element);
			if (link.isEmpty()) {
				return false;
			}

			links.add(link.get());
		}

		return true;
	}

	private Optional<Link> findLinkToApartment(Element element) {
		try {
			var item = element.findElement(By.cssSelector(".item"));

			var id = item.attr("id").substring(10);
			var href = index.resolve("/angebot/" + id);

			var title = item.findElement(By.cssSelector("#lnkToDetails_" + id));

			var link = Link.newBuilder()
					.title(title.text())
					.href(href)
					.build();

			return Optional.of(link);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	private boolean gotoNextPage(Session session) {
		try {
			var next = session.findElement(By.cssSelector("a.pull-right.text-right"));
			next.click();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
