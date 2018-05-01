package helpers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import utilities.SegaLogger;

import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

public class DataValidationHelper {
	private static int port = 3306;
	private int interval;
	private String rbnb_server, sql_server, sql_db, sql_user,sql_password,site_name;
	private Sink snk;
	private Connection connection;
	private DataFetchHelper fetcher;
	private ArrayList<RBNBChannelObject> data;
	private SegaLogger log = new SegaLogger("/usr/share/tomcat7/segalogs/DataValidationHelper_Log.txt");
	
	public DataValidationHelper(){
		//TODO: Generic empty constructor
	}
	public DataValidationHelper(int interval,String rbnb_server, String site_name, String sql_server, String sql_db, String sql_user, String sql_password){
		this.interval = interval;
		this.rbnb_server = rbnb_server;
		this.site_name = site_name;
		this.sql_server = sql_server;
		this.sql_db = sql_db;
		this.sql_user = sql_user;
		this.sql_password = sql_password;
	}
	public String run() throws Exception{
		String result = "";
		try{
			if(connect_to_rbnb(rbnb_server,"DataValidationSink") && connect_to_sql(sql_server,sql_db,sql_user,sql_password)){
				log.write("Connected to RBNB server " + rbnb_server);
				log.write("Connected to MySQL server " + sql_server);
				result = result.concat("Connected to RBNB server " + rbnb_server + "<br/>Connected to MySQL server " + sql_server);
				ChannelTreeRetriever ctr = new ChannelTreeRetriever(rbnb_server);
				String[] channels = ctr.getChannelTree();
				List<String> channelList = new ArrayList<String>();
				for(String s : channels){
					if(s.contains(site_name)){
						if(!s.contains("_Log") && !s.contains("_raw") && !s.contains("wisard")){
							channelList.add(s);
							//result = result.concat("<br/>"+s);
						}
					}
				}
				result = result.concat(validateData(channelList));
				
			}
			else
				log.write("Error connecting to RBNB or MySQL");
			
		} catch(SAPIException e){
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log.write(errors);
		} catch (ClassNotFoundException e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log.write(errors);
		} catch (SQLException e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log.write(errors);
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log.write(errors);
		}
		return result;
	}
	public String validateData(List<String> channels) throws SQLException{
		String[] channelNameSplit;
		String table_name = "";
		String time_stamp_column = "TmStamp";
		String column_name;
		String table_name_complete;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Statement stm;
		ResultSet resultSet;
		List<SampleTimestampPackage> samples;
		long db_timestamp;
		float db_value;
		String result = "";
		String[] splitString;
		String query;
		//EX: arboretum_channelized/cr1000/Daily/AirTC_Avg
		for(String s : channels){
			splitString = s.split("/");
			table_name = site_name + "_" + splitString[2].toLowerCase();
			if(splitString.length == 4){
				result = result.concat("<br/>" + table_name);
				query = "select " + time_stamp_column + "," + splitString[3] + " from " + table_name + " order by " + time_stamp_column + " desc limit 1;";
				stm = connection.createStatement();
				resultSet = stm.executeQuery(query);
				while(resultSet.next()){					
					result = result.concat("\t" + new Date(resultSet.getDate(time_stamp_column).getTime()).toString() + "\t" + resultSet.getFloat(splitString[3]));
				}
				stm.close();
			}
		}
		
		
		return result;
		
	}
	public boolean connect_to_rbnb(String rbnb_server, String sink_name) throws SAPIException{
		snk = new Sink();
		snk.OpenRBNBConnection(rbnb_server, sink_name);
		return snk.VerifyConnection();		
	}
	
	public boolean connect_to_sql(String sql_server, String sql_db, String sql_user, String sql_password) throws ClassNotFoundException, SQLException{

		//MySQL JDBC init as class
		Class.forName("com.mysql.jdbc.Driver");		
		
		//Make connection and store in the connection variable
		connection = DriverManager.getConnection("jdbc:mysql://"+sql_server+":"+port+ "/"+sql_db, sql_user,sql_password);		
		
		return connection != null ? true : false;
	}

}
