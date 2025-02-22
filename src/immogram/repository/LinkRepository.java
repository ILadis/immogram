package immogram.repository;

import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Optional;

import immogram.Exceptions;
import immogram.Link;

public class LinkRepository implements Repository<URI, Link> {

	public static LinkRepository openNew(Connection conn) {
		var repo = new LinkRepository(conn);
		repo.createTables();

		return repo;
	}

	private final Connection conn;

	private LinkRepository(Connection conn) {
		this.conn = conn;
	}

	private void createTables() {
		try {
			var stmt = conn.createStatement();
			stmt.addBatch(""
					+ "CREATE TABLE IF NOT EXISTS links ("
					+ "  title VARCHAR(2048),"
					+ "  seen TIMESTAMP WITH TIME ZONE,"
					+ "  href VARCHAR(1024)"
					+ ")");
			stmt.addBatch(""
					+ "CREATE UNIQUE INDEX IF NOT EXISTS idx_links "
					+ "ON links (href)");
			stmt.executeBatch();
		} catch (SQLException e) {
			Exceptions.throwUnchecked(e);
		}
	}

	@Override
	public Iterator<Link> findAll() {
		try {
			var stmt = conn.prepareStatement(""
					+ "SELECT title, seen, href "
					+ "FROM links ORDER BY seen DESC");

			return new ResultSetIterator<Link>(stmt.executeQuery()) {
				protected @Override Link map(ResultSet result) throws SQLException {
					return LinkRepository.this.map(result);
				}
			};
		} catch (SQLException e) {
			return Exceptions.throwUnchecked(e);
		}
	}

	@Override
	public Optional<Link> findBy(URI href) {
		try {
			var stmt = conn.prepareStatement(""
					+ "SELECT title, seen, href "
					+ "FROM links WHERE href = ?");

			stmt.setString(1, href.toString());

			var result = stmt.executeQuery();
			if (!result.next()) {
				return Optional.empty();
			}

			var link = map(result);
			return Optional.of(link);
		} catch (SQLException e) {
			return Exceptions.throwUnchecked(e);
		}
	}

	public Optional<Link> findLastSeen() {
		try {
			var stmt = conn.prepareStatement(""
					+ "SELECT title, seen, href "
					+ "FROM links ORDER BY seen DESC "
					+ "LIMIT 1");

			var result = stmt.executeQuery();
			if (!result.next()) {
				return Optional.empty();
			}

			var link = map(result);
			return Optional.of(link);
		} catch (SQLException e) {
			return Exceptions.throwUnchecked(e);
		}
	}

	private Link map(ResultSet result) throws SQLException {
		var title = result.getString(1);
		var seen = result.getTimestamp(2).toInstant();
		var href = URI.create(result.getString(3));

		return Link.newBuilder()
				.title(title)
				.seen(seen)
				.href(href)
				.build();
	}

	@Override
	public void save(Link link) {
		try {
			var stmt = conn.prepareStatement(""
					+ "INSERT INTO links ("
					+ "  title, seen, href"
					+ ") VALUES ("
					+ "  ?, ?, ?"
					+ ")");

			stmt.setString(1, link.title());
			stmt.setTimestamp(2, Timestamp.from(link.seen()));
			stmt.setString(3, link.href().toString());

			stmt.executeUpdate();
		} catch (SQLException e) {
			Exceptions.throwUnchecked(e);
		}
	}

	@Override
	public void delete(Link link) {
		try {
			var stmt = conn.prepareStatement(""
					+ "DELETE FROM links WHERE "
					+ "href = ?");

			stmt.setString(1, link.href().toString());

			stmt.executeUpdate();
		} catch (SQLException e) {
			Exceptions.throwUnchecked(e);
		}
	}

}
