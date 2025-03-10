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
					+ "  tags VARCHAR(256) ARRAY[10],"
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
					+ "SELECT title, tags, seen, href "
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
					+ "SELECT title, tags, seen, href "
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
					+ "SELECT title, tags, seen, href "
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

	public Iterator<Link> findByTag(String tag) {
		try {
			var stmt = conn.prepareStatement(""
					+ "SELECT title, tags, seen, href "
					+ "FROM links WHERE ARRAY_CONTAINS(tags, ?) "
					+ "ORDER BY seen DESC");

			var tags = conn.createArrayOf("VARCHAR", new Object[] { tag });
			stmt.setArray(1, tags);

			return new ResultSetIterator<Link>(stmt.executeQuery()) {
				protected @Override Link map(ResultSet result) throws SQLException {
					return LinkRepository.this.map(result);
				}
			};
		} catch (SQLException e) {
			return Exceptions.throwUnchecked(e);
		}
	}

	private Link map(ResultSet result) throws SQLException {
		var title = result.getString(1);
		var tags = (Object[]) result.getArray(2).getArray();
		var seen = result.getTimestamp(3).toInstant();
		var href = URI.create(result.getString(4));

		var link = Link.newBuilder()
				.title(title)
				.seen(seen)
				.href(href);

		for (Object tag : tags) {
			link.tag(tag.toString());
		}

		return link.build();
	}

	@Override
	public void save(Link link) {
		try {
			var stmt = conn.prepareStatement(""
					+ "INSERT INTO links ("
					+ "  title, tags, seen, href"
					+ ") VALUES ("
					+ "  ?, ?, ?, ?"
					+ ")");

			stmt.setString(1, link.title());
			stmt.setTimestamp(3, Timestamp.from(link.seen()));
			stmt.setString(4, link.href().toString());

			var tags = conn.createArrayOf("VARCHAR", link.tags().toArray());
			stmt.setArray(2, tags);

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
