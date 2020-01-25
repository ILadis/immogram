package immogram;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Apartment {

	private String id, description, location;
	private Integer fee, space, rooms;
	private URI picture;
	private Set<String> tags;

	public String uniqueIdentifier() {
		return id;
	}

	public String description() {
		return description;
	}

	public String location() {
		return location;
	}

	public Integer rentalFee() {
		return fee;
	}

	public Integer spaceInSquareMeters() {
		return space;
	}

	public Integer numRooms() {
		return rooms;
	}

	public URI pictureUri() {
		return picture;
	}

	public Set<String> tags() {
		return Collections.unmodifiableSet(tags);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Apartment) {
			var other = (Apartment) object;
			return other.id.equals(id);
		}
		return super.equals(object);
	}

	public static Builder newBuilder() {
		var target = new Apartment();
		target.tags = new HashSet<>();
		return target.new Builder();
	}

	public class Builder {
		public Builder uniqueIdentifier(String value) {
			id = value;
			return this;
		}

		public Builder description(String value) {
			description = value;
			return this;
		}

		public Builder location(String value) {
			location = value;
			return this;
		}

		public Builder rentalFee(Integer value) {
			fee = value;
			return this;
		}

		public Builder spaceInSquareMeters(Integer value) {
			space = value;
			return this;
		}

		public Builder numRooms(Integer value) {
			rooms = value;
			return this;
		}

		public Builder pictureUri(URI value) {
			picture = value;
			return this;
		}

		public Builder addTag(String value) {
			tags.add(value);
			return this;
		}

		public Apartment build() {
			return Apartment.this;
		}
	}
}
