package immogram.repository;

import java.util.Optional;

public interface Repository<I, E> {
	Optional<E> findBy(I id);
	void save(E entity);
	void delete(E entity);
}
