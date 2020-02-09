package immogram.repository;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
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
					+ "  href VARCHAR(1024),"
					+ "  title VARCHAR(512)"
					+ ")");
			stmt.addBatch(""
					+ "CREATE UNIQUE INDEX IF NOT EXISTS idx_href "
					+ "ON links (href)");
			stmt.executeBatch();
		} catch (SQLException e) {
			Exceptions.throwUnchecked(e);
		}
	}

	@Override
	public Optional<Link> findBy(URI href) {
		try {
			var stmt = conn.prepareStatement(""
					+ "SELECT title "
					+ "FROM links WHERE href = ?");

			stmt.setString(1, href.toString());

			var result = stmt.executeQuery();
			if (!result.next()) {
				return Optional.empty();
			}

			var link = Link.newBuilder()
					.title(result.getString(1))
					.href(href)
					.build();

			return Optional.of(link);
		} catch (SQLException e) {
			return Exceptions.throwUnchecked(e);
		}
	}

	@Override
	public void save(Link link) {
		try {
			var stmt = conn.prepareStatement(""
					+ "MERGE INTO links ("
					+ "  title, href"
					+ ") KEY (href) VALUES ("
					+ "  ?, ?"
					+ ")");

			stmt.setString(1, link.title());
			stmt.setString(2, link.href().toString());

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
