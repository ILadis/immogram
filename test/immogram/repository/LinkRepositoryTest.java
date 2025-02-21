package immogram.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import immogram.Link;

public class LinkRepositoryTest {

	private LinkRepository sut;
	private Connection conn;

	@BeforeEach
	void setUp() throws SQLException {
		conn = DriverManager.getConnection("jdbc:h2:mem:test");
		sut = LinkRepository.openNew(conn);
	}

	@AfterEach
	void tearDown() throws SQLException {
		conn.close();
	}

	@Test
	void findBy_shouldReturnEmptyOptional_whenLinkWithGivenHrefDoesNotExist() {
		// arrange
		var link = Link.newBuilder()
				.title("localhost - 8080")
				.href(URI.create("http://localhost:8080/test"))
				.build();

		sut.save(link);

		// act
		var result1 = sut.findBy(URI.create("ftp://localhost:2323"));
		var result2 = sut.findBy(URI.create("http://localhost:8080/test"));

		// assert
		assertTrue(result1.isEmpty());
		assertTrue(result2.isPresent());
	}

	@Test
	void findBy_shouldMapPublishedTimestampCorrectly() {
		// arrange
		var link = Link.newBuilder()
				.title("localhost - 8080")
				.seen(Instant.parse("2007-12-03T10:15:30Z"))
				.href(URI.create("http://localhost:8080/test"))
				.build();

		sut.save(link);

		// act
		var result = sut.findBy(URI.create("http://localhost:8080/test")).get();

		// assert
		assertEquals("2007-12-03T10:15:30Z", result.seen().toString());
	}

	@Test
	void findAll_shouldReturnEmptyIterator_whenNoLinksExist() {
		// act
		var result = sut.findAll();

		// assert
		assertFalse(result.hasNext());
	}

	@Test
	void findAll_shouldReturnAllAvailableLinks() {
		// arrange
		var hrefs = List.of(
				"http://localhost:8080/test",
				"ftp://localhost:2323",
				"ssh://root@localhost:2222",
				"dav://localhost:442");

		for (String href : hrefs) {
			var link = Link.newBuilder()
					.title(href)
					.href(URI.create(href))
					.build();

			sut.save(link);
		}

		// act
		var result1 = sut.findAll();
		var result2 = hrefs.iterator();

		// assert
		while (result1.hasNext()) {
			assertEquals(result2.next(), result1.next().href().toString());
		}
		assertFalse(result1.hasNext());
		assertFalse(result2.hasNext());
	}
}
