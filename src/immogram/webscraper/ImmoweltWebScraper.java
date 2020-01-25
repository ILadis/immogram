package immogram.webscraper;

import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import immogram.Apartment;
import immogram.webdriver.By;
import immogram.webdriver.Session;
import immogram.webdriver.WebDriver;
import immogram.webscraper.utils.Timer;
import immogram.webscraper.utils.Watcher;

public class ImmoweltWebScraper implements WebScraper<Apartment> {

	private final String city;

	public ImmoweltWebScraper(String city) {
		this.city = city;
	}

	@Override
	public Collection<Apartment> execute(WebDriver driver) {
		var session = Session.createNew(driver);
		session.navigateTo(URI.create("https://www.immowelt.de/suche/wohnungen/mieten"));

		submitSearch(session, city);
		adjustDistance(session);

		var apartments = new LinkedHashSet<Apartment>();
		do {
			addAllApartmentsOnPage(session, apartments);
		} while (gotoNextPage(session));

		session.close();

		return apartments;
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

		var item = session.findElement(By.cssSelector("input[name='SearchRange'][value='5']"));
		item.click();

		var submit = session.findElement(By.cssSelector("#btnSearch"));
		submit.click();
	}

	private void addAllApartmentsOnPage(Session session, Set<Apartment> apartments) {
		var watcher = Watcher.watch(apartments);
		var unchanged = 0;

		var duration = Duration.ofSeconds(3);

		do {
			if (!Timer.sleepFor(duration)) break;

			addApartmentsCurrentlyOnPage(session, apartments);
			scrollToBottom(session);

			if (watcher.hasChanged()) unchanged = 0;
			else unchanged++;
		} while(unchanged < 3);
	}

	private void addApartmentsCurrentlyOnPage(Session session, Set<Apartment> apartments) {
		var elements = session.findElements(By.cssSelector(".listitem_wrap"));

		for (var element : elements) {
			var description = element.findElement(By.cssSelector("h2"));
			var location = element.findElement(By.cssSelector(".listlocation"));
			//var tags = element.findElements(By.cssSelector(".eq_list li"));
			var price = element.findElement(By.cssSelector(".price_rent strong"));
			var rooms = element.findElement(By.cssSelector(".rooms"));

			var apartment = Apartment.newBuilder()
					.uniqueIdentifier(element.attr("data-oid"))
					.description(description.text())
					.location(location.text())
					.rentalFee(parseInteger(price.text()))
					.numRooms(parseInteger(rooms.text()))
					.build();

			apartments.add(apartment);
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

	private Integer parseInteger(String value) {
		var pattern = Pattern.compile("(\\d+)");
		var matcher = pattern.matcher(value);

		if (matcher.find()) {
			return Integer.parseInt(matcher.group(0));
		}

		return null;
	}
}
