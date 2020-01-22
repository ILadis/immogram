package immogram.webdriver.http;

import java.io.InputStream;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import immogram.webdriver.Element;
import immogram.webdriver.Session;

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

	static String forValue(InputStream in) {
		var reader = Json.createReader(in);

		var value = reader.readObject()
				.getString("value");

		return value;
	}

}
