package immogram.webdriver.http;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import immogram.webdriver.Element;
import immogram.webdriver.Session;
import immogram.webdriver.ShadowRoot;

class JsonReaders {

	static String forErrorMessage(InputStream in) {
		var reader = Json.createReader(in);

		return reader.readObject()
				.getJsonObject("value")
				.getString("message");
	}

	static Session.Id forSessionId(InputStream in) {
		var reader = Json.createReader(in);

		var value = reader.readObject()
				.getJsonObject("value");

		return toSessionId(value);
	}

	private static Session.Id toSessionId(JsonObject object) {
		var value = object.getString("sessionId");
		return new Session.Id(value);
	}

	static Element.Id forElementId(InputStream in) {
		var reader = Json.createReader(in);

		var value = reader.readObject()
				.getJsonObject("value");

		return toElementId(value);
	}

	private static Element.Id toElementId(JsonObject object) {
		var key = object.keySet().iterator().next();
		var value = object.getString(key);

		return new Element.Id(value);
	}

	static List<Element.Id> forElementIds(InputStream in) {
		var reader = Json.createReader(in);

		var values = reader.readObject()
				.getJsonArray("value");

		return values.getValuesAs(JsonReaders::toElementId);
	}

	static ShadowRoot.Id forShadowRootId(InputStream in) {
		var reader = Json.createReader(in);

		var value = reader.readObject()
				.getJsonObject("value");

		return toShadowRootId(value);
	}

	private static ShadowRoot.Id toShadowRootId(JsonObject object) {
		var key = object.keySet().iterator().next();
		var value = object.getString(key);

		return new ShadowRoot.Id(value);
	}

	static String forTextValue(InputStream in) {
		var reader = Json.createReader(in);

		var value = reader.readObject()
				.getString("value");

		return value;
	}

	static ByteBuffer forBase64Value(InputStream in) {
		var reader = Json.createReader(in);
		var decoder = Base64.getDecoder();

		var value = reader.readObject()
				.getString("value");

		var raw = decoder.decode(value);

		return ByteBuffer.wrap(raw);
	}

}
