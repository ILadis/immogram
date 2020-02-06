package immogram;

import java.net.URI;

public class Link {

	private Link() { }

	private String title;
	private URI href;

	public String title() {
		return title;
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
		if (object instanceof Link) {
			var other = (Link) object;
			return other.href.equals(href);
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

		public Builder href(URI value) {
			href = value;
			return this;
		}

		public Link build() {
			return Link.this;
		}
	}
}
