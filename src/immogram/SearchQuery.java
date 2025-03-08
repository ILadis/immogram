package immogram;

import static java.util.function.Predicate.not;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public record SearchQuery(String city, Marketing marketing, RealEstate realEstate) {
	public static enum Marketing {
		RENT, BUY;
	}

	public static enum RealEstate {
		APARTMENT, HOUSE;
	}

	public Set<String> terms() {
		var terms = Arrays.asList(city.split("\\s+"));

		return terms.stream()
				.filter(not(String::isBlank))
				.collect(Collectors.toSet());
	}

	@Override
	public final String toString() {
		return String.format("%s (%s, %s)", city, marketing.name(), realEstate.name());
	}

	public static SearchQuery forRentingAppartment(String city) {
		return new SearchQuery(city, Marketing.RENT, RealEstate.APARTMENT);
	}

	public static SearchQuery forBuyingAppartment(String city) {
		return new SearchQuery(city, Marketing.BUY, RealEstate.APARTMENT);
	}
}
