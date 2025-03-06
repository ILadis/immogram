package immogram.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import immogram.Screenshot;

class ScreenshotRepositoryTest {

	private ScreenshotRepository sut;
	private Connection conn;

	@BeforeEach
	void setUp() throws SQLException {
		conn = DriverManager.getConnection("jdbc:h2:mem:test");
		sut = ScreenshotRepository.openNew(conn);
	}

	@AfterEach
	void tearDown() throws SQLException {
		conn.close();
	}

	@Test
	void save_shouldSupportScreenshotsUpto6Megabytes() {
		// arrange
		var buffer = ByteBuffer.allocate(6291456);
		var screenshot = Screenshot.newBuilder()
				.url(URI.create("http://localhost:8080/test"))
				.bitmap(buffer)
				.build();

		// act & assert
		assertDoesNotThrow(() -> sut.save(screenshot));
	}

	@Test
	void save_shouldThrowException_whenScreenshotIsLargerThan6Megabytes() {
		// arrange
		var buffer = ByteBuffer.allocate(6291456 + 1);
		var screenshot = Screenshot.newBuilder()
				.url(URI.create("http://localhost:8080/test"))
				.bitmap(buffer)
				.build();

		// act & assert
		var exception = assertThrows(SQLException.class, () -> sut.save(screenshot));
		assertTrue(exception.getMessage().contains("too long"));
	}
}
