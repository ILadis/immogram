package immogram;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Objects;

public class Screenshot {

	private Screenshot() { }

	private ByteBuffer bitmap;
	private URI url;

	public ByteBuffer bitmap() {
		return bitmap;
	}

	public URI url() {
		return url;
	}

	@Override
	public int hashCode() {
		return Objects.hash(url);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Screenshot other) {
			return Objects.equals(url, other.url);
		}
		return super.equals(object);
	}

	public static Builder newBuilder() {
		var target = new Screenshot();
		return target.new Builder();
	}

	public class Builder {

		private Builder() { }

		public Builder url(URI value) {
			url = value;
			return this;
		}

		public Builder bitmap(ByteBuffer value) {
			bitmap = value;
			return this;
		}

		public Screenshot build() {
			return Screenshot.this;
		}
	}
}
