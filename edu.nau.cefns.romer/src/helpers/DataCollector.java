package helpers;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.TimeZone;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

/**
 * This is a helper class for SegaDataServlet that is used to connect to RBNB.
 * Requests for data are made through SegaDataServlet, and a temporary instance of this class
 * is created that will handle RBNB connections. The data is fetched from the appropriate channel
 * for the specified time duration and is returned to the SegaDataServlet as a String. The response
 * string is preformatted to to be interpreted by the calling Javascript function. This allows
 * Javascript to easily parse the response string into Javascript arrays. However, if the channel is a
 * log file, the response string will be an HTML formatted version of the newest log entry\n\n
 * <code>
 *  Ex:\n
 *  Say there are two available data points in RBNB.\n
 *  They are fetched in an array and stored as follows:\n
 *  timestamp1 | data1\n
 *  timestamp2 | data2\n
 *  
 *  They will be formatted for Javascript as follows:\n
 *  timestamp 1 # data 1, timestamp 2 # data 2\n
 *  
 *  Whenever the Javascript parses the values, it first creates\n
 *  a 1xn array that splits the response string by the ',' character.\n
 *  Depending on the calling function, the data can be split again \n
 *  by the '#' character, resulting in a 2xn array.\n\n
 *  </code>
 * 
 * TODO: Include flowchart
 *  
 * @see SegaDataServlet
 * @author jdk85
 *
 */
public class DataCollector {
	/** RBNB sink object*/
	private Sink snk; 
	/** RBNB channel map object*/
	private ChannelMap snkMap; 
	/** RBNB Server address*/
	private String serverAddress; 
	/** Name for temporary sink*/
	private String clientName;
	/** Array containing RBNB timestamps*/
	private double[] times = {}; 
	/** Name of operating system */
	private static String OS = System.getProperty("os.name").toLowerCase(); 
	
	/**
	 * Constructor that determines the system IP address.
	 * The constructor checks if the operating system is Unix or Windows based and then fetches
	 * the IP address and updates serverAddress. It also automatically adds the port number
	 * needed to connect to RBNB.
	 */
	public DataCollector(){
		try{
			if (isUnix()) serverAddress = getIP();			
			else serverAddress = java.net.InetAddress.getLocalHost().getHostAddress() +":3333";
		}catch (Throwable t) { 
			System.out.println("THROWN");
			serverAddress="localhost:3333"; 
		}
	}
	public DataCollector(String serverAddress){
		if(!serverAddress.contains(":"))this.serverAddress = serverAddress+":3333";
		else this.serverAddress = serverAddress;
	}
	/**
	 * Opens connection to RBNB.
	 * Method will connect to RBNB that's running on the local machine and return 
	 * a human-readable String that indicates the status of the connection.
	 * 
	 * @param clientName
	 * @return String indicating success or failure in connecting to RBNB server
	 */
	public String connect(String clientName){
		this.clientName = clientName;
		try {
			snk = new Sink();
			snk.OpenRBNBConnection(serverAddress, clientName);
			if(snk.VerifyConnection()) return "Connected As"+ clientName + " to " + serverAddress;
			else return "Error connecting to server";
		}catch (SAPIException e) {
			e.printStackTrace();
			return "SAPI Exception: " + e;
		}catch(Exception e){
			e.printStackTrace();
			return "General Exception: " + e;
		}
	}
	/**
	 * Returns the newest log message for a specified channel.
	 * Calling functions must send the channel name, a start and duration time value
	 * and a reference referring to the type of data request to make.
	 * From the <a href="http://dataturbine.org/sites/default/files/programs/RBNB/doc/index.html">RBNB Documentation</a>,
	 * the reference string has the following options:\n
	 * \code
	 * "absolute" -- The start parameter is absolute time from midnight, Jan 1st, 1970 UTC.\n
     * "newest" -- The start parameter is measured from the most recent data available in the server at 
     * the time this request is received. Note that for this case, the start parameter actually represents 
     * the end of the duration, and positive times proceed toward oldest data. Thus if there is data at times 1, 2, and 3, 
     * Request(map, 1, 0, "newest") will return the data at time 2, while Request(map, -1, 2.5, "newest") will return the 
     * data at times 2 and 3.\n
     * "oldest" -- As "newest", but relative to the oldest data.\n
     * "aligned" -- As "newest", but rather than per channel, this is relative to the newest for all of the channels.\n
     * "after" -- A combination between "absolute" and "newest", this flag causes the server to return the newest data 
     * available after the specified start time. Unlike "newest", you do not have to request the data to find out that 
     * you already have it. Unlike "absolute", a gap may be inserted in the data to provide you with the freshest data.\n
     * "modified" -- Similar to "after", but attempts to return a duration's worth of data in a contiguous block. If the 
     * data is not available after the start time, it will be taken from before the start time.\n
     * "next" - gets the data that immediately follows the time range specified. This will skip over gaps.\n
     * "previous" - get the data that immediately preceeds the time range specified. This will skip over gaps.\n
	 * \endcode
	 * 
	 * 
	 * @param logChannelName - name of the log channel to fetch data from
	 * @param start - start time for RBNB request command
	 * @param duration - duration for RBNB request command
	 * @param ref - string referring to RBNB request type
	 * @return This function returns an HTML formatted String that contains the log information
	 */
	public String getLog(String logChannelName, double start, double duration, String ref){
		String response = "";
		snkMap = new ChannelMap();

		try{
			snkMap.Add(logChannelName);
			snk.Request(snkMap, start, duration, ref);			
			snk.Fetch(-1, snkMap);
			String[] str = snkMap.GetDataAsString(0);
			//HTML div tag to contain the log data
			response = response.concat("<div style=\"font-weight:normal;padding-left: 15px;\">");
			//write the log data
			//RBNB header info is contained in <>, so this just makes that text bold and blue
			//'~~' is used to temporarily represent a '>' character so that doesn't get preemptively replaced by the second replaceall call
			for(String s : str){
				s = s.replaceAll("<", "<span style=\"color:blue;font-weight:bold;\"~~&#60;");
				s = s.replaceAll(">", "&#62;</span>");
				s = s.replaceAll("~~", ">");
				s = s.replaceAll("\n", "<br/>");
				response = response.concat(s+"<br/>");
			}
			//close the div
			response = response.concat("</div>");
			return response;
			
		}catch(SAPIException e){
			e.printStackTrace();
			return "SAPI Exception thrown while retreiving log data";
			
		}catch(Exception e){
			e.printStackTrace();
			return "Unknown Exception thrown while retreiving log info";
		}
	}
	/**
	 * This function returns a string containing RBNB data.
	 * 
	 * @param channelNames - handles retrieving RBNB data for multiple channels
	 * @param start  - start time for RBNB request command
	 * @param duration  - duration for RBNB request command
	 * @param ref - string referring to RBNB request type
	 * @return This function returns a string with RBNB timestamps and data points formatted so that it can be parsed easily by the calling JSP 
	 */
	public String getData(String[] channelNames,double start,double duration,String ref){
		
		snkMap = new ChannelMap();
		String response = "";
		try {
			//add each channel name to the channel map
			for(String s : channelNames){
				snkMap.Add(s);
			}
			//fetch the data for all channels
			snk.Request(snkMap, start, duration, ref);			
			snk.Fetch(-1, snkMap);
			
			//these arrays are used for collecting data for different data types
			//TODO: this can probably be edited/cleaned up for a more efficient way to do this
			double[] dVal = {};
			float[] fVal = {};
			int[] iVal = {};
			short[] sVal = {};
			byte[] bVal = {};
			byte[][] bAVal = {};
			long[] lVal = {};
			String[] strVal = {};			
			for(int i = 0; i < channelNames.length; i++){
				
				int foundIndex = -1;
				//if the channel doesn't contain any data, the returned data array won't contain anything in its place, 
				//so this is used to make sure the order of the channels is maintained
				if((foundIndex = findInArray(snkMap.GetChannelList(),channelNames[i])) != -1){
					//fetch the array containing all the time stamps
					times = snkMap.GetTimes(foundIndex);
					//switch on the datatype and store in the appropriate array
					//each case is exactly the same except for the type of data so only the first case is commented
					//the last data point needs to be marked differently so that the JSP doesn't think there is an empty point at the end of the array
					switch(snkMap.GetType(foundIndex)){
						case ChannelMap.TYPE_FLOAT64:
							//store an array containing all the data points
							dVal = snkMap.GetDataAsFloat64(foundIndex);
							//parse the array and concat the timestamp and corresponding data point to the response string
							for(int j = 0; j < dVal.length; j++){
				        		double d = dVal[j];
				        		//if it is not the last data point add a ',' character to the end of the string
				        		if(j != dVal.length-1){
				        			//'#' separates timestamp and data, ',' separates data points
				        			// -7 hours for time zone offset
				        			response = response.concat(times[j]*1000-(7*60*60*1000)+"#").concat(d + ",");
				        		}
				        		//if the last data point has the ',' character, the JSP will add an empty cell to the array it creates from the response string
				        		else{
				        			response = response.concat(times[j]*1000-(7*60*60*1000)+"#").concat(d + "");
				        		}
				        		
				        	}
							break;
	
						case ChannelMap.TYPE_FLOAT32:
							fVal = snkMap.GetDataAsFloat32(foundIndex);
							for(int j = 0; j < fVal.length; j++){
				        		float f = fVal[j];
				        		if(j != fVal.length-1){
				        			response = response.concat(times[j]*1000-(7*60*60*1000)+"#").concat(f + ",");
				        		}
				        		else{
				        			response = response.concat(times[j]*1000-(7*60*60*1000)+"#").concat(f + "");
				        		}
				        		
				        	}
							break;
						
						case ChannelMap.TYPE_INT32:
							iVal = snkMap.GetDataAsInt32(foundIndex);
							for(int j = 0; j < iVal.length; j++){
				        		int in = iVal[j];
				        		if(j != iVal.length-1){
				        			response = response.concat(times[j]*1000-(7*60*60*1000) +"#").concat(in + ",");
				        		}
				        		else{
				        			response = response.concat(times[j]*1000-(7*60*60*1000) +"#").concat(in + "");
				        		}
				        		
				        	}
							break;
	
						case ChannelMap.TYPE_INT16:
							sVal = snkMap.GetDataAsInt16(foundIndex);
							for(int j = 0; j < sVal.length; j++){
				        		short s = sVal[j];
				        		if(j != sVal.length-1){
				        			response = response.concat(times[j]*1000-(7*60*60*1000)+"#").concat(s + ",");
				        		}
				        		else{
				        			response = response.concat(times[j]*1000-(7*60*60*1000)+"#").concat(s + "");
				        		}
				        		
				        	}
							break;
							
						case ChannelMap.TYPE_INT8:
							bVal = snkMap.GetDataAsInt8(foundIndex);
							for(int j = 0; j < bVal.length; j++){
				        		byte b = bVal[j];
				        		if(j != dVal.length-1){
				        			response = response.concat(times[j]*1000-(7*60*60*1000)+"#").concat(b + ",");
				        		}
				        		else{
				        			response = response.concat(times[j]*1000-(7*60*60*1000)+"#").concat(b + "");
				        		}
				        		
				        	}
							break;
							
						case ChannelMap.TYPE_INT64:
							lVal = snkMap.GetDataAsInt64(foundIndex);
							for(int j = 0; j < lVal.length; j++){
				        		long l = lVal[j];
				        		if(j != lVal.length-1){
				        			response = response.concat(times[j]*1000-(7*60*60*1000)+"#").concat(l + ",");
				        		}
				        		else{
				        			response = response.concat(times[j]*1000-(7*60*60*1000)+"#").concat(l + "");
				        		}
				        		
				        	}
							break;	
							
						case ChannelMap.TYPE_BYTEARRAY:
							bAVal = snkMap.GetDataAsByteArray(foundIndex);
							SimpleDateFormat date_format = new SimpleDateFormat("MMM dd hh:mm:ss:S a Z");
							date_format.setTimeZone(TimeZone.getTimeZone("US/Arizona"));
							
							for(int k = 0; k < bAVal.length; k++){
								Date resultdate = new Date((long)(times[k]*1000));
								response = response.concat("<p><span style=\"font-family:'Courier New', Courier, monospace;font-size:16px;color:blue;font-weight:bold;\">Command Issued:&nbsp;</span>");
								response = response.concat("<span style=\"font-family:'Courier New', Courier, monospace;font-size:14px;color:#3A3F42;font-weight:normal;\">");
								response = response.concat(date_format.format(resultdate));
								response = response.concat("</span><br/>");
								for(int j = 0; j < bAVal[k].length; j++){
							    	if(j<10)response = response.concat("<span style=\"font-family:'Courier New', Courier, monospace;font-size:12px;color:#3A3F42;font-weight:bold;\">BYTE["
							    			+"<span style=\"color:red\">0" + j + "</span>]: " 
							    			+ String.format("0x%02X ", bAVal[0][j])
							    			+ "</span><br/>");
							    	else response = response.concat("<span style=\"font-family:'Courier New', Courier, monospace;font-size:12px;color:#3A3F42;font-weight:bold;\">BYTE["
							    			+"<span style=\"color:red\">" + j + "</span>]: " 
							    			+ String.format("0x%02X ", bAVal[0][j])
							    			+ "</span><br/>");
							    }
								response = response.concat("<br/></p>");
							}
							break;
						case ChannelMap.TYPE_STRING:
							strVal = snkMap.GetDataAsString(foundIndex);
							for(int j = 0; j < strVal.length; j++){
				        		String s = strVal[j];
				        		if(j != lVal.length-1){
				        			response = response.concat(times[j]*1000-(7*60*60*1000)+": ").concat(s + "\r\n");
				        		}
				        		else{
				        			response = response.concat(times[j]*1000-(7*60*60*1000)+": ").concat(s + "");
				        		}
				        		
				        	}
							break;
	
						default:
							throw new ClassCastException(
								"Unsupported type.");
					}
					
					//'$' indicates the end of a data set for the particular channel
					response = response.concat("$");
				}
				//if the channel had no new data, indicate it by adding a '$' so the JSP knows that it was an empty data set
				else response = response.concat("$");
				//log.write(channelNames[i] + " Found Index: " + foundIndex);
			}
			return response;

		} catch (SAPIException e) {
			e.printStackTrace();
			return "empty";
		}
		
	}
	
	/**
	 * This method closes the RBNB sink connection.
	 * @return A message saying the sink connection was closed
	 */
	public String disconnect(){
		if(snk.VerifyConnection()){
			snk.CloseRBNBConnection();
		}
		return "Closed connection to " + clientName;
	}
	
	/**
	 * This is a helper method for getData().
	 * 
	 * @param arr - The string array to be searched 
	 * @param value - The value to find in the array
	 * @return The index of the value is returned if it is found, otherwise a -1 is returned
	 */
	public int findInArray(String[] arr, String value){
		
		for(int i = 0; i < arr.length; i++){
			if(arr[i].equalsIgnoreCase(value)) return i;
		}
		return -1;
	}

	/**
	 * Checks if operating system is Unix-based.
	 * 
	 * @return True if operating system is Unix-based, false otherwise.
	 */
	public static boolean isUnix() {		 
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 ); 
	}
	
	/**
	 * Gets IP address of system for Unix-based machines.
	 * 
	 * @return String containing the system's IP address with the RBNB port 3333 added.
	 * @throws SocketException
	 */
	public static String getIP() throws SocketException 
    {
    String ipAddr = "";
    Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets))
        {
        if(netint.getDisplayName().equals("eth0"))
        {
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) 
                {
        ipAddr = inetAddress.toString();
        ipAddr = ipAddr.substring(1);
                }
        }
        }
        return ipAddr + ":3333";
        
    }
	/**
	 * This function returns a string containing RBNB data.
	 * 
	 * @param channelNames - handles retrieving RBNB data for multiple channels
	 * @param start  - start time for RBNB request command
	 * @param duration  - duration for RBNB request command
	 * @param ref - string referring to RBNB request type
	 * @return This function returns a string with RBNB timestamps and data points formatted so that it can be parsed easily by the calling JSP 
	 */
	public LinkedList<Object[]> getDataAsArray(String[] channelNames,double start,double duration,String ref){
		LinkedList<Object[]> objList = new LinkedList<Object[]>();
		snkMap = new ChannelMap();
		try {
			//add each channel name to the channel map
			for(String s : channelNames){
				snkMap.Add(s);
			}
			//fetch the data for all channels
			snk.Request(snkMap, start, duration, ref);			
			snk.Fetch(-1, snkMap);
			
			//these arrays are used for collecting data for different data types
			//TODO: this can probably be edited/cleaned up for a more efficient way to do this
			double[] dVal = {};
			float[] fVal = {};
			int[] iVal = {};
			short[] sVal = {};
			byte[] bVal = {};
			byte[][] bAVal = {};
			long[] lVal = {};
			String[] strVal = {};			
			for(int i = 0; i < channelNames.length; i++){
				Object[] objArr = new Object[3];
				int foundIndex = -1;
				//if the channel doesn't contain any data, the returned data array won't contain anything in its place, 
				//so this is used to make sure the order of the channels is maintained
				if((foundIndex = findInArray(snkMap.GetChannelList(),channelNames[i])) != -1){
					//fetch the array containing all the time stamps
					times = snkMap.GetTimes(foundIndex);
					//switch on the datatype and store in the appropriate array
					//each case is exactly the same except for the type of data so only the first case is commented
					//the last data point needs to be marked differently so that the JSP doesn't think there is an empty point at the end of the array
					switch(snkMap.GetType(foundIndex)){
						case ChannelMap.TYPE_FLOAT64:
							//store an array containing all the data points
							dVal = snkMap.GetDataAsFloat64(foundIndex);
							objArr[0] = ChannelMap.TYPE_FLOAT64;
							objArr[1] = times;
				        	objArr[2] = dVal;
				        	objList.push(objArr);
				        	break;
	
						case ChannelMap.TYPE_FLOAT32:
							fVal = snkMap.GetDataAsFloat32(foundIndex);
							objArr[0] = ChannelMap.TYPE_FLOAT32;
							objArr[1] = times;
				        	objArr[2] = fVal;
				        	objList.push(objArr);
							break;
						
						case ChannelMap.TYPE_INT32:
							iVal = snkMap.GetDataAsInt32(foundIndex);
							objArr[0] = ChannelMap.TYPE_INT32;
							objArr[1] = times;
				        	objArr[2] = iVal;
				        	objList.push(objArr);
							break;
	
						case ChannelMap.TYPE_INT16:
							sVal = snkMap.GetDataAsInt16(foundIndex);
							objArr[0] = ChannelMap.TYPE_INT16;
							objArr[1] = times;
				        	objArr[2] = sVal;
				        	objList.push(objArr);
							break;
							
						case ChannelMap.TYPE_INT8:
							bVal = snkMap.GetDataAsInt8(foundIndex);
							objArr[0] = ChannelMap.TYPE_INT8;
							objArr[1] = times;
				        	objArr[2] = bVal;
				        	objList.push(objArr);
							break;
							
						case ChannelMap.TYPE_INT64:
							lVal = snkMap.GetDataAsInt64(foundIndex);
							objArr[0] = ChannelMap.TYPE_INT64;
							objArr[1] = times;
				        	objArr[2] = lVal;
				        	objList.push(objArr);
							break;	
							
						case ChannelMap.TYPE_BYTEARRAY:
							bAVal = snkMap.GetDataAsByteArray(foundIndex);
							objArr[0] = ChannelMap.TYPE_BYTEARRAY;
							objArr[1] = times;
				        	objArr[2] = bAVal;
				        	objList.push(objArr);
							break;
						case ChannelMap.TYPE_STRING:
							strVal = snkMap.GetDataAsString(foundIndex);
							objArr[0] = ChannelMap.TYPE_STRING;
							objArr[1] = times;
				        	objArr[2] = strVal;
				        	objList.push(objArr);
							break;
	
						default:
							throw new ClassCastException(
								"Unsupported type.");
					}

				}
				else objList.push(null);
			}
			return objList;

		} catch (SAPIException e) {
			e.printStackTrace();
			return objList;
		}
		
	}

	public Date[] getValidTimeInterval(String[] channelNames) throws SAPIException,Exception {
		snkMap = new ChannelMap();
		for(String s : channelNames){
			snkMap.Add(s);
		}
		snk.Request(snkMap, 0, 0, "oldest");			
		snk.Fetch(-1, snkMap);
		Date startDate = new Date(),endDate=new Date(0),tempDate;
		
		for(int i = 0; i < snkMap.NumberOfChannels(); i++){
			if((tempDate = new Date((long)snkMap.GetTimeStart(i)*1000)).before(startDate)){
				startDate = tempDate;
			}
		}
		snk.Request(snkMap, 0, 0, "newest");			
		snk.Fetch(-1, snkMap);
		for(int i = 0; i < snkMap.NumberOfChannels(); i++){
			if((tempDate = new Date((long)snkMap.GetTimeStart(i)*1000)).after(endDate)){
				endDate = tempDate;
			}
		}
		
		
		
		Date[] interval = {startDate,endDate};
		return interval;
	}

}
