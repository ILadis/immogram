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
import immogram.webscraper.utils.Timer;
import immogram.webscraper.utils.Watcher;

public class ImmoweltWebScraper implements WebScraper<Link> {

	private final URI index;
	private final String city;

	public ImmoweltWebScraper(String city) {
		this.city = city;
		this.index = URI.create("https://www.immowelt.de");
	}

	@Override
	public Collection<Link> execute(WebDriver driver) {
		try (var session = Session.createNew(driver)) {
			session.navigateTo(index.resolve("/suche/wohnungen/mieten"));

			submitSearch(session, city);
			adjustDistance(session);

			var links = new LinkedHashSet<Link>();
			do {
				addAllApartmentsOnPage(session, links);
			} while (gotoNextPage(session));

			return links;
		}
	}

	private void submitSearch(Session session, String city) {
		var input = session.findElement(By.cssSelector("#tbLocationInput"));
		input.sendKeys(city);

		var submit = session.findElement(By.cssSelector("#btnSearchSubmit"));
		submit.click();
	}

	private void adjustDistance(Session session) {
		var dropdown = session.waitForElement(By.cssSelector(".umkreis"), Duration.ofSeconds(5));
		dropdown.click();

		var item = session.findElement(By.cssSelector("input[name='SearchRange'][value='0']"));
		item.click();

		var submit = session.findElement(By.cssSelector("#btnSearch"));
		submit.click();
	}

	private void addAllApartmentsOnPage(Session session, Set<Link> links) {
		var watcher = Watcher.watch(links);
		var unchanged = 0;

		var duration = Duration.ofSeconds(3);

		do {
			if (!Timer.sleepFor(duration)) break;

			addApartmentsCurrentlyOnPage(session, links);
			scrollToBottom(session);

			if (watcher.hasChanged()) unchanged = 0;
			else unchanged++;
		} while(unchanged < 3);
	}

	private void addApartmentsCurrentlyOnPage(Session session, Set<Link> links) {
		var elements = session.findElements(By.cssSelector(".listitem_wrap"));

		for (var element : elements) {
			var id = element.attr("data-oid");
			var href = index.resolve("/expose/" + id);

			var title = element.findElement(By.cssSelector("h2"));

			var link = Link.newBuilder()
					.title(title.text())
					.href(href)
					.build();

			links.add(link);
		}
	}

	private void scrollToBottom(Session session) {
		session.executeScript(""
				+ "var elements = Array.from(document.querySelectorAll('.listcontent h2'));"
				+ "if (elements.length) {"
				+ "  elements.pop().scrollIntoView({behavior: 'smooth', block: 'end'});"
				+ "}");
	}

	private boolean gotoNextPage(Session session) {
		try {
			var next = session.findElement(By.cssSelector("#nlbPlus"));
			next.click();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
