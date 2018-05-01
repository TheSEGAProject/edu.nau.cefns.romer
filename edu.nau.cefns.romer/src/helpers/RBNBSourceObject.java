package helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom class representation of an RBNB source object
 * It contains the source name and type (e.g. log, metric, experiment)
 * as well as a String list of all channels associated with that source
 * 
 * @author jdk85
 */
public class RBNBSourceObject implements java.io.Serializable {

	
	/** Required by java.io.Serializable*/
	private static final long serialVersionUID = 4004327447902380606L;
	/** The name of the source as provided by RBNB */
	private String sourceName = null;
	/** The type of the source (log,metric,experiment, etc.)*/
	private String type = null; //should this eventually be an enum/interface?
	/** An String array containing all the channel names in the source */
	private List<String> channelNames = new ArrayList<String>();
		/**
		 * Generic constructor takes sourceName and type as strings
		 * @param sourceName
		 * @param type
		 */
		public RBNBSourceObject(String sourceName, String type) {
			this.sourceName = sourceName;
			this.type = type;
		}
		/**
		 * Adds a new channel name to the source channelName array list
		 * @param channelName
		 */
		public void addChannelName(String channelName){
			channelNames.add(channelName);
		}
		/*
		 * GETTERS/SETTERS
		 */
		public void setSourceName(String sourceName) {
	        this.sourceName = sourceName;
	    }
	    public void setType(String type) {
	        this.type = type;
	    }

	    

	    public String getSourceName() {
	    	return sourceName;
	    }
	    public String getType() {
	    	return type;
	    }
	    public List<String> getChannelNames(){
	    	return channelNames;
	    }
	    public String[] getChannels() {
	    	return channelNames.toArray(new String[0]);
	    }
	   

	}