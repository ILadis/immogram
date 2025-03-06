package immogram;

public record SearchQuery(String city, Marketing marketing, RealEstate realEstate) {
	public static enum Marketing {
		RENT, BUY;
	}

	public static enum RealEstate {
		APARTMENT, HOUSE;
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
