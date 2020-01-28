package immogram.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import immogram.repository.Repository;

public class SaveAndFilter<I, E> implements Task<Collection<E>, Collection<E>> {

	private final Repository<I, E> repo;
	private final Function<E, I> identifier;

	public SaveAndFilter(Repository<I, E> repo, Function<E, I> identifier) {
		this.repo = repo;
		this.identifier = identifier;
	}

	@Override
	public Collection<E> execute(Collection<E> input) {
		var output = new ArrayList<E>();

		for (var entity : input) {
			var id = identifier.apply(entity);
			var existing = repo.findBy(id);

			if (existing.isEmpty()) {
				output.add(entity);
			}

			repo.save(entity);
		}

		return output;
	}

}
