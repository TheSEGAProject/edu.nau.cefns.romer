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

import com.rbnb.sapi.SAPIException;
/**
 * @author jdk85
 *
 */
public class ValveCommandExp extends ControlClass implements java.io.Serializable  { 


	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String confirmation = "Last Edit: 09/9/2014 - 11:23 PM";
	private int attempts = 0;
	/**
	 * DemoExp Constructor
	 * The constructor simply calls the parent constructor
	 * @param expID
	 */
	public ValveCommandExp(String expID, String expDesc, String sinkServerAddress,String sourceServerAddress,boolean init) {
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
		
		ExperimentParameterObject wisard_id = new ExperimentParameterObject(
				"wisard_id",
				"1652",
				"Wisard NODE ID",
				"Experiment Parameters",
				"Wisard ID that gets sent a reset command - as an integer",
				true,
				false);
		
		ExperimentParameterObject hub_id = new ExperimentParameterObject(
				"hub_id",
				"1653",
				"Wisard HUB ID",
				"Experiment Parameters",
				"Wisard ID that gets sent a reset command - as an integer",
				true,
				false);

		ExperimentParameterObject processor_id = new ExperimentParameterObject(
				"processor_id",
				"0x03",
				"Processor ID",
				"Experiment Parameters",
				"This is the processor ID that is to receive the valve command. It should be written as hex and may include the leading '0x'.",
				true,
				true);
		
		ExperimentParameterObject command = new ExperimentParameterObject(
				"command",
				"0x5B",
				"Command Value",
				"Experiment Parameters",
				"",
				true,
				true);
		
		
		getParameters().add(confMessage);
		
		getParameters().add(wisard_id);
		getParameters().add(hub_id);
		
		getParameters().add(processor_id);
		
		getParameters().add(command);
	}
	/**
	 * @see ControlInterface
	 */
	@Override
	public void initRules() {
		super.initRules();
		getRules().add(new Rule("Debug Logging","If running, experiment will write to the local log file",true,new String[0]));		
		getRules().add(new Rule("Send Reset Command","This rule checks to see if we should be monitoring the channel specified" +
				" and generating commands.",
				false,new String[0]));
		
		
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
		    		else{		    			
		    			updateParameter("runningStatus",notRunningStr); 
		    			log.write("Unable to connect...");
		    		}
		    		
			        
			        /*
			         * Main Loop - continually checks sink channels for soil moisture status values, sends cmds as needed
			         */
			        
				        while(!Thread.currentThread().isInterrupted()){   
					        
				        	if(getRule("Send Reset Command").getIsRunning()){
				        		short val_short = Short.parseShort((String)getParameterById("wisard_id").getValue());
				        		byte dest_high = (byte)((val_short >> 8 ) & 0xFF);
					    		byte dest_low = (byte) (val_short & 0xFF);
					    		val_short = Short.parseShort((String)getParameterById("hub_id").getValue());
					    		byte dest_high_hub = (byte)((val_short >> 8 ) & 0xFF);
					    		byte dest_low_hub = (byte) (val_short & 0xFF);
					    		
					    		
					    		byte processor = (byte)Integer.parseInt(((String)getParameterById("processor_id").getValue()).replace("0x", ""),16);
					    		byte command = (byte)Integer.parseInt(((String)getParameterById("command").getValue()).replace("0x", ""),16);
					    		
					    		byte msgLenIndex = 10;
						        //Variables for storing fetched data and the command		    		
						        byte[] cmdMessage = 
						        	{
						        		//=======NET=======
						        		//Index:0
						        		
						        		//DESTINATION ADDRESS [2]
						        		dest_high_hub,//(Destination HI)
						        		dest_low_hub,//(Destination LOW)
						        		//SOURCE ADDRESS [2]
						        		(byte)0xFE,//(Source)
						        		(byte)0xFE,
						        		
						        		//=======TRANSPORT=======
						        		//Index: 4
						        		
						        		//MESSAGE ID [1]
						        		(byte)0x03,//(Operational)
						        		//MESSAGE FLAGS [1]
						        		(byte)0x20,//(Single Message)
						        		//MESSAGE NUMBER [2]
						        		(byte)0x00,
						        		(byte)0x01,//(Message 1 of 1)
						        		//MESSAGE ADDRESS [2]
						        		dest_high,
						        		dest_low,//(WiSARD ID)
						        		//MESSAGE PAYLOAD LENGTH [1] Byte Position: 10
						        		(byte)0x00,
						        		
						        		//=======MESSAGE PAYLOAD=======
						        		//Index: 11
						        		//DATA ELEMENT ID [1]
						        		(byte)0x01, //(CMD packet)
						        		//DATA PAYLOAD LENGTH [1]
						        		(byte)0x06,
						        		//DATA ELEMENT VERSION [1]
						        		(byte)0x6E, //??
						        		
						        		//=======DATA ELEMENT PAYLOAD=======
						        		
						        		//PROCESSOR ID [1]
						        		processor, //Processor ID
						        		(byte) 0x03, //Valve is transducer 3
						        		command
						        		
						        		
						        	};
						        cmdMessage[msgLenIndex] = (byte) (cmdMessage.length+1);
						        addCrcAndFlush(cmdMessage);
						        getRule("Send Reset Command").setIsRunning(false);
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
