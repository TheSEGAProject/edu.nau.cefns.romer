package servlets;

import helpers.XYDataPointObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.authentication.AttributePrincipal;

import utilities.SegaLogger;

/**
 * This servlet is used to store and fetch saved request queries.
 * 
 * @author jdk85
 * 
 */
@WebServlet("/RequestFormServlet")
public class RequestFormServlet extends HttpServlet implements Servlet {
	/** Required by java.io.Serializable */
	private static final long serialVersionUID = 7199877521119156157L;
	/** Log object */
	private static SegaLogger log = new SegaLogger(
			"/usr/share/tomcat7/segalogs/RequestFormServlet.txt");
	/** Connection variable used to handle MySQL JDBC connection */
	private Connection connection = null;

	/**
	 * Default servlet constructor, calls parent constructor
	 * @see HttpServlet#HttpServlet()
	 */
	public RequestFormServlet() {
		super();
	}
	/**
	 * Connection method handles database connection
	 * @return True if connection is successful, otherwise return false
	 */
	public boolean connect() {
		try {
			//MySQL JDBC init as class
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log.write(errors);
			return false;
		}
		
		//log.write("Successfully Connected to " + db);

		try {
			//Make connection and store in the connection variable
			connection = DriverManager.getConnection(
					"jdbc:mysql://sega.nau.edu:3306/sega", "drupal_admin",
					"pdqxMK207@73");
		} catch (SQLException e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log.write(errors);
			return false;
		}
		return connection != null ? true : false;
	}
	
	/**
	 * Fetch method takes in a form ID number and fetches the form 
	 * from the MySQL database and returns the parameters as an array of 
	 * XYDataPointObjects that is parsed by the web interface to load the
	 * form into the data request page
	 * 
	 * @param form_id
	 * @return Form parameters
	 * @throws SQLException
	 */
	public ArrayList<XYDataPointObject> fetchFormData(int form_id)
			throws SQLException {
		//Create a connection statement
		Statement stm = connection.createStatement();
		//Fetch form parameters by executing SQL statement
		ResultSet resultSet = stm
				.executeQuery("select form_key,form_val from form_data_key_val where fid="
						+ form_id + ";");
		ArrayList<XYDataPointObject> formObjs = new ArrayList<XYDataPointObject>();
		//Parse the result set and store in XYDataPointObject array
		while (resultSet.next()) {
			formObjs.add(new XYDataPointObject(resultSet.getString("form_key"),
					resultSet.getString("form_val")));
		}
		//If there are results available, return the form object, otherwise return null
		if (formObjs.size() >= 1) {
			return formObjs;
		} else
			return null;
	}
	/**
	 * Helper method used to fetch form objects associated by a user's id.
	 * The form object is created by querying the database to fetch all form objects 
	 * for a particular user and is returned as a set of 2 XYDataPointObjects
	 * that includes the id (integer) of the form as well as the name (string).
	 * 
	 * @param uid
	 * @return Form info as XYDataPointObject
	 * @throws SQLException
	 */
	public ArrayList<XYDataPointObject> fetchFormInfo(int uid)
			throws SQLException {
		//Create statement
		Statement stm = connection.createStatement();
		//Fetch the id and name of each form that a user has saved
		ResultSet resultSet = stm
				.executeQuery("select id,form_name from form_data_info where uid="
						+ uid + ";");
		ArrayList<XYDataPointObject> formObjs = new ArrayList<XYDataPointObject>();
		//Iterate over result set and build an array of XYDataPointObjects
		while (resultSet.next()) {
			formObjs.add(new XYDataPointObject(resultSet.getInt("id"),
					resultSet.getString("form_name")));
		}
		//If there were results fetched from MySQL, return the XYDataPointObject array,
			//otherwise return null
		if (formObjs.size() >= 1) {
			return formObjs;
		} else
			return null;
	}
	/**
	 * This method is called from the 'Save data request' button on the 
	 * data request .jsp page. It takes the relevant data request info and
	 * stores it in the MySQL database. The database used two tables: one 
	 * stores each form for all users using a unique form name and the user's
	 * ID and the other table stores the parameters of the form with the associated 
	 * form id.
	 * 
	 * @param uid
	 * @param form_name
	 * @return Integer value representing the form id as it appears in the database
	 * @throws SQLException
	 */
	public int writeFormInfo(int uid, String form_name) throws SQLException {
		//Create statement
		Statement stm = connection.createStatement();
		//Insert a new form name into the form info data table
		String writeFormInfoStr = "insert into form_data_info values (null,"
				+ uid + ",'" + form_name + "');";
		stm.executeUpdate(writeFormInfoStr);
		//Fetches the form id from the recently inserted form
			//This is done because the table auto-increments the form ID
		ResultSet resultSet = stm
				.executeQuery("select id from form_data_info order by id desc limit 1");
		//If the fetch was successful, return the form id
		if (resultSet.next()) {
			return resultSet.getInt("id");
		} else
			return -1;
	}
	/**
	 * This method handles taking request parameters and saving them to the 
	 * SQL form database. While not the most efficient way to insert (row x row)
	 * the data request will never be large enough to really necessitate the use
	 * of a more efficient insert.
	 * 
	 * 
	 * @param form_id The auto-incremented valued obtained from writeFormInfo()
	 * @param keyVals The parameters to be saved to the db
	 * @throws SQLException
	 * @see writeFormInfo()
	 */
	public void writeFormData(int form_id, ArrayList<XYDataPointObject> keyVals)
			throws SQLException {
		//Placeholder string for SQL instert statement
		String writeKeyVal = null;
		//Create the statement
		Statement stm = connection.createStatement();
		//For each parameter key/value pair, insert into the database
		for (XYDataPointObject xy : keyVals) {
			writeKeyVal = "insert into form_data_key_val values (null,"
					+ form_id + ",'" + xy.getX().toString() + "','"
					+ xy.getY().toString() + "');";
			//Execute the query for each parameter
			stm.executeUpdate(writeKeyVal);
		}
	}
	/**
	 * Helper method to fetch the associated uid number from a user name string
	 * 
	 * @param user_name is a String representation of the user's name
	 * @return uid associated with the username
	 * @throws SQLException
	 */
	public int getUid(String user_name) throws SQLException {
		//Create statement
		Statement stm = connection.createStatement();
		//Fetch the username associated with the user name
		ResultSet resultSet = stm
				.executeQuery("select uid from users where name='" + user_name
						+ "';");
		//If results have been successfully fetched, return the value
		if (resultSet.next()) {
			return resultSet.getInt("uid");
		//Otherwise return error code (-1)
		} else
			return -1;
	}
	/**
	 * Helper method used to delete saved forms from the db. The 
	 * method first deletes the key/value parameters from 
	 * form_data_key_val table and then removes the form 
	 * id and name entry from form_info table
	 * 
	 * @param form_id The int id of the form to be deleted
	 * @throws SQLException
	 */
	public void deleteForm(int form_id) throws SQLException {
		//Create statement
		Statement stm = connection.createStatement();
		//Delete all key/value parameter rows associated with the form id
		String delete = "delete from form_data_key_val where fid=" + form_id
				+ ";";
		stm.executeUpdate(delete);
		//Delete the form info entry using the form id
		delete = "delete from form_data_info where id=" + form_id + ";";
		stm.executeUpdate(delete);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

	}

