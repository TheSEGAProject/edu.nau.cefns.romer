package helpers;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.client.authentication.AttributePrincipal;

import servlets.SegaChannelListServlet;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

/**
 * Fetches RBNB channel names for SegaChannelListServlet.
 * This class is used as a helper class by the SegaChannelListServlet class. When a user requests RBNB channels
 * via a web browser, the SegaChannelListServlet handles the request and utilizes the ChannelTreeRetriever class
 * to create a temporary RBNB sink that pulls all the channel names and returns them to SegaChannelListServlet
 * as a String array.
 * @see SegaChannelListServlet
 * 
 * @author jdk85
 *
 */
public class ChannelTreeRetriever {
	/** IP address of machine that runs this class */
	private String serverAddress; 
	/** Name for the temporary sink channel that is created to fetch channels */
	private String clientName = "TreeRetrieverSink"; 
	 /**RBNB sink object*/  
	private Sink snk = new Sink();
	/** Channel map used to request registration to RBNB sink*/
	private ChannelMap cm; 
	/** Name value of operating system */
	private static String OS = System.getProperty("os.name").toLowerCase(); 
	
	/**
	 * Constructor that determines the system IP address.
	 * The constructor checks if the operating system is Unix or Windows based and then fetches
	 * the IP address and updates serverAddress. It also automatically adds the port number
	 * needed to connect to RBNB.
	 */
	public ChannelTreeRetriever(){
		try{
			if (isUnix()) serverAddress = getIP();			
			else serverAddress = java.net.InetAddress.getLocalHost().getHostAddress() +":3333";			
		}catch (Throwable t) { 
			serverAddress="localhost:3333"; 
		}
	}
	public ChannelTreeRetriever(String serverAddress){
		this.serverAddress = serverAddress;
	}
	
	/**
	 * Method for fetching RBNB channel names.
	 * This method connects to RBNB temporarily and fetches the names of all available channels.
	 * If no channels are available, it will return a String array containing an error message. 
	 * @return A string array containing the name of each RBNB channel.
	 */
	public String[] getChannelTree(){
		String[] channels = {}; //Contains the string values (names) of RBNB channels
		try{
			if(!snk.VerifyConnection()){
				//Open sink connection
				snk.OpenRBNBConnection(serverAddress, clientName);
				//Create temporary registration map
				ChannelMap regMap = new ChannelMap();
                //Create channel map to hold the channels
				cm = new ChannelMap();
				//Request registration to sink connection
                snk.RequestRegistration(cm);
				//Fetch results from sink, store in registration map
                regMap = snk.Fetch(10,cm);                
                //Fetch the channel names as a String[]
                channels = regMap.GetChannelList();
                //Shutdown RBNB connection
                snk.CloseRBNBConnection();
                //Return errors if no channels were returned
                if(channels.length != 0) return channels;
                else return null;
			}
			//If connection can't be made, return empty string array
			else return channels;
		}catch(SAPIException e){
			return null;
		}
		
	}
	
