package immogram.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import immogram.Exceptions;

abstract class ResultSetIterator<E> implements Iterator<E> {

	private ResultSet resultSet;
	private boolean hasNext;

	public ResultSetIterator(ResultSet resultSet) {
		this.resultSet = resultSet;
		this.hasNext = probeNext();
	}

	private boolean probeNext() {
		try {
			return resultSet.next();
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public E next() {
		if (!hasNext) {
			throw new NoSuchElementException();
		}

		try {
			return map(resultSet);
		} catch (SQLException e) {
			return Exceptions.throwUnchecked(e);
		} finally {
			hasNext = probeNext();
		}
	}

	protected abstract E map(ResultSet resultSet) throws SQLException;

}
