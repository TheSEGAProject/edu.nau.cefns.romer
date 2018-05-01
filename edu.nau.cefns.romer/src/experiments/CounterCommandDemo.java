package experiments;
/*
This experiment monitors a specified soil moisture channel and tries to mirror that value to another. An allowable range is set that determines whether commands need to be sent. This initial version does not check for valve status.
 */

import helpers.ExperimentParameterObject;
import helpers.SampleTimestampPackage;
import interfaces.ControlInterface;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;

import utilities.ControlClass;
import utilities.DataGeneratorCodec;
import utilities.Experiment;
import utilities.Rule;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
/**
 * @author jdk85
 *
 */
public class CounterCommandDemo extends ControlClass implements java.io.Serializable  { 


	

	private static final long serialVersionUID = 1194186383934964195L;
	private static String confirmation = "Version 1 - Last Edit: 09/25/2014 - 10:41 AM";
	private int attempts = 0;
	private int counter;
	/**
	 * DemoExp Constructor
	 * The constructor simply calls the parent constructor
	 * @param expID
	 */
	public CounterCommandDemo(String expID, String expDesc, String sinkServerAddress,String sourceServerAddress,boolean init) {
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

		ExperimentParameterObject counter_channel = new ExperimentParameterObject(
				"counter_channel",
				"exp249a-03_channelized/wisard_1669/proc_0/datasrc_4",
				"Counter Status Channel",
				"Experiment Parameters",
				"The address of the valve status source channel.",
				true,
				true);		
		ExperimentParameterObject upper_limit = new ExperimentParameterObject(
				"upper_limit",
				"50",
				"Upper Limit",
				"Experiment Parameters",
				"This parameter is the value that is sent along to the CP.",
				true,
				false);
		ExperimentParameterObject lower_limit = new ExperimentParameterObject(
				"lower_limit",
				"25",
				"Lower Limit",
				"Experiment Parameters",
				"This parameter is the value that is sent along to the CP.",
				true,
				false);
		ExperimentParameterObject command_value = new ExperimentParameterObject(
				"command_value",
				"0x0C",
				"Command Value to Send",
				"Experiment Parameters",
				"This parameter is the value that is sent along to the CP.",
				true,
				false);		
		ExperimentParameterObject destination_address_high_byte = new ExperimentParameterObject(
				"destination_address_high_byte",
				"0x06",
				"Destination Address (HI Byte)",
				"Experiment Parameters",
				"This is the high byte value for the WiSARD destination ID. This address is used to declare the destination address when creating command messages for the WiSARDNetwork. It should be written as hex and may include the leading '0x'.",
				true,
				true);
		
		ExperimentParameterObject destination_address_low_byte = new ExperimentParameterObject(
				"destination_address_low_byte",
				"0x85",
				"Destination Address (LOW Byte)",
				"Experiment Parameters",
				"This is the low byte value for the WiSARD destination ID. This address is used to declare the destination address when creating command messages for the WiSARDNetwork. It should be written as hex and may include the leading '0x'.",
				true,
				true);
		
		ExperimentParameterObject hub_address_high_byte = new ExperimentParameterObject(
				"hub_address_high_byte",
				"0x06",
				"Hub Address (HI Byte)",
				"Experiment Parameters",
				"This is the high byte value for the WiSARD hub ID. It should be written as hex and may include the leading '0x'.",
				true,
				true);
		
		ExperimentParameterObject hub_address_low_byte = new ExperimentParameterObject(
				"hub_address_low_byte",
				"0x80",
				"Destination Address (LOW Byte)",
				"Experiment Parameters",
				"This is the low byte value for the WiSARD hub ID. It should be written as hex and may include the leading '0x'.",
				true,
				true);
		
		ExperimentParameterObject processor_id = new ExperimentParameterObject(
				"processor_id",
				"0x00",
				"Processor ID",
				"Experiment Parameters",
				"This is the processor ID that is to receive the valve command. It should be written as hex and may include the leading '0x'.",
				true,
				true);
		ExperimentParameterObject timeout = new ExperimentParameterObject(
				"timeout",
				"300",
				"Timeout (in seconds)",
				"Experiment Parameters",
				"This is number of seconds to wait for a response before generating a new one.",
				true,
				true);

		getParameters().add(confMessage);
		getParameters().add(hub_address_high_byte);
		getParameters().add(hub_address_low_byte);
		
		getParameters().add(destination_address_high_byte);
		getParameters().add(destination_address_low_byte);
		
		
		getParameters().add(counter_channel);
		
		getParameters().add(upper_limit);
		getParameters().add(lower_limit);
		
		getParameters().add(command_value);
		
		
		
		getParameters().add(processor_id);
		getParameters().add(timeout);
		
	}
	/**
	 * @see ControlInterface
	 */
	@Override
	public void initRules() {
		super.initRules();
		getRules().add(new Rule("Debug Logging","If running, experiment will write to the local log file",true,new String[0]));	
		getRules().add(new Rule("Send Init Command","If running, the intial command is sent",false,new String[0]));	
		getRules().add(new Rule("Reset Counter Command","If running, the intial command is sent",false,new String[0]));	
		
		
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
		    		counter = Integer.parseInt(((String)getParameterById("lower_limit").getValue()));
		    		
					byte hub_high = (byte)Integer.parseInt(((String)getParameterById("hub_address_high_byte").getValue()).replace("0x", ""),16);
		    		byte hub_low = (byte)Integer.parseInt(((String)getParameterById("hub_address_low_byte").getValue()).replace("0x", ""),16);
		    		
					byte dest_high = (byte)Integer.parseInt(((String)getParameterById("destination_address_high_byte").getValue()).replace("0x", ""),16);
		    		byte dest_low = (byte)Integer.parseInt(((String)getParameterById("destination_address_low_byte").getValue()).replace("0x", ""),16);
		    		
		    		byte processor = (byte)Integer.parseInt(((String)getParameterById("processor_id").getValue()).replace("0x", ""),16);
		    		byte command = (byte)Integer.parseInt(((String)getParameterById("command_value").getValue()).replace("0x", ""),16);
		    		byte msgLenIndex = 10;
			        //Variables for storing fetched data and the command		    		
			        byte[] cmdMessage = 
			        	{
			        		//=======NET=======
			        		
			        		//DESTINATION ADDRESS [2]
			        		hub_high,//(Destination HI)
			        		hub_low,//(Destination LOW)
			        		//SOURCE ADDRESS [2]
			        		(byte)0xFE,//(Source)
			        		(byte)0xFE,
			        		
			        		//=======TRANSPORT=======
			        		
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
			        		
			        		//DATA ELEMENT ID [1]
			        		(byte)0x01, //(CMD packet)
			        		//DATA PAYLOAD LENGTH [1]
			        		(byte)0x09,
			        		//DATA ELEMENT VERSION [1]
			        		(byte)0x6E, //??
			        		
			        		//=======DATA ELEMENT PAYLOAD=======
			        		
			        		//COMMAND ID [2]
			        		processor, //Processor ID
			        		command, //
			        		(byte)0x00,
			        		(byte)0x00,
			        		(byte)0x00,
			        		(byte)0x00
			        	};
			        		
			        cmdMessage[msgLenIndex] = (byte) (cmdMessage.length+1);
			        
			        int counter_index = -1;
			        byte[][] counter_value;
			        byte[] counter_bytes;
			        boolean countUp = true;
			        SampleTimestampPackage stsp;
			        ChannelMap aMap = new ChannelMap();
			        counter_index = aMap.Add(getParameterToStringById("counter_channel"));
			        getSink().Subscribe(aMap);		        
			        long timeSent = System.currentTimeMillis();
			        
			        
			        /*
			         * Main Loop - continually checks sink channels for soil moisture status values, sends cmds as needed
			         */
			        
				        while(!Thread.currentThread().isInterrupted())
				        {   
						        	//Fetches new data
					        			if(getRule("Send Init Command").getIsRunning() || timeSent > System.currentTimeMillis()-Integer.parseInt(((String)getParameterById("timeout").getValue()))*1000){
									        
					        				getRule("Send Init Command").setIsRunning(false);
					        				counter_bytes = ByteBuffer.allocate(4).putInt(counter).array();
					        				
						            		for(int i = 0; i < 4; i++){
						            			cmdMessage[cmdMessage.length-4+i] = counter_bytes[i];
						            		}
						            		timeSent = System.currentTimeMillis();
						            		addCrcAndFlush(cmdMessage);									        
									        postStatusAsInt(new int[]{counter});
					        			}
					        			
					        			if(getRule("Reset Counter Command").getIsRunning()){
					        				counter = Integer.parseInt(((String)getParameterById("lower_limit").getValue()));
					        			}
					        			
					        			aMap = getSink().Fetch(1000);	
					        			
					        			
										if(!aMap.GetIfFetchTimedOut() && aMap.NumberOfChannels() > 0){
							            	counter_value = aMap.GetDataAsByteArray(counter_index);
							            	for(byte[] b : counter_value){
							            		stsp = DataGeneratorCodec.decodeValuePair(aMap.GetTimeStart(counter_index), b);
							            		if((int)stsp.getSample_data() >= Integer.parseInt((String)getParameterById("upper_limit").getValue())){
							            			countUp = false;
							            		}
							            		else if((int)stsp.getSample_data() <= Integer.parseInt((String)getParameterById("lower_limit").getValue())){
							            			countUp = true;
							            		}
							            		
							            		if(countUp){
							            			counter++;
							            		}
							            		else{
							            			counter--;
							            		}
							            		
							            		counter_bytes = ByteBuffer.allocate(4).putInt(counter).array();
							            		for(int i = 0; i < 4; i++){
							            			cmdMessage[cmdMessage.length-4+i] = counter_bytes[i];
							            		}
							            		
							            	
							            		timeSent = System.currentTimeMillis();			       
										        addCrcAndFlush(cmdMessage);									        
										        postStatusAsInt(new int[]{counter});
							            	}
										}
										else if(System.currentTimeMillis() - timeSent > Integer.parseInt((String)getParameterById("lower_limit").getValue())*1000){
											//If timeout, send again
											log.write("Timeout Reached - resending ("+counter+")");
					        				counter_bytes = ByteBuffer.allocate(4).putInt(counter).array();
					        				
						            		for(int i = 0; i < 4; i++){
						            			cmdMessage[cmdMessage.length-4+i] = counter_bytes[i];
						            		}
						            		timeSent = System.currentTimeMillis();
						            		addCrcAndFlush(cmdMessage);									        
									        postStatusAsInt(new int[]{counter});
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
				
			}
	    	catch(Exception e){
				updateParameter("runningStatus",notRunningStr);
	        	if(getRule("Debug Logging").getIsRunning()){
	        		StringWriter errors = new StringWriter();
                	e.printStackTrace(new PrintWriter(errors));
	        		log.write("Unhandled Exception, experiment operation will not be resumed");
	        		log.write(errors.toString());
	        		
	        	}
	        	
	        	
	        }
			}
		}
}
			

