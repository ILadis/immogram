package immogram.repository;

import java.util.Optional;

public interface Repository<I, E> {
	Optional<E> findById(I id);
	void save(E entity);
}
