package immogram.repository;

import java.sql.Connection;
import java.sql.SQLException;

import immogram.Apartment;

public class ApartmentRepository implements Repository<Apartment> {

	public static ApartmentRepository openNew(Connection conn) throws SQLException {
		var repo = new ApartmentRepository(conn);
		repo.createTables();

		return repo;
	}

	private final Connection conn;

	private ApartmentRepository(Connection conn) {
		this.conn = conn;
	}

	private void createTables() throws SQLException {
		conn.createStatement().execute(""
				+ "CREATE TABLE IF NOT EXISTS apartments ("
				+ "  id VARCHAR(64),"
				+ "  description VARCHAR(512),"
				+ "  PRIMARY KEY(id)"
				+ ")");
	}

	@Override
	public void save(Apartment apartment) throws SQLException {
		var stmt = conn.prepareStatement(""
				+ "INSERT INTO apartments (id, description) VALUES (?, ?)");

		stmt.setString(1, apartment.uniqueIdentifier());
		stmt.setString(2, apartment.description());

		stmt.executeUpdate();
	}

}
