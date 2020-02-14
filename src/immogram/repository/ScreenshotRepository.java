package immogram.repository;

import java.net.URI;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import immogram.Exceptions;
import immogram.Screenshot;

public class ScreenshotRepository implements Repository<URI, Screenshot> {

	public static ScreenshotRepository openNew(Connection conn) {
		var repo = new ScreenshotRepository(conn);
		repo.createTables();

		return repo;
	}

	private final Connection conn;

	private ScreenshotRepository(Connection conn) {
		this.conn = conn;
	}

	private void createTables() {
		try {
			var stmt = conn.createStatement();
			stmt.addBatch(""
					+ "CREATE TABLE IF NOT EXISTS screenshots ("
					+ "  href VARCHAR(1024),"
					+ "  bitmap BINARY(2097152)"
					+ ")");
			stmt.addBatch(""
					+ "CREATE UNIQUE INDEX IF NOT EXISTS idx_screenshots "
					+ "ON screenshots (href)");
			stmt.executeBatch();
		} catch (SQLException e) {
			Exceptions.throwUnchecked(e);
		}
	}

	@Override
	public Optional<Screenshot> findBy(URI href) {
		try {
			var stmt = conn.prepareStatement(""
					+ "SELECT bitmap "
					+ "FROM screenshots WHERE href = ?");

			stmt.setString(1, href.toString());

			var result = stmt.executeQuery();
			if (!result.next()) {
				return Optional.empty();
			}

			var bitmap = ByteBuffer.wrap(result.getBytes(1));

			var screenshot = Screenshot.newBuilder()
					.href(href)
					.bitmap(bitmap)
					.build();

			return Optional.of(screenshot);
		} catch (SQLException e) {
			return Exceptions.throwUnchecked(e);
		}
	}

	@Override
	public void save(Screenshot screenshot) {
		try {
			var stmt = conn.prepareStatement(""
					+ "MERGE INTO screenshots ("
					+ "  href, bitmap"
					+ ") KEY (href) VALUES ("
					+ "  ?, ?"
					+ ")");

			stmt.setString(1, screenshot.href().toString());
			stmt.setBytes(2, screenshot.bitmap().array());

			stmt.executeUpdate();
		} catch (SQLException e) {
			Exceptions.throwUnchecked(e);
		}
	}

	@Override
	public void delete(Screenshot screenshot) {
		try {
			var stmt = conn.prepareStatement(""
					+ "DELETE FROM screenshots WHERE "
					+ "href = ?");

			stmt.setString(1, screenshot.href().toString());

			stmt.executeUpdate();
		} catch (SQLException e) {
			Exceptions.throwUnchecked(e);
		}
	}

}
