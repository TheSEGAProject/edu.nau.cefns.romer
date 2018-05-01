package servlets;

import helpers.KeyValueObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import utilities.SegaLogger;


/**
 * Servlet implementation class WisardBuildServlet
 */
@WebServlet("/WisardBuildServlet")
public class WisardBuildServlet extends HttpServlet {
	/** Required by java.io.Serializable */
	private static final long serialVersionUID = 5199097670843989396L;
	/** Log object */
	private static SegaLogger log = new SegaLogger(
			"/usr/share/tomcat7/segalogs/WisardBuildServlet.txt");
	/** Connection variable used to handle MySQL JDBC connection */
	private Connection connection = null;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WisardBuildServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * Connection method handles database connection
	 * @return True if connection is successful, otherwise return false
	 */
	public boolean connect(String url,String user,String password) throws SQLException,ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		//Make connection and store in the connection variable
		connection = DriverManager.getConnection(url,user,password);
		return connection != null ? true : false;
	}
	public boolean disconnect() throws SQLException{
		connection.close();
		return connection.isClosed();
		
	}
	/**
	 * 
	 * @param results
	 * @return
	 * @throws SQLException 
	 */
	public ArrayList<KeyValueObject> getBuildFormInitFromDevServer(ArrayList<KeyValueObject> results) throws SQLException{
		Statement stm = connection.createStatement();
		ResultSet resultSet = stm.executeQuery("select option_id,option_value from wisard_build_form_options;");
		while(resultSet.next()){
			results.add(new KeyValueObject(resultSet.getObject("option_id").toString(),resultSet.getObject("option_value").toString()));
		}
		return results;
	}
	
	/**
	 * Fetches the results for the columns specified from the table parameter
	 * @param columns
	 * @param table_name
	 * @return String array containing results
	 */
	public ArrayList<KeyValueObject> getBuildFormInitInfo(ArrayList<KeyValueObject> results,String col,String table_name) throws SQLException{
		//Create statement
		Statement stm = connection.createStatement();
		//Fetch the id and name of each form that a user has saved
		ResultSet resultSet = stm.executeQuery("select " + col +" from " + table_name + ";");
		//Iterate over result set and build an array of XYDataPointObjects
		while (resultSet.next()) {
			results.add(new KeyValueObject(col,resultSet.getObject(col).toString()));
		}
		resultSet.close();
		return results;
	}
	
	public int fetch_WisardID() throws ClassNotFoundException, SQLException{
		connect("jdbc:mysql://sega.nau.edu:3306/sega_odm_test","egr249","Se6@2014Mysql");
		//Create statement
		Statement stm = connection.createStatement();
		//Fetch the id and name of each form that a user has saved
		ResultSet resultSet = stm.executeQuery("select WisardID from wisards order by WisardID desc limit 1;");
		int wis_id = -1;
		while (resultSet.next()) {
			wis_id = (int)resultSet.getObject("WisardID");
		}
		resultSet.close();				
		disconnect();
		return wis_id + 1;
	}
	public int fetch_SPID() throws ClassNotFoundException, SQLException{
		connect("jdbc:mysql://sega.nau.edu:3306/sega_odm_test","egr249","Se6@2014Mysql");
		//Create statement
		Statement stm = connection.createStatement();
		//Fetch the id and name of each form that a user has saved
		ResultSet resultSet = stm.executeQuery("select SPID from spboards order by SPID desc limit 1;");
		int sp_id = -1;
		while (resultSet.next()) {
			sp_id = (int)resultSet.getObject("SPID");
		}
		resultSet.close();				
		disconnect();
		return sp_id;
	}
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getSession().removeAttribute("error_msg");
		request.getSession().removeAttribute("sp_1_id");
		request.getSession().removeAttribute("sp_1_type");
		request.getSession().removeAttribute("sp_2_id");
		request.getSession().removeAttribute("sp_2_type");
		request.getSession().removeAttribute("sp_3_id");
		request.getSession().removeAttribute("sp_3_type");
		request.getSession().removeAttribute("sp_4_id");
		request.getSession().removeAttribute("sp_4_type");
		
		String redirect,init,garden_site,cp_role,sp_1_type,sp_2_type,sp_3_type,sp_4_type;
		redirect=init=garden_site=cp_role=sp_1_type=sp_2_type=sp_3_type=sp_4_type=null;
		if((init = request.getParameter("init")) != null){
			if(init.equals("true")){
				ArrayList<KeyValueObject> initBuildFormResults = new ArrayList<KeyValueObject>();
				
				try {
					connect("jdbc:mysql://sega.nau.edu:3306/sega_odm_test","egr249","qxYz!75@");
					initBuildFormResults = getBuildFormInitInfo(initBuildFormResults,"SiteName","sites");
					initBuildFormResults = getBuildFormInitInfo(initBuildFormResults,"WisardID","wisards");
					disconnect();
					connect("jdbc:mysql://wisard-serv1.egr.nau.edu:3306/sega_testing","segawebapp","qxYz!75");
					initBuildFormResults = getBuildFormInitFromDevServer(initBuildFormResults);
					disconnect();
					
				} catch (SQLException e) {
					StringWriter errors = new StringWriter();
		        	e.printStackTrace(new PrintWriter(errors));
		    		log.write(errors);
				} catch (ClassNotFoundException e) {
					StringWriter errors = new StringWriter();
		        	e.printStackTrace(new PrintWriter(errors));
		    		log.write(errors);
				} catch(Exception e){
					StringWriter errors = new StringWriter();
		        	e.printStackTrace(new PrintWriter(errors));
		    		log.write(errors);
				}
				request.getSession().setAttribute("init", "false");
				request.getSession().setAttribute("initBuildFormResults", initBuildFormResults);
			}
		}
		if((garden_site = request.getParameter("select_garden_site")) != null){
			try{
				request.getSession().setAttribute("garden_site", garden_site);
				if((cp_role = request.getParameter("select_cp_role")) != null){
					request.getSession().setAttribute("wisard_id", fetch_WisardID());
					request.getSession().setAttribute("cp_role", cp_role);
					int sp_id = -1;														  
					if((sp_1_type = request.getParameter("select_sp_1_type")) != null){
						if(!sp_1_type.equalsIgnoreCase("None")){
							if(sp_id < 0){
								sp_id = fetch_SPID();
							}
							request.getSession().setAttribute("sp_1_type", sp_1_type);
							request.getSession().setAttribute("sp_1_id", sp_id+=1);
						}
					}
					if((sp_2_type = request.getParameter("select_sp_2_type")) != null){
						if(!sp_2_type.equalsIgnoreCase("None")){
							if(sp_id < 0){
								sp_id = fetch_SPID();
							}
							request.getSession().setAttribute("sp_2_type", sp_2_type);
							request.getSession().setAttribute("sp_2_id", sp_id+=1);
						}
					}
					if((sp_3_type = request.getParameter("select_sp_3_type")) != null){
						if(!sp_3_type.equalsIgnoreCase("None")){
							if(sp_id < 0){
								sp_id = fetch_SPID();
							}
							request.getSession().setAttribute("sp_3_type", sp_3_type);
							request.getSession().setAttribute("sp_3_id", sp_id+=1);
						}
					}
					if((sp_4_type = request.getParameter("select_sp_4_type")) != null){
						if(!sp_4_type.equalsIgnoreCase("None")){
							if(sp_id < 0){
								sp_id = fetch_SPID();
							}
							request.getSession().setAttribute("sp_4_type", sp_4_type);
							request.getSession().setAttribute("sp_4_id", sp_id+=1);
						}
					}
							
						
					
				}
			}catch(Exception e){
				request.getSession().setAttribute("error_msg", e.getMessage());
				StringWriter errors = new StringWriter();
	        	e.printStackTrace(new PrintWriter(errors));
	    		log.write(errors);
			}
			
		}
		if((redirect=request.getParameter("redirect")) != null){
			response.sendRedirect(redirect);
		}
	}

}
