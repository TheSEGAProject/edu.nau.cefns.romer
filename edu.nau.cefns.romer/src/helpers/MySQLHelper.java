package helpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * MySQLHelper
 * 
 * @author jes244
 *
 */
public class MySQLHelper 
{
	// TODO Better doxygen \brief and @return statements.
	// TODO Handle errors in a user friendly environment.
	// TODO What should be private or public?
	// TODO Is get_connection(), etc. necessary?
	
	public MySQLHelper(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private Connection connection;
	private Statement statement;
	private ResultSet result_set;
	private ResultSetMetaData meta_data;
	
	/**
	 * \brief Opens a connection to the specified MySQL server.
	 * 
	 * @param url MySQL server address, example: "jdbc:mysql://localhost:3306/testdb".
	 * @param user User to connect to the server as.
	 * @param password Password for the specified user.
	 */
	public void open_connection(String url, String user, String password)
	{
		// Close the current connection
		close_connection();
		// Reset connection
		connection = null;
		// Try and connect to the new parameters
		try {
				
	        	connection = DriverManager.getConnection(url, user, password);
	        	//return "Successfully connected as "+ user + "@" + url+".";
	        }
	        catch (SQLException e) {
	        	e.printStackTrace();
	        	//return "Failed to connect as "+ user + "@" + url+".";
	        }
	}
	
	/**
	 * \brief Execute an insert.
	 * 
	 * @param insert The insert to execute.
	 */
	public void execute_insert(String insert)
	{
		// Close the current statement
		close_statement();
		// Reset result_set and statement
		statement = null;
		// Try and execute the new statement
		try {
			statement = connection.createStatement();
			statement.executeUpdate(insert);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * \brief Execute a query.
	 * 
	 * @param query The query to execute.
	 */
	public void execute_query(String query)
	{
		// Close the current statement
		close_statement();
		// Reset result_set and statement
		result_set = null;
		statement = null;
		// Try and execute the new statement
		try {
			statement = connection.createStatement();
			result_set = statement.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * \brief Get the current Connection.
	 * 
	 * @return The Connection.
	 */
	public Connection get_connection()
	{
		return connection;
	}
	
	/**
	 * \brief Get the current Statement.
	 * 
	 * @return The Statement.
	 */
	public Statement get_statement()
	{
		return statement;
	}
	
	/**
	 * \brief Get the current ResultSet.
	 * 
	 * @return The ResultSet.
	 */
	public ResultSet get_result_set()
	{
		return result_set;
	}

	/**
	 * \brief Get the ResultSetMetaData of the current ResultSet.
	 * 
	 * @return The ResultSetMetaData of the ResultSet.
	 */
	public ResultSetMetaData get_meta_data()
	{
		// Ensure that there is a Connection, Statement and ResultSet
		if (connection == null || statement == null || result_set == null)
			return null;
		// Try and get the ResultSetMetaData
		try {
			meta_data = result_set.getMetaData();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return meta_data;
	}
	
	/**
	 * \brief Get the results in a readable format from the current ResultSet, if there are any.
	 * 
	 * @return The results from the ResultSet.
	 */
	public String[][] get_results()
	{
		// Ensure that there is a Connection, Statement and ResultSet
		if (connection == null || statement == null || result_set == null)
			return null;
		// Get the ResultSetMetaData of the ResultSet
		get_meta_data();
		int rows = 0;
		int columns = 0;
		// Try and get the number of columns
		try {
			columns = meta_data.getColumnCount();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// Create a "table" for the ResultSet
		String[][] results = new String[rows][columns];
		try {
			while (result_set.next()) {
				// Create a temporary place holder
				String[][] temp_results = results;
				// Update the number of rows
				results = new String[++rows][columns];
				// Copy the data back
				int i = 0;
				for (String[] x : temp_results) {
					int j = 0;
					for (String y : x)
						results[i][j++] = y;
					i++;
				}
				// Add the next row of data to results
				for (i = 0; i < columns; i++)
					results[rows - 1][i] = result_set.getString(i + 1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return results;
	}
	
	/**
	 * \brief Close the current Connection.
	 */
	public void close_connection()
	{
		// Close the current Statement before closing the current Connection
		close_statement();
		// Try and close the current Connection
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * \brief Close the current Statement.
	 */
	private void close_statement()
	{
		// Close the current ResultSet before closing the current Statement
		close_result_set();
		// Try and close the current Statement
		try {
        		if (statement != null)
        			statement.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
	}
	
	/**
	 * \brief Close the current ResultSet.
	 */
	private void close_result_set()
	{
		// Try and close the current ResultSet
		try {
        		if (result_set != null)
        			result_set.close();
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
	}
}