	/**
	 * Servlet doPost handles POST requests passed to the servlet from the web portal.
	 * This servlet handles all the requests made through the user landing page ("segaWeb/user/index.jsp")
	 * 
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		//Placeholder strings used for parsing the request
		String redirect, write_form, fetch_form_info, user_name, form_name, fetch_form_data, form_id_str, action, getAttrs;
		redirect = write_form = fetch_form_info = user_name = form_name = fetch_form_data = form_id_str = action = getAttrs = null;

		try {
			
			//Establish whether or not the user has administrator privileges
			AttributePrincipal principal = (AttributePrincipal) request
					.getUserPrincipal();
			Map<String, Object> attributes = principal.getAttributes();
			String role = (String) attributes.get("role.name");
			if (role != null && role.contains("administrator")) {
				request.getSession().setAttribute("isAdmin", "true");
			} else
				request.getSession().setAttribute("isAdmin", "false");
			//This is automatically called when the page first loads
			if ((getAttrs = request.getParameter("getUserAttributes")) != null) {
				//Set the fetchUserAttributes attribute to false so that the form isn't submitted again after initialization
				request.getSession().setAttribute("fetchUserAttributes",
						"false");
				//If the getUserAttributes param has been set to true, pass back the user attributes to the index page
				//These variables are displayed in the "User Attributes" section of the "My Account" tab on the user landing page
				if (getAttrs.equals("true")
						&& request.getUserPrincipal() != null) {
					if (attributes != null) {
						request.getSession().setAttribute("userAttributes",
								attributes);
					}
				}
			}
			//If a write request has been made to save the form (called from /segaWeb/data/form/datarequest.jsp)
			if ((write_form = request.getParameter("write_form")) != null) {
				//If request to write is true
				if (write_form.equals("true")) {
					//If username is not null (makes sure user is logged in)
					if ((user_name = request.getParameter("user_name")) != null) {
						//If database connection is successful
						if (connect()) {
							//Placehold uid to -1
							int uid = -1;
							//Fetch user id from db
							if ((uid = getUid(user_name)) != -1) {
								//If form name field is not null
								if ((form_name = request
										.getParameter("form_name")) != null) {
									//Write form name to form_info table, returns auto_incremented form id
									int form_id = writeFormInfo(uid, form_name);
									//Init key/value parameter array list
									ArrayList<XYDataPointObject> keyVals = new ArrayList<XYDataPointObject>();
									//Enumerate over all the parameters for the request
									Enumeration<String> params = request
											.getParameterNames();
									while (params.hasMoreElements()) {
										String param = params.nextElement();
										//Check if the parameter is the user name, write form, or form name
										if (!param.equals("user_name")
												&& !param.equals("write_form")
												&& !param.equals("form_name")) {
											//If not, the parameter is a key/val pair for the data request
											String[] vals = request
													.getParameterValues(param);
											//If the parameter is a selected channel list, it means there can be any number
											//of channel names, if it is, split it
											if (param
													.equals("selectedChannels")
													&& vals.length == 1) {
												vals = vals[0].split(",");
											}
											
											//Now the parameter value result is stored in a String array
											//For all parameters except selectedChannels this will only execute once
											for (String str : vals) {
												//Add to keyVals array using param as key and str as vals
												keyVals.add(new XYDataPointObject(
														param, str));
											}
										}
									}
									//If the form_id was successfully retrieved, write the form data to the database
									if (form_id != -1) {
										writeFormData(form_id, keyVals);
									}
								}
							} else
								//Otherwise there was an error with the username/user id fetch
								log.write("Error retreiving user id from database");
							connection.close();
						} else
							//Otherwise there was an error connecting to the database
							log.write("Error connecting to MySQL database");
					}
					//Set the refreshFormInfo so when the user page reloads it will fetch the newly added form and add it to the form list
					request.getSession()
							.setAttribute("refreshFormInfo", "true");
				}
			}

			//This section handles fetching a user's forms and displaying them on the user landing page
			if ((fetch_form_info = request.getParameter("fetch_form_info")) != null) {
				//If the flag has been set to fetch the forms
				if (fetch_form_info.equals("true")) {
					//Make sure the user name parameter has been passed along with the request
					if ((user_name = request.getParameter("user_name")) != null) {
						//Connect to the database
						if (connect()) {
							int uid = -1;
							//Fetch the user id associated with the user name
							//If user id is valid, pass back all the forms for the user
							if ((uid = getUid(user_name)) != -1) {
								request.getSession().setAttribute(
										"formObjects", fetchFormInfo(uid));
							} else
								log.write("Error retreiving user id from database");
							connection.close();
						} else
							log.write("Error Connecting to MySQL database");
					}
					//Reset the refreshFormInfo flag
					request.getSession().setAttribute("refreshFormInfo",
							"false");
				}
			}

			//This section handles fetching a form when a user clicks the info button and loads the form into the datarequest.jsp page
			if ((fetch_form_data = request.getParameter("fetch_form_data")) != null) {
				if (fetch_form_data.equals("true")) {
					//Make sure a form id was passed with the request
					if ((form_id_str = request.getParameter("form_id_str")) != null) {
						//Connect to the database
						if (connect()) {
							//Parse the form ID
							int form_id = Integer.parseInt(form_id_str);
							//Check the action parameter
							if ((action = request.getParameter("action")) != null) {
								//If the play button was clicked, load the form
								if (action.equals("load") || action.equals("play")) {
									//Create array list to store form params from the database
									ArrayList<XYDataPointObject> params = fetchFormData(form_id);
									String channelList = "";
									//For each param set key/value as an attribute
									for (XYDataPointObject xy : params) {
										//If the key/value pair has a single value
										if (!xy.getX().toString()
												.equals("selectedChannels"))
											request.getSession().setAttribute(
													xy.getX().toString(),
													xy.getY().toString());
										//Otherwise attach the channel name to the channel list using a comma separator
										else
											channelList = channelList.concat(xy
													.getY() + ",");
									}
									//Deletes the last ',' from the channel list string
									if (channelList.endsWith(","))
										channelList = channelList.substring(0,
												channelList.lastIndexOf(","));
									//Add the complete channel list as an attribute
									if (channelList != null)
										request.getSession()
												.setAttribute(
														"selectedChannels",
														channelList);	
									if(action.equals("play")){
										request.getRequestDispatcher("/servlets/SegaDataRequestServlet").forward(request, response);
		                                log.write("action is play");
		                                return;
											
									}
								}
								//If the delete button was clicked, remove the form from the database by calling the helper method
								else if (action.equals("delete")) {
									deleteForm(form_id);
									//Reset the refreshFormInfo flag
									request.getSession()
											.setAttribute("refreshFormInfo", "true");
								}
							}
						} else
							log.write("Error connecting to MySQL database");

					}
					
				}

			}
			//This section handles reloading the experiment list menu
			if ((action = request.getParameter("action")) != null) {
				if (action.equals("getExperimentList")) {
					//Set the experiment list returned from SegaExperimentServlet as attribute
					request.getSession().setAttribute("experiments",
							SegaExperimentServlet.getExperiments(attributes));
					//Reset refreshExpMenu flag to false
					request.getSession()
							.setAttribute("refreshExpMenu", "false");
				}
			}
			//Check the redirect and pass it along to the response
			if ((redirect = request.getParameter("redirect")) != null) {
				log.write("Inside redirect on RequestFormServlet: action = " + action + " redirect = " + redirect);
				response.sendRedirect(redirect);
			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log.write(errors);
		}
	}

}
