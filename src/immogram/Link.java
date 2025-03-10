package immogram;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Link {

	private Link() { }

	private String title = new String();
	private Set<String> tags = new LinkedHashSet<>();
	private Instant seen = Instant.EPOCH;
	private URI href;

	public String title() {
		return title;
	}

	public Set<String> tags() {
		return Collections.unmodifiableSet(tags);
	}

	public Instant seen() {
		return seen;
	}

	public URI href() {
		return href;
	}

	@Override
	public int hashCode() {
		return href.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Link other) {
			return Objects.equals(title, other.title)
					&& Objects.equals(tags, other.tags)
					&& Objects.equals(seen, other.seen)
					&& Objects.equals(href, other.href);
		}
		return super.equals(object);
	}

	public static Builder newBuilder() {
		var target = new Link();
		return target.new Builder();
	}

	public class Builder {

		private Builder() { }

		public Builder title(String value) {
			title = value;
			return this;
		}

		public Builder tag(String value) {
			tags.add(value);
			return this;
		}

		public Builder tags(Collection<String> values) {
			values.forEach(tags::add);
			return this;
		}

		public Builder seen(Instant value) {
			seen = value;
			return this;
		}

		public Builder href(URI value) {
			href = value;
			return this;
		}

		public Link build() {
			return Link.this;
		}
	}
}
