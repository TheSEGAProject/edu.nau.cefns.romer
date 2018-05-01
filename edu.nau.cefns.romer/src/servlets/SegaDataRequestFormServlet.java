package servlets;


import helpers.ChannelTreeRetriever;
import helpers.DataCollector;
import helpers.KeyValueObject;
import helpers.MySQLHelper;
import helpers.RBNBSourceObject;
import helpers.SegaServerObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import utilities.SegaLogger;



/**
 * This servlet handles the data request from datarequest.jsp and returns 
 * dynamic information that populates the form in order to ensure real-time
 * validation of what data is available
 * 
 * @author jdk85
 *
 */
@WebServlet("/SegaDataRequestFormServlet")
public class SegaDataRequestFormServlet extends HttpServlet implements Servlet {
	
	/** Required by java.io.Serializable */
	private static final long serialVersionUID = 6888655359700667900L;
	/** Log file and location to write to disk */
	private static SegaLogger log = new SegaLogger("/usr/share/tomcat7/segalogs/SegaDataRequestFormServlet.txt");
	/** Absolute URL for the database */
	private static String DB_URL = "jdbc:mysql://wisard-serv1.egr.nau.edu:3306/sega_testing";
	/** Username used when connecting to the database */
	private static String DB_USER = "segawebapp";
	/** Password used when connecting to the database */
	private static String DB_PASSWORD = "qxYz!75";
	/** Helper class used to handle SQL connections
	 * @see MySQLHelper  
	 */
	private static MySQLHelper mysql = new MySQLHelper();
	
	/**
	 * This method takes in a table name parameter and runs
	 * a simple query that returns all the rows from the table
	 * @param tableName the name of the table to fetch results from
	 * @return A string array containing the results of the query
	 */
	protected String[][] getAllResults(String tableName){
			mysql.open_connection(DB_URL,DB_USER,DB_PASSWORD);
			mysql.execute_query("select * from " + tableName);
			String[][] results = mysql.get_results();
			mysql.close_connection();
			return results;
		
	}
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SegaDataRequestFormServlet() {
        super();
		
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
     }
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//These are placeholder strings used when parsing request parameters
		String data_type_selection,server_addr,current_tab,selected_channels,selected_server,redirect,init,starting_date,ending_date,output_style,
			selected_data_interval,time_interval_text,time_interval_select,updating_data,time_interval_style_select;
		String[] include_hidden;
		//Init all the placeholder strings to null
		data_type_selection=server_addr=current_tab=selected_channels=selected_server=redirect=init=starting_date=ending_date=output_style=
				selected_data_interval=time_interval_text=time_interval_select=updating_data=time_interval_style_select=null;
		String[][] servers_from_db = null;
		String[] parsedChannels,parsedServer;
		parsedChannels=parsedServer=null;
		
