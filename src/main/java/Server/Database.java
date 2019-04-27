package Server;

import java.sql.DriverManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * Bridging the gap between h2/SQL and Java
 */
public class Database {

	private String address; // the IP address of the h2 server

	/**
	 * Initialize a new Database object
	 * @param address the IP address of the h2 server
	 * @throws SQLException in case the table could not be created
	 */
	public Database(String address) throws SQLException {

		this.address = address;
		
		// figure out if the database is new and needs to be setup or not
		ArrayList<String> data = null;
		try {
			data = getData(); // a request for a table that does not exist will result in an error, which we then catch
		} catch (SQLException e) {
			// define the table to hold the quotes
			set("CREATE TABLE QUOTES (quote VARCHAR(255) not NULL)");
		}
		System.out.println("Database initialized: " + this + data);

	}

	/**
	 * Initialize a new database object using an in-memory instance of h2
	 * @throws SQLException in case the table could not be created
	 */
	public Database() throws SQLException {
		this("jdbc:h2:mem:test");
	}

	/**
	 * Add a quote to the database
	 * @param quote the quote String to be added
	 * @throws SQLException in case the quote could not be added
	 */
	public void addQuote(String quote) throws SQLException {
		set("INSERT INTO QUOTES VALUES('" + quote + "')");
	}

	/**
	 * Edit a quote in the database (this will edit every occurrence of the quote)
	 * @param oldQuote the quote to be edited
	 * @param newQuote the edited version of the quote
	 * @throws SQLException in case the quote could not be edited
	 */
	public void editQuote(String oldQuote, String newQuote) throws SQLException {
		ArrayList<String> oldDB = getData(); // remember the dataset prior to the mutation
		set("UPDATE QUOTES SET quote='" + newQuote + "' WHERE quote='" + oldQuote + "'"); // mutate the data
		// if the dataset didn't change, the request is not considered successful
		if (oldDB.equals(getData()))
			throw new SQLException();
	}

	/**
	 * Delete a quote in the database (this will delete every occurrence of the quote)
	 * @param quote the quote to be deleted
	 * @throws SQLException in case the quote could not be deleted
	 */
	public void deleteQuote(String quote) throws SQLException {
		ArrayList<String> oldDB = getData(); // remember the dataset prior to the mutation
		set("DELETE FROM QUOTES WHERE quote='" + quote + "'"); // mutate the data
		// if the dataset didn't change, the request is not considered successful
		if (oldDB.equals(getData()))
			throw new SQLException();
	}

	/**
	 * Retrieve the table of quotes from the database as an ArrayList
	 * @return an ArrayList of the quote Strings in the database
	 * @throws SQLException in case the table could not be retrieved
	 */
	public ArrayList<String> getData() throws SQLException {
		// connect to the database and create a new SQL statement
		Connection connection = connectToDB();
		Statement statement = connection.createStatement();
		// retrieve the data as a ResultSet and convert it to an ArrayList
		ResultSet result = statement.executeQuery("SELECT * FROM QUOTES");
		ArrayList<String> output = convertToArrayList(result);
		// close the database connections and return the dataset
		statement.close();
		connection.close();
		return output;
	}

	@Override
	public String toString() {
		// try to retrieve the metadata of the database connection, otherwise just return Object.toString()
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

	/**
	 * Attempt to connect to the database, which might be remote or in-memory
	 * @return the database Connection object
	 * @throws SQLException in case the connection could not be established
	 */
    private Connection connectToDB() throws SQLException {
        return DriverManager.getConnection(address, "sa", "");
	}

	/**
	 * Execute a mutation-query on the database. This should be things like adding a quote, removing or editing existing ones
	 * @param query the SQL String to execute onto the database
	 * @throws SQLException in case the given query could not be executed onto the database
	 */
	private void set(String query) throws SQLException {
		// establish connection and statement object
		Connection connection = connectToDB();
		Statement statement = connection.createStatement();
		// execute the given query and close the connections
		statement.execute(query);
		statement.close();
		connection.close();
	}

	/**
	 * Converts the h2 ResultSet into an ArrayList of Strings
	 * @param rs the ResultSet to convert
	 * @return the converted ArrayList of Strings
	 * @throws SQLException in case the ResultSet could not be successfully converted to an ArrayList of Strings
	 */
	private ArrayList<String> convertToArrayList(ResultSet rs) throws SQLException {
		// define output object
		ArrayList<String> output = new ArrayList<>();
		// append the String version of the data to the output object for each available entry in the ResultSet, then return
		while(rs.next()) output.add(rs.getString("QUOTE"));
		return output;
	}

}