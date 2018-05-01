package experiments;
/*
This experiment monitors a specified soil moisture channel and tries to mirror that value to another. An allowable range is set that determines whether commands need to be sent. This initial version does not check for valve status.
 */

import helpers.ExperimentParameterObject;
import interfaces.ControlInterface;

import java.io.PrintWriter;
import java.io.StringWriter;

import utilities.ControlClass;
import utilities.Experiment;
import utilities.Rule;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
/**
 * @author jdk85
 *
 */
public class SimpleExperiment extends ControlClass implements java.io.Serializable  { 


	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String confirmation = "Last Edit: 08/13/2014 - 1:26 PM";
	private int attempts = 0;
	/**
	 * DemoExp Constructor
	 * The constructor simply calls the parent constructor
	 * @param expID
	 */
	public SimpleExperiment(String expID, String expDesc, String sinkServerAddress,String sourceServerAddress,boolean init) {
		super(expID,expDesc,sinkServerAddress,sourceServerAddress,init);
	}
	/**
	 * 
	 * @see ExperimentParameterObject
	 * @see ControlClass
	 * @see Experiment
	 */
	@Override
	public void initParams(){
		super.initParams();
		ExperimentParameterObject confMessage = new ExperimentParameterObject("confMessage",confirmation,"Confirmation Message","ExperimentParameters","The message parameter is used to verify that the correct version of the Java source code was uploaded",false,false);
		
		ExperimentParameterObject monitor_channel_name = new ExperimentParameterObject(
				"monitor_channel_name",
				"Spoof_wisard_10/Random",
				"Source Channel Name to Monitor",
				"Experiment Parameters",
				"This parameter specifies the channel to monitor",
				true,
				false);
		
		ExperimentParameterObject destination_address_high_byte = new ExperimentParameterObject(
				"destination_address_high_byte",
				"0x00",
				"Destination Address (HI Byte)",
				"Experiment Parameters",
				"This is the high byte value for the WiSARD destination ID. This address is used to declare the destination address when creating command messages for the WiSARDNetwork. It should be written as hex and may include the leading '0x'.",
				true,
				true);
		
		ExperimentParameterObject destination_address_low_byte = new ExperimentParameterObject(
				"destination_address_low_byte",
				"0xBC",
				"Destination Address (LOW Byte)",
				"Experiment Parameters",
				"This is the low byte value for the WiSARD destination ID. This address is used to declare the destination address when creating command messages for the WiSARDNetwork. It should be written as hex and may include the leading '0x'.",
				true,
				true);
		
		
		
		getParameters().add(confMessage);
		
		getParameters().add(monitor_channel_name);
		
		getParameters().add(destination_address_high_byte);
		getParameters().add(destination_address_low_byte);
	}
	/**
	 * @see ControlInterface
	 */
	@Override
	public void initRules() {
		super.initRules();
		getRules().add(new Rule("Debug Logging","If running, experiment will write to the local log file",true,new String[0]));
		getRules().add(new Rule("reconnectOnTimeout","If this rule is running, the control will automatically try to " +
				"reconnect on exceptions. If this rule is reset, manually reconnect to resume normal operation.",
				true,new String[0]));
		getRules().add(new Rule("MonitorSourceChannel","This rule checks to see if we should be monitoring the channel specified" +
				" and generating commands.",
				true,new String[0]));
		
		
	}
	
	/**
	 * getMessage is just used to provide a sanity check when uploading
	 * control programs to segaWeb. The message will be displayed in the edit experiment menu
	 * and can be used to verify that the correct version of a program was uploaded.
	 */
	@Override
	public String getMessage() {
		return confirmation;
		
	}
	/**
	 * This override is pretty important. It overrides the parent createRBNBControl class and returns an inner
	 * class from this instance instead of an inner RBNBControl class from the parent ControlClass
	 */
	@Override
	protected RBNBControl createRBNBControl(){
		return new RBNBControl();
	}

		/**
		 * This class only provides the logic for experiment specific control and 
		 * then relies on the parent class RBNBControl to provide all base level logic
		 * such as RBNB connections, getters and setters for the channel maps needed
		 * by RBNBControl, as well as general command packet structuring.
		 * @author jdk85
		 *
		 */
		public class RBNBControl extends ControlClass.RBNBControl{
			@Override
			public void run(){
	    	try{
		    		if(connect()) updateParameter("runningStatus",runningStr); 		
			        byte[] cmdFinal = new byte[9];
			        byte[] cmdSent = new byte[1];
			        ChannelMap aMap = new ChannelMap();
			        int monitorChannelIndex = aMap.Add((String)getParameterById("monitor_channel_name").getValue());
			        getSink().Subscribe(aMap);
			        int[] data_val;
			        int wis_time = 0;
			        int spoof_val = 0;
			        /*
			         * Main Loop - continually checks sink channels for soil moisture status values, sends cmds as needed
			         */
			        
				        while(!Thread.currentThread().isInterrupted()){   
					        
				        	if(getRule("MonitorSourceChannel").getIsRunning()){
				        		//Fetches new data
				        		aMap = getSink().Fetch(10000);				        	
		        		
								if(!aMap.GetIfFetchTimedOut()){
									if((monitorChannelIndex = aMap.GetIndex((String)getParameterById("monitor_channel_name").getValue())) != -1){
					            		//Store the data points
										data_val = aMap.GetDataAsInt32(monitorChannelIndex);
										if(data_val.length == 2){
											wis_time = data_val[0];
											spoof_val = data_val[1];
											if(spoof_val >= 128){													
												cmdFinal[0] = (byte)(wis_time  >> 24 & 0xFF);
												cmdFinal[1] = (byte)(wis_time  >> 16 & 0xFF);
												cmdFinal[2] = (byte)(wis_time  >>  8 & 0xFF);
												cmdFinal[3] = (byte)(wis_time  >>  0 & 0xFF);
												cmdFinal[4] = (byte)(spoof_val >> 24 & 0xFF);
												cmdFinal[5] = (byte)(spoof_val >> 16 & 0xFF);
							 					cmdFinal[6] = (byte)(spoof_val >>  8 & 0xFF);
												cmdFinal[7] = (byte)(spoof_val >>  0 & 0xFF);
												cmdFinal[8] = 0x5B;
												cmdSent[0] = 0x5B;
												command(cmdFinal);
											}
										}
					            		
									}
								
								}
				        		
					        }
					        	
				        }
					        	
					 
	    		
	    	
			}catch(SAPIException e){
				if(validateReconnectAttempt(e.getLocalizedMessage(),attempts++)){
					reconnect();
				}
				else{
					updateParameter("runningStatus",notRunningStr);
					log.write("Reconnect validation failed, experiment operation will not be resumed");
				}
				
			}catch(Exception e){
				updateParameter("runningStatus",notRunningStr);
	    		StringWriter err = new StringWriter();
	        	e.printStackTrace(new PrintWriter(err));
	        	if(getRule("Debug Logging").getIsRunning()){
	        		log.write("Reconnect validation failed, experiment operation will not be resumed");
	        		log.write(err);
	        	}
	        	
	        }
	    }
		
	}

}
