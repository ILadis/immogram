package immogram.repository;

import java.net.URI;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
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
					+ "  bitmap BINARY(6291456),"
					+ "  url VARCHAR(1024)"
					+ ")");
			stmt.addBatch(""
					+ "CREATE UNIQUE INDEX IF NOT EXISTS idx_screenshot_urls "
					+ "ON screenshots (url)");
			stmt.executeBatch();
		} catch (SQLException e) {
			Exceptions.throwUnchecked(e);
		}
	}

	@Override
	public Iterator<Screenshot> findAll() {
		try {
			var stmt = conn.prepareStatement(""
					+ "SELECT url, bitmap "
					+ "FROM screenshots");

			return new ResultSetIterator<Screenshot>(stmt.executeQuery()) {
				protected @Override Screenshot map(ResultSet result) throws SQLException {
					return ScreenshotRepository.this.map(result);
				}
			};
		} catch (SQLException e) {
			return Exceptions.throwUnchecked(e);
		}
	}

	@Override
	public Optional<Screenshot> findBy(URI url) {
		try {
			var stmt = conn.prepareStatement(""
					+ "SELECT url, bitmap "
					+ "FROM screenshots WHERE url = ?");

			stmt.setString(1, url.toString());

			var result = stmt.executeQuery();
			if (!result.next()) {
				return Optional.empty();
			}

			var screenshot = map(result);
			return Optional.of(screenshot);
		} catch (SQLException e) {
			return Exceptions.throwUnchecked(e);
		}
	}

	private Screenshot map(ResultSet result) throws SQLException {
		var url = URI.create(result.getString(1));
		var bitmap = ByteBuffer.wrap(result.getBytes(2));

		return Screenshot.newBuilder()
				.url(url)
				.bitmap(bitmap)
				.build();
	}

	@Override
	public void save(Screenshot screenshot) {
		try {
			var stmt = conn.prepareStatement(""
					+ "MERGE INTO screenshots ("
					+ "  url, bitmap"
					+ ") KEY (url) VALUES ("
					+ "  ?, ?"
					+ ")");

			stmt.setString(1, screenshot.url().toString());
			stmt.setBytes(2, screenshot.bitmap().array());

			stmt.executeUpdate();
		} catch (SQLException e) {
			Exceptions.throwUnchecked(e);
		}
	}

	@Override
	public void delete(Screenshot screenshot) {
		deleteBy(screenshot.url());
	}

	public void deleteBy(URI url) {
		try {
			var stmt = conn.prepareStatement(""
					+ "DELETE FROM screenshots WHERE "
					+ "url = ?");

			stmt.setString(1, url.toString());

			stmt.executeUpdate();
		} catch (SQLException e) {
			Exceptions.throwUnchecked(e);
		}
	}

}