	/**
	 * Currently unimplemented, this method takes an HttpServletRequest as a parameter and is supposed
	 * to only return channels that the user making the request has access to. The idea is to return every channel
	 * if the user has admin privileges or check experiment specific access and return channels based on that.
	 * There could also eventually be a guest service that will return channels that are flagged as public by
	 * an experimenter.
	 * @deprecated Use
	 * @return A string array containing the name of each RBNB channel.
	 */
	public String[] getChannelTree(HttpServletRequest request){
		String[] errors = {"No public channels avaible"}; //Returned in the case of an error while fetching the channel names
		if (request.getUserPrincipal() != null) {
			AttributePrincipal principal = (AttributePrincipal) request.getUserPrincipal();
			
			Map<String, Object> attributes = principal.getAttributes();
			if (attributes != null) {
		        Iterator<String> attributeNames = attributes.keySet().iterator();
		        String [] approvedChannels = {};
		        String chanNames = "";
		        boolean isAdmin = false;
		        //Cycle through first run to check for admin
		        for(;attributeNames.hasNext();){
		        	String attributeName = (String) attributeNames.next();
		        	if(attributeName.equalsIgnoreCase("role.name")){
						if(((String)attributes.get(attributeName)).contains("administrator"));
						//if user has admin privileges, set isAdmin flag as true
						isAdmin = true;
					}
		        }
		        
		        if(isAdmin){
		        	return getChannelTree();
		        }
		        else if(!isAdmin){
		        	//Cycle through second time to check channel names 
			        for (;attributeNames.hasNext();) {
						
						String attributeName = (String) attributeNames.next();
						if(attributeName.equalsIgnoreCase("channel_name")){
							chanNames = (String)attributes.get(attributeName);
							chanNames = chanNames.replace("[", "");
							chanNames = chanNames.replace("]", "");
							chanNames = chanNames.replace(" ", "");
							approvedChannels =  chanNames.split("[,]+");
						}
						
			        }
			        if(approvedChannels.length == 0){
			        	return getPublicChannels();
			        }
			        else return null;
				}
		        else return errors;
		        
			} else return errors;
		} else return errors;
		

	
		
		
	}
	
