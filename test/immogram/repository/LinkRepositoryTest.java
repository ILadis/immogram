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

class LinkRepositoryTest {

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
				.title("test link")
				.href(URI.create("http://localhost:8080/test"))
				.build();

		sut.save(link);

		// act
		var result = sut.findBy(URI.create("ftp://localhost:2323"));

		// assert
		assertTrue(result.isEmpty());
	}

	@Test
	void findBy_shouldReturnLink_whenLinkWithGivenHrefExists() {
		// arrange
		var link = Link.newBuilder()
				.title("test link")
				.seen(Instant.parse("2007-12-03T10:15:30Z"))
				.href(URI.create("http://localhost:8080/test"))
				.build();

		sut.save(link);

		// act
		var result = sut.findBy(URI.create("http://localhost:8080/test")).get();

		// assert
		assertEquals("test link", result.title());
		assertEquals("2007-12-03T10:15:30Z", result.seen().toString());
		assertEquals("http://localhost:8080/test", result.href().toString());
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
					.title("test link")
					.href(URI.create(href))
					.build();

			sut.save(link);
		}

		// act
		var result = sut.findAll();

		// assert
		var iterator = hrefs.iterator();
		while (result.hasNext()) {
			assertEquals(iterator.next(), result.next().href().toString());
		}

		assertFalse(result.hasNext());
		assertFalse(iterator.hasNext());
	}

	@Test
	void findLastSeen_shouldReturnEmptyOption_whenNoLinksExist() {
		// act
		var result = sut.findLastSeen();

		// assert
		assertTrue(result.isEmpty());
	}

	@Test
	void findLastSeen_shouldReturnLastSeenLink_whenMultipleLinksExist() {
		// arrange
		var seens = List.of(
				"2007-12-03T10:15:30Z",
				"2007-12-02T09:10:45Z",
				"2007-12-04T11:20:50Z",
				"2007-12-01T08:05:10Z");

		for (String seen : seens) {
			var link = Link.newBuilder()
					.title(seen)
					.href(URI.create("http://localhost:8080/link?seen=" + seen))
					.seen(Instant.parse(seen))
					.build();

			sut.save(link);
		}

		// act
		var result = sut.findLastSeen();

		// assert
		assertTrue(result.isPresent());
		assertEquals("2007-12-04T11:20:50Z", result.get().seen().toString());
	}
}
