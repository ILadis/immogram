package immogram;

import java.net.URI;
import java.nio.ByteBuffer;

public class Screenshot {

	private Screenshot() { }

	private URI href;
	private ByteBuffer bitmap;

	public URI href() {
		return href;
	}

	public ByteBuffer bitmap() {
		return bitmap;
	}

	@Override
	public int hashCode() {
		return href.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Screenshot) {
			var other = (Screenshot) object;
			return other.href.equals(href);
		}
		return super.equals(object);
	}

	public static Builder newBuilder() {
		var target = new Screenshot();
		return target.new Builder();
	}

	public class Builder {

		private Builder() { }

		public Builder href(URI value) {
			href = value;
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
