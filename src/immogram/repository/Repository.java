package immogram.repository;

import java.sql.SQLException;

public interface Repository<E> {
	void save(E entity) throws SQLException;
}
