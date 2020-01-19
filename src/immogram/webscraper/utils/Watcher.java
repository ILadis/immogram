package immogram.webscraper.utils;

public class Watcher {

	public static Watcher watch(Object target) {
		var watcher = new Watcher(target);
		watcher.updateHash(target.hashCode());

		return watcher;
	}

	private final Object target;
	private final int[] hash;

	private Watcher(Object target) {
		this.target = target;
		this.hash = new int[1];
	}

	public boolean hasChanged() {
		var hash = target.hashCode();
		var changed = this.hash[0] != hash;

		updateHash(hash);

		return changed;
	}

	private void updateHash(int hash) {
		this.hash[0] = hash;
	}
}
