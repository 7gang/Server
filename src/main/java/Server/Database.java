package Server;

import java.sql.DriverManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class Database {

	private String address;

	public Database(String address) throws ClassNotFoundException, SQLException {

		this.address = address;
		Class.forName("org.h2.Driver");
		
		ArrayList<String> data = null;
		try {
			data = getData();
		} catch (SQLException e) {
			set("CREATE TABLE QUOTES (quote VARCHAR(255) not NULL)");
		}
		System.out.println("Database initialized: " + this + data);

	}

	public Database() throws SQLException, ClassNotFoundException {
		this("jdbc:h2:mem:test");
	}

	public void addQuote(String quote) throws SQLException {
		set("INSERT INTO QUOTES VALUES('" + quote + "')");
	}

	public void editQuote(String oldQuote, String newQuote) throws SQLException {
		ArrayList<String> oldDB = getData();
		set("UPDATE QUOTES SET quote='" + newQuote + "' WHERE quote='" + oldQuote + "'");
		if (oldDB.equals(getData()))
			throw new SQLException();
	}

	public void deleteQuote(String quote) throws SQLException {
		ArrayList<String> oldDB = getData();
		set("DELETE FROM QUOTES WHERE quote='" + quote + "'");
		if (oldDB.equals(getData()))
			throw new SQLException();
	}

	public ArrayList<String> getData() throws SQLException {
		Connection connection = connectToDB();
		Statement statement = connection.createStatement();
		ResultSet result = statement.executeQuery("SELECT * FROM QUOTES");
		ArrayList<String> output = convertToArrayList(result);
		statement.close();
		connection.close();
		return output;
	}

	@Override
	public String toString() {
		try {
			Connection connection = connectToDB();
			String output = connection.getMetaData().toString();
			connection.close();
			return output;
		} catch (SQLException e) {
			e.printStackTrace();
			return super.toString();
		}
	}

    private Connection connectToDB() throws SQLException {
        return DriverManager.getConnection(address, "sa", "");
	}

	private void set(String query) throws SQLException {
		Connection connection = connectToDB();
		Statement statement = connection.createStatement();
		statement.execute(query);
		statement.close();
		connection.close();
	}

	private ArrayList<String> convertToArrayList(ResultSet rs) throws SQLException {
		ArrayList<String> output = new ArrayList<>();
		while(rs.next()) output.add(rs.getString("QUOTE"));
		return output;
	}

}