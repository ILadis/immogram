package immogram.repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import immogram.Apartment;
import immogram.Exceptions;

public class ApartmentRepository implements Repository<String, Apartment> {

	public static ApartmentRepository openNew(Connection conn) {
		var repo = new ApartmentRepository(conn);
		repo.createTables();

		return repo;
	}

	private final Connection conn;

	private ApartmentRepository(Connection conn) {
		this.conn = conn;
	}

	private void createTables() {
		try {
			conn.createStatement().execute(""
					+ "CREATE TABLE IF NOT EXISTS apartments ("
					+ "  id VARCHAR(64),"
					+ "  description VARCHAR(512),"
					+ "  location VARCHAR(256),"
					+ "  fee INT, space INT, rooms INT,"
					+ "  PRIMARY KEY(id)"
					+ ")");
		} catch (SQLException e) {
			Exceptions.throwUnchecked(e);
		}
	}

	@Override
	public Optional<Apartment> findById(String id) {
		try {
			var stmt = conn.prepareStatement(""
					+ "SELECT id, description, location, fee, space, rooms "
					+ "FROM apartments WHERE id = ?");

			stmt.setString(1, id);

			var result = stmt.executeQuery();
			if (!result.next()) {
				return Optional.empty();
			}

			var apartment = Apartment.newBuilder()
					.uniqueIdentifier(result.getString(1))
					.description(result.getString(2))
					.location(result.getString(3))
					.rentalFee(result.getObject(4, Integer.class))
					.spaceInSquareMeters(result.getObject(5, Integer.class))
					.numRooms(result.getObject(6, Integer.class))
					.build();

			return Optional.of(apartment);
		} catch (SQLException e) {
			return Exceptions.throwUnchecked(e);
		}
	}

	@Override
	public void save(Apartment apartment) {
		try {
			var stmt = conn.prepareStatement(""
					+ "MERGE INTO apartments ("
					+ "  id, description, location, fee, space, rooms"
					+ ") VALUES (?, ?, ?, ?, ?, ?)");

			stmt.setString(1, apartment.uniqueIdentifier());
			stmt.setString(2, apartment.description());
			stmt.setString(3, apartment.location());
			stmt.setObject(4, apartment.rentalFee());
			stmt.setObject(5, apartment.spaceInSquareMeters());
			stmt.setObject(6, apartment.numRooms());

			stmt.executeUpdate();
		} catch (SQLException e) {
			Exceptions.throwUnchecked(e);
		}
	}

}
