package helpers;

import java.util.ArrayList;
import java.util.List;
/**
 * This is a helper class used to store channel information as well as the channel's datapoints
 * and timestamps in an array of XYDataPointObjects
 * 
 * @author jdk85
 *@see SampleTimestampPackage
 *
 */
public class RBNBChannelObject implements java.io.Serializable {
	/** Required by java.io.Serializable */
	private static final long serialVersionUID = 6353567056587091412L;
	/** The channel name as displayed by RBNB*/
	private String channel_name = null;
	/** The data type of the channel data*/
	private int sample_type_ID  = -1;
	/** A readable string of the data type*/
	private String sample_type_name = null;

	/** An array list object containing key/value for timestamps and datapoints*/
	private List<SampleTimestampPackage> sample_data = new ArrayList<SampleTimestampPackage>();
	
	
	
		/**
		 * Constructor takes in the channel name
		 * @param channelName
		 */
		public RBNBChannelObject(String channelName) {
			this.setChannel_name(channelName);
		}
		/*
		 * GETTERS/SETTERS
		 */

		

		/**
		 * @return the channel_name
		 */
		public String getChannel_name() {
			return channel_name;
		}



		/**
		 * @param channel_name the channel_name to set
		 */
		public void setChannel_name(String channel_name) {
			this.channel_name = channel_name;
		}



		/**
		 * @return the sample_type_ID
		 */
		public int getSample_type_ID() {
			return sample_type_ID;
		}

		/**
		 * @param sample_type_ID the sample_type_ID to set
		 */
		public void setSample_type_ID(int sample_type_ID) {
			this.sample_type_ID = sample_type_ID;
		}

		/**
		 * @return the sample_type_name
		 */
		public String getSample_type_name() {
			return sample_type_name;
		}

		/**
		 * @param sample_type_name the sample_type_name to set
		 */
		public void setSample_type_name(String sample_type_name) {
			this.sample_type_name = sample_type_name;
		}






		/**
		 * @return the sample_data
		 */
		public List<SampleTimestampPackage> getSample_data() {
			return sample_data;
		}



		/**
		 * @param sample_data the sample_data to set
		 */
		public void setSample_data(List<SampleTimestampPackage> sample_data) {
			this.sample_data = sample_data;
		}



		
	    
	   

	}