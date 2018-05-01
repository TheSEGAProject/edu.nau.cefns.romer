package helpers;

import java.util.ArrayList;
import java.util.Date;

import utilities.DataGeneratorCodec;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;


/**
 * This helper class handles connecting to RBNB, collects the data and wraps it in a custom RBNB object.
 * 
 * 
 * @see RBNBObject
 * @see XYDataPointObject
 * @see SegaDataServlet
 * @author jdk85
 *
 */
public class DataFetchHelper {
	/** RBNB sink object*/
	private Sink snk;
	/** RBNB channel map object*/
	private ChannelMap snkMap; 	
	/** RBNB Server address*/
	private String serverAddress;
	/** Name for temporary sink*/
	private String clientName;
	/**
	 * Constructor
	 * This method adds a port number if not included, otherwise it just stores the
	 * server address to the local variable serverAddress.
	 * @param serverAddress
	 */
	public DataFetchHelper(String serverAddress){
		if(!serverAddress.contains(":"))this.serverAddress = serverAddress+":3333";
		else this.serverAddress = serverAddress;
	}
	/**
	 * Opens connection to RBNB.
	 * Method will connect to the RBNB that's running on serverAddress sent in to the constructor and return 
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
			return "SAPI Exception: " + e;
		}catch(Exception e){
			return "General Exception: " + e;
		}
	}
	
	
	/**
	 * This function returns an array list containing RBNB channel data objects.
	 * 
	 * @param channelNames - handles retrieving RBNB data for multiple channels
	 * @param start  - start time for RBNB request command
	 * @param duration  - duration for RBNB request command
	 * @param ref - string referring to RBNB request type
	 * @return This function returns a string with RBNB timestamps and data points formatted so that it can be parsed easily by the calling JSP 
	 */
	public ArrayList<RBNBChannelObject> getData(String[] channelNames, double start, double duration, String ref){
		snkMap = new ChannelMap();
		try {
			//Add each channel name to the channel map
			for(String s : channelNames){
				snkMap.Add(s);
			}
			//Make the request
			snk.Request(snkMap, start, duration, ref);	
			//Fetch the data using -1 timeout, if there isn't data immediately available, it will return an empty channel map 
			snk.Fetch(-1, snkMap);
			int numberOfChannels = snkMap.NumberOfChannels();
			//Place holder for RBNB timestamp array
			double[] times = null;
			
			//If there is no data return from the function
			if(numberOfChannels == 0) return null;
			
			//Otherwise, create a new arraylist of RBNBChannelObject
			ArrayList<RBNBChannelObject> data = new ArrayList<RBNBChannelObject>();
			
			//Iterate over the returned channel map based on the number of channels
			for(int i = 0; i < numberOfChannels; i++){
				
				//Store the channel name
				String channel_name = snkMap.GetName(i);
				
				//Create a channel object for each data set
				RBNBChannelObject rbco = new RBNBChannelObject(channel_name);
				
				//Check the data type
				int typeID = snkMap.GetType(i);				
				
				//Store channel info in the channel object
				rbco.setSample_type_ID(typeID);
				rbco.setSample_type_name(snkMap.TypeName(typeID));
				
				//Determine what type of timestamp sample to store			
				
				
				//Fetch timestamp data
				times = snkMap.GetTimes(i);
				
				//Create an array list for the data points 
				ArrayList<SampleTimestampPackage> dataPoints = new ArrayList<SampleTimestampPackage>();
				
				//Switch over data type and store data point and timestamp in a new XYDataPointObject
				//and add XYDataPointObject to the dataPoints array
				switch(typeID){
					case ChannelMap.TYPE_BYTEARRAY:
						byte[][] byteArrayVals = snkMap.GetDataAsByteArray(i);
						if(channel_name.contains("channelized") && (channel_name.contains("cr1000") || channel_name.contains("wisard"))){
							//Store channel info in the channel object
							rbco.setSample_type_ID(byteArrayVals[0][0]);
							rbco.setSample_type_name(snkMap.TypeName(byteArrayVals[0][0]));
							
							for(int j = 0; j < byteArrayVals.length;j++){
								dataPoints.add(DataGeneratorCodec.decodeValuePair(times[j],byteArrayVals[j]));
							}
						}
						else{
							for(int j = 0; j < byteArrayVals.length;j++){
								dataPoints.add(new SampleTimestampPackage((long)(times[j]*1000),(long)(times[j]*1000),byteArrayVals[j]));
							}
						}
						break;
					case ChannelMap.TYPE_FLOAT32:			
						float[] float32Vals = snkMap.GetDataAsFloat32(i);
						for(int j = 0; j < float32Vals.length;j++){						
							dataPoints.add(new SampleTimestampPackage((long)(times[j]*1000),(long)(times[j]*1000),float32Vals[j]));
						}	
						break;
					case ChannelMap.TYPE_FLOAT64:
						double[] float64Vals = snkMap.GetDataAsFloat64(i);
						
						for(int j = 0; j < float64Vals.length;j++){
							dataPoints.add(new SampleTimestampPackage((long)(times[j]*1000),(long)(times[j]*1000),float64Vals[j]));
						}
						
						break;
					case ChannelMap.TYPE_INT16:
						short[] int16Vals = snkMap.GetDataAsInt16(i);
						for(int j = 0; j < int16Vals.length;j++){
							dataPoints.add(new SampleTimestampPackage((long)(times[j]*1000),(long)(times[j]*1000),int16Vals[j]));
						}	
						break;				
					case ChannelMap.TYPE_INT32:
						int[] int32Vals = snkMap.GetDataAsInt32(i);
						for(int j = 0; j < int32Vals.length;j++){
								dataPoints.add(new SampleTimestampPackage((long)(times[j]*1000),(long)(times[j]*1000),int32Vals[j]));
						}
						break;
					case ChannelMap.TYPE_INT64:
						long[] int64Vals = snkMap.GetDataAsInt64(i);
						for(int j = 0; j < int64Vals.length;j++){
							dataPoints.add(new SampleTimestampPackage((long)(times[j]*1000),(long)(times[j]*1000),int64Vals[j]));
						}
						break;
					case ChannelMap.TYPE_INT8:
						byte[] int8Vals = snkMap.GetDataAsInt8(i);
						for(int j = 0; j < int8Vals.length;j++){
							dataPoints.add(new SampleTimestampPackage((long)(times[j]*1000),(long)(times[j]*1000),int8Vals[j]));
						}
						break;
					case ChannelMap.TYPE_STRING:
						String[] stringVals = snkMap.GetDataAsString(i);
						for(int j = 0; j < stringVals.length;j++){
							dataPoints.add(new SampleTimestampPackage((long)(times[j]*1000),(long)(times[j]*1000),stringVals[j]));
						}
						break;
									
				}
				
				//Add the datapoints to the channel object
				rbco.setSample_data(dataPoints);
				//Add the channel object to the data array
				data.add(rbco);				
				
			}
			
			//return the array
			return data;

		} catch (SAPIException e) {
			return null;
		}
		catch (Exception e) {
			return null;
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
	 * This method is used to fetch the absolute start and end dates of a given set of channels.
	 * It is used by the web app to fill in the start/end data intervals when fetching data by date.
	 * This function specifies a valid date range for a given set of data, but takes the longest possible
	 * range over all selected channels. For example, if channel one spans June 5th to June 8th and channel two
	 * spans June 12th to June 24th, the valid date range returned will be June 5th to June 24th.
	 *
	 * @param channelNames
	 * @return interval
	 * @throws SAPIException
	 * @throws Exception
	 */
	public Date[] getValidTimeInterval(String[] channelNames) throws SAPIException,Exception {
		//Create a temp channel map using snkMap
		snkMap = new ChannelMap();
		//Add all the channels passed to the function
		for(String s : channelNames){
			snkMap.Add(s);
		}
		//START DATE
		//Make the request and fetch the data using the oldest data point
		snk.Request(snkMap, 0, 0, "oldest");			
		snk.Fetch(-1, snkMap);
		//Create a start,end, and temp Date object
		Date startDate = new Date(),endDate=new Date(0),tempDate;
		
		//Iterate over the channels that have returned data
		for(int i = 0; i < snkMap.NumberOfChannels(); i++){
			//Compare each point and if the time is before the currently stored temp date, reassign tempDate
			if((tempDate = new Date((long)snkMap.GetTimeStart(i)*1000)).before(startDate)){
				startDate = tempDate;
			}
		}
		//END DATE
		//Make the request and fetch the data using the most recent data point
		snk.Request(snkMap, 0, 0, "newest");			
		snk.Fetch(-1, snkMap);
		//Iterate over the channels that have returned data
		for(int i = 0; i < snkMap.NumberOfChannels(); i++){
			//Compare each point and if the time is after the currently stored temp date, reassign tempDate
			if((tempDate = new Date((long)snkMap.GetTimeStart(i)*1000)).after(endDate)){
				endDate = tempDate;
			}
		}
		
		
		//Return a date object containing the absolute start and end date range for the data
		Date[] interval = {startDate,endDate};
		return interval;
	}

}