	/**
	 * This is the currently implemented method for returning channel information as an array of RBNBSourceObject, which
	 * can be easily parsed by the calling .jsp
	 * 
	 * 
	 * 
	 * TODO: Attach experiment id to channels so that we can return a richer set of information about the channel
	 * @see getUserData() and putUserData() from the RBNB API
	 * 
	 */
	public List<RBNBSourceObject> getSourceObjects(){		
		//Fetch the channel tree
		String[] channels = getChannelTree();
		if(channels != null){
			//Create placeholder arraylist for source objects
			List<RBNBSourceObject> sources = new ArrayList<RBNBSourceObject>();
			//Create placeholder listiterator to check if a source already exists 
			ListIterator<RBNBSourceObject> li = null;
				
				//Iterate over all the channel names in the channel string array
				for(String s : channels){
					//TODO: Verify that the source name never contains a '/'
					//Split the fully qualified channel name into source and channel strings
					String sourceName = s.substring(0,s.indexOf('/'));
					String channelName = s.substring(s.indexOf('/')+1);
					//Index used to determine if the source is already included in the source object array
					int sourceObjectIndex = -1;
					//Set the list iterator equal to a list iterator for the current contents of the source array
					li = sources.listIterator();
					//For each source object in the array, check if the source already exists
					//If the source is already stored 
					//TODO: There is probably a more efficient way to do this
					while(li.hasNext()){
						if(li.next().getSourceName().equals(sourceName)) sourceObjectIndex = li.previousIndex();
					}
					
					//If the source hasn't already been added to the list, add the source and define the channel type
					//TODO: Implement more descriptors for each source (e.g. garden server, permissions, experiments, etc.)
					if(sourceObjectIndex == -1){
						//TODO: distinguish between sensor/network/management channels
						//If the source is not a log or metric channel, assume experiment data
						if(sourceName.indexOf("_Log") < 0 && sourceName.indexOf("_Metrics") < 0){
							sources.add(new RBNBSourceObject(sourceName,"experiment_data"));
							sources.get(sources.size()-1).addChannelName(channelName);
						}
						//If the source is a log channel
						else if(sourceName.indexOf("_Log") >= 0 && sourceName.indexOf("_Metrics") < 0){
							sources.add(new RBNBSourceObject(sourceName,"log_channel"));
							sources.get(sources.size()-1).addChannelName(channelName);
						}
						//If the source is a metric channel
						else if(sourceName.indexOf("_Log") < 0 && sourceName.indexOf("_Metrics") >= 0){
							sources.add(new RBNBSourceObject(sourceName,"metric_channel"));
							sources.get(sources.size()-1).addChannelName(channelName);
						}
						
					}
					//If the source already has been added to the arraylist, get the source object and add the new
					//channel to the source object
					else{
						sources.get(sourceObjectIndex).addChannelName(channelName);
					}	
					
				}
				//Return the arraylist of the sources
				return sources;
		}
		
		else return null;
}
	/**
	 * This is the currently implemented method for returning channel information as an array of RBNBSourceObject, which
	 * can be easily parsed by the calling .jsp
	 * 
	 * 
	 * 
	 * TODO: Attach experiment id to channels so that we can return a richer set of information about the channel
	 * @see getUserData() and putUserData() from the RBNB API
	 * 
	 */
	public List<RBNBSourceObject> getSourceObjectsExcludingHidden(){		
		//Fetch the channel tree
		String[] channels = getChannelTree();
		if(channels != null){
			//Create placeholder arraylist for source objects
			List<RBNBSourceObject> sources = new ArrayList<RBNBSourceObject>();
			//Create placeholder listiterator to check if a source already exists 
			ListIterator<RBNBSourceObject> li = null;
				
				//Iterate over all the channel names in the channel string array
				for(String s : channels){
					//TODO: Verify that the source name never contains a '/'
					//Split the fully qualified channel name into source and channel strings
					String sourceName = s.substring(0,s.indexOf('/'));
					String channelName = s.substring(s.indexOf('/')+1);
					//Index used to determine if the source is already included in the source object array
					int sourceObjectIndex = -1;
					//Set the list iterator equal to a list iterator for the current contents of the source array
					li = sources.listIterator();
					//For each source object in the array, check if the source already exists
					//If the source is already stored 
					//TODO: There is probably a more efficient way to do this
					while(li.hasNext()){
						if(li.next().getSourceName().equals(sourceName)) sourceObjectIndex = li.previousIndex();
					}
					
					//If the source hasn't already been added to the list, add the source and define the channel type
					//TODO: Implement more descriptors for each source (e.g. garden server, permissions, experiments, etc.)
					if(sourceObjectIndex == -1){
						//TODO: distinguish between sensor/network/management channels
						//If the source is not a log or metric channel, assume experiment data
						if(sourceName.indexOf("_Log") < 0 && sourceName.indexOf("_Metrics") < 0){
							sources.add(new RBNBSourceObject(sourceName,"experiment_data"));
							sources.get(sources.size()-1).addChannelName(channelName);
						}						
						
					}
					//If the source already has been added to the arraylist, get the source object and add the new
					//channel to the source object
					else{
						sources.get(sourceObjectIndex).addChannelName(channelName);
					}	
					
				}
				//Return the arraylist of the sources
				return sources;
		}
		
		else return null;
}
	/**
	 * Method for fetching RBNB channel names when a user is not logged in.
	 * This method connects to RBNB temporarily and fetches the names of all available channels.
	 * If no channels are available, it will return a String array containing an error message. 
	 * @deprecated
	 * @return A string array containing the name of each RBNB channel.
	 */
	public String[] getPublicChannels(){
		String[] channels = {}; //Contains the string values (names) of RBNB channels 
		String[] errors = {"Currently no public channels"}; //Returned in the case of an error while fetching the channel names 
			channels = getChannelTree();
                if(channels.length != 0){
                	List<String> tempStrings = new ArrayList<String>();
                	for(String s: channels){
                		if(s.contains("public"))tempStrings.add(s);                		
                	}
                	if(tempStrings.size() != 0){
                		String[] result = {};
                		result = tempStrings.toArray(result);
                		return result;
                	}
                	else return errors;
                }
                else return errors;
			
		
		
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
	public static String getIP() throws SocketException{
    String ipAddr = ""; //temporary variable to store IP address
    Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)){
	        if(netint.getDisplayName().equals("eth0")){
		        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
		        for (InetAddress inetAddress : Collections.list(inetAddresses)){
			        ipAddr = inetAddress.toString();
			        ipAddr = ipAddr.substring(1);
		        }
	        }
        }
        return ipAddr + ":3333"; //Adds port number for RBNB purposes
    }
	

}
