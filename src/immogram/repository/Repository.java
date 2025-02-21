package immogram.repository;

import java.util.Iterator;
import java.util.Optional;

public interface Repository<I, E> {
	Iterator<E> findAll();
	Optional<E> findBy(I id);
	void save(E entity);
	void delete(E entity);
}