		try{
			//Reset the error message attribute
			request.removeAttribute("errorMsg");
			//Make sure that the form has provided a redirect attribute
			if((redirect = request.getParameter("redirect")) != null){
				//If datarequest.jsp is being loaded for the first time, automatically fetch and 
				//return the available data styles				
				if((init = request.getParameter("init")) != null){
					if(init.equals("true")){
						List<KeyValueObject> server_type_list = new ArrayList<KeyValueObject>();
						//NOTE: These values are *only* defined here...
						//They should eventually live in a database
						server_type_list.add(new KeyValueObject("rbnb_data","Real-Time Data"));
						server_type_list.add(new KeyValueObject("management_data","SEGA Management Data"));
						server_type_list.add(new KeyValueObject("archived_sega_data","Archived SEGA Data"));
						server_type_list.add(new KeyValueObject("archived_network_data","Archived WiSARDNet Data"));
						request.getSession().setAttribute("initForm", "false");
						request.getSession().setAttribute("server_types", server_type_list);
					}
					
				}
				//Current tab variable is used to keep track of which accordion tab the user should be redirected to
				if((current_tab = request.getParameter("current_tab")) != null){
					request.getSession().setAttribute("currentTab",current_tab);
				}
				
				//This returns all available servers when a user has selected a server type
				//The server objects are fetched from the respective tables and parsed by the jsp 
				//to get the name/ip/type of each server which is uses to build a drop down select menu
				if((data_type_selection = request.getParameter("data_type_selection")) != null){
		        	if(data_type_selection.equals("rbnb_data")){
		        		servers_from_db = getAllResults("rbnb_servers");
		        	}
		        	else if(data_type_selection.equals("management_data")){
		        		servers_from_db = getAllResults("management_servers");
		        	}
		        	else if(data_type_selection.equals("archived_sega_data")){
		        		servers_from_db = getAllResults("archived_sega_servers");
		        	}
		        	else if(data_type_selection.equals("archived_network_data")){
		        		servers_from_db = getAllResults("archived_network_servers");
		        	}
		        	
		        	//If there were no results, return an error message
		        	if(servers_from_db == null){
		        		String[] error_message = {"Error Retreiving Results"};
		        		request.getSession().setAttribute("servernames",error_message);
		        	}
		        	//Otherwise parse the MySQL server results and create server objects to pass along to the jsp
		        	else{
		        		List<SegaServerObject> servers = new ArrayList<SegaServerObject>();
			        	for(int i = 0; i < servers_from_db.length;i++){
			        		//TODO: there's probably a better way to do this
			    			servers.add(new SegaServerObject(servers_from_db[i][0],servers_from_db[i][1],servers_from_db[i][2],servers_from_db[i][3],servers_from_db[i][4]));        			
			    		}   		
			    		request.getSession().setAttribute("serverNames",servers);
			    		request.getSession().setAttribute("selectedDataType", data_type_selection);
			    		
			    		//Set default data_type_selection_name (used for the final request tab)
			    		String data_type_selection_name = "No data type";
			    		if(data_type_selection.equals("rbnb_data"))data_type_selection_name = "Real-Time Data";
			    		else if(data_type_selection.equals("management_data"))data_type_selection_name = "SEGA Management Data";
			    		else if(data_type_selection.equals("archived_sega_data"))data_type_selection_name = "Archived SEGA Data";
			    		else if(data_type_selection.equals("archived_network_data"))data_type_selection_name = "Archived WiSARDNet Data";
			    		request.getSession().setAttribute("selectedDataTypeName", data_type_selection_name);
		        	}
		        	
		        }
				//When the user selects a server, this parameter will get passed along to this servlet
				//The servlet returns the appropriate server information (channels, tables, columns, etc) as
				//well as storing the selected server address for the final request
				else if((server_addr = request.getParameter("data_server_addr")) != null){
					//TODO: check data server type first 					 
					//something needs to go here to process the port number too	
					ChannelTreeRetriever ctr = new ChannelTreeRetriever(server_addr);
					List<RBNBSourceObject> sources;
					if((include_hidden = request.getParameterValues("includeHidden")) != null){
						request.getSession().setAttribute("hiddenCheckboxVal", "true");
						sources = ctr.getSourceObjects();
						
					}
					else{
						request.getSession().setAttribute("hiddenCheckboxVal", "false");
						sources = ctr.getSourceObjectsExcludingHidden();
					}
					
					request.getSession().setAttribute("rbnb_channels", sources);
					request.getSession().setAttribute("selectedServerAddrName", server_addr);
					
					
				}
				if((selected_channels = request.getParameter("selected_channels")) != null){
					if((selected_server = request.getParameter("selected_server"))!=null){
						parsedChannels = selected_channels.split(",");
						parsedServer = selected_server.split(",");
						
						request.getSession().setAttribute("selectedIPAddress", parsedServer[0]);
						request.getSession().setAttribute("selectedServerName", parsedServer[1]);
						
						DataCollector dc = new DataCollector(parsedServer[0]);
					    try {
					      dc.connect("IntervalSink");
					      Date[] startEndTimes = dc.getValidTimeInterval(parsedChannels);
					      dc.disconnect();
					      request.getSession().removeAttribute("selectedStartDate");
					      request.getSession().removeAttribute("selectedEndDate");
					      request.getSession().setAttribute("startInterval",startEndTimes[0]);
					      request.getSession().setAttribute("endInterval",startEndTimes[1]);
					    } catch (Exception e) {
					    	StringWriter errors = new StringWriter();
				        	e.printStackTrace(new PrintWriter(errors));
				    		log.write(errors);
					    }
					}
					request.getSession().setAttribute("selectedChannels",selected_channels);
				}
				if((selected_data_interval = request.getParameter("selected_data_interval")) != null){
					//If date interval
					if(selected_data_interval.equals("0")){
						request.getSession().setAttribute("selectedDataIntervalName", "Date Range");
						if((starting_date = request.getParameter("starting_date")) != null){
							if((ending_date = request.getParameter("ending_date")) != null){
								starting_date = starting_date + " 00:00:00";
								ending_date = ending_date + " 23:59:59";
								SimpleDateFormat sdf  = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
								request.getSession().setAttribute("selectedStartDate", sdf.parseObject(starting_date));
								request.getSession().setAttribute("selectedEndDate", sdf.parseObject(ending_date));
						
							}
							
							
						}
					}
					//if time interval
					else if(selected_data_interval.equals("1")){
						if((time_interval_style_select = request.getParameter("interval_style_select")) != null){
							request.getSession().setAttribute("selectedDataIntervalName", time_interval_style_select);
							if((time_interval_text = request.getParameter("time_interval_text")) != null){
								if((time_interval_select = request.getParameter("time_interval_select")) != null){									
									long duration = Long.parseLong(time_interval_text);
									if(time_interval_select.equals("Minutes")) duration = duration*60;
									else if(time_interval_select.equals("Hours")) duration = duration*60*60;
									else if(time_interval_select.equals("Days")) duration = duration*60*60*24;
									else if(time_interval_select.equals("Weeks")) duration = duration*60*60*24*7;
									else if(time_interval_select.equals("Years")) duration = duration*60*60*24*365;
									request.getSession().setAttribute("selectedTimeIntervalValue", duration);
									request.getSession().setAttribute("selectedTimeIntervalText",time_interval_text);
									request.getSession().setAttribute("selectedTimeIntervalSelect",time_interval_select);
									
								}
							}
						}
					}
					
					
					
					request.getSession().setAttribute("selectedDataInterval", selected_data_interval);
					
				}
				
				if((output_style = request.getParameter("output_style_select")) != null){
					request.getSession().setAttribute("selectedOutputStyle", output_style);
				}
				if((updating_data = request.getParameter("updating_data")) != null){
					request.getSession().setAttribute("updatingData", updating_data);
				}
				
				
				response.sendRedirect(redirect);
			}
		}catch(Exception e){
			StringWriter errors = new StringWriter();
        	e.printStackTrace(new PrintWriter(errors));
    		log.write(errors);
    		if(redirect != null){
    			request.getSession().setAttribute("errorMsg", "An error occured while processing the request. Please try again or notify a system administrator");
    		}
		}
	}
}
