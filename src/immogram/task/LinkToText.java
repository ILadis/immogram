package immogram.task;

import java.util.ArrayList;
import java.util.Collection;

import immogram.Link;

public class LinkToText implements Task<Collection<Link>, Collection<String>> {

	@Override
	public Collection<String> execute(Collection<Link> input) {
		var output = new ArrayList<String>();

		for (var link : input) {
			var text = new StringBuilder();

			text.append('[').append(link.title()).append(']');
			text.append('(').append(link.href()).append(')');

			output.add(text.toString());
		}

		return output;
	}

}
