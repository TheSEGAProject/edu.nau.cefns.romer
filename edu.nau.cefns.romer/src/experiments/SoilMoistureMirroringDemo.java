package experiments;
/*
This experiment monitors a specified soil moisture channel and tries to mirror that value to another. An allowable range is set that determines whether commands need to be sent. This initial version does not check for valve status.
 */

import helpers.ExperimentParameterObject;
import interfaces.ControlInterface;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import utilities.ArrayUtilities;
import utilities.CRC;
import utilities.ControlClass;
import utilities.Experiment;
import utilities.Rule;

import com.rbnb.sapi.ChannelMap;
/**
 * @author jdk85
 *
 */
public class SoilMoistureMirroringDemo extends ControlClass implements java.io.Serializable  { 


	

	private static final long serialVersionUID = 1194186383934964195L;
	private static String confirmation = "Last Edit: 07/18/2014 - 1:18 PM";
	/**
	 * DemoExp Constructor
	 * The constructor simply calls the parent constructor
	 * @param expID
	 */
	public SoilMoistureMirroringDemo(String expID, String expDesc, String sinkServerAddress,String sourceServerAddress,boolean init) {
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
		
		ExperimentParameterObject mirror_server = new ExperimentParameterObject(
				"mirror_server",
				"exp249a-03.egr.nau.edu:3333",
				"Garden Server Address",
				"Experiment Parameters",
				"This parameter defines the garden server address that the control source channel is to be mirrored to.",
				true,
				true);
		ExperimentParameterObject mirror_source_name = new ExperimentParameterObject(
				"mirror_source_name",
				"ControlSource",
				"Garden Server Source Name",
				"Experiment Parameters",
				"This parameter sets the channel name as it will appear to the garden server (typically 'ControlSource')",
				true,
				true);
		
		ExperimentParameterObject allowable_sm_threshold = new ExperimentParameterObject(
				"allowable_sm_threshold",
				"5",
				"Allowable +/- Soil Moisture Threhold",
				"Experiment Parameters",
				"This parameter sets the allowable range (+/-) value for the control to generate a command. If the sensor reading on the mirror sensor drops below the sensor reading on the original sensor by the range value, a 'Valve ON' command is generated and sent. Otherwise, if the mirror sensor rises above the original sensor value by the range value, then a 'Valve OFF' command is sent.",
				true,
				false);
		
		ExperimentParameterObject valveStatus_On = new ExperimentParameterObject(
				"valveStatus_On",
				"1",
				"Valve Status ON",
				"Experiment Parameters",
				"This parameter defines what value to check for on the valve status channel to determine if the valve is already ON",
				true,
				false);
		
		ExperimentParameterObject valveStatus_Off = new ExperimentParameterObject(
				"valveStatus_Off",
				"0",
				"Valve Status OFF",
				"Experiment Parameters",
				"This parameter defines what value to check for on the valve status channel to determine if the valve is already OFF",
				true,
				false);
		
		ExperimentParameterObject commandView_On = new ExperimentParameterObject(
				"commandView_On",
				"1",
				"Command Status ON",
				"Experiment Parameters",
				"When an ON command is generated, this value is sent to the 'Command View' channel. This is mostly used for debugging and verification and has no impact on the actual control.",
				true,
				false);
		
		ExperimentParameterObject commandView_Off = new ExperimentParameterObject(
				"commandView_Off",
				"0",
				"Command Status OFF",
				"Experiment Parameters",
				"When an OFF command is generated, this value is sent to the 'Command View' channel. This is mostly used for debugging and verification and has no impact on the actual control.",
				true,
				false);
		
		ExperimentParameterObject wisard_channel_orig = new ExperimentParameterObject(
				"wisard_channel_orig",
				"exp249a-03_channelized/wisard_1645/soilmoisture",
				"WiSARD Channel Original",
				"Experiment Parameters",
				"This is the channel that wisard_1643 is trying to mimic.",
				true,
				false);
		

		ExperimentParameterObject wisard_channel_mirror = new ExperimentParameterObject(
				"wisard_channel_mirror",
				"exp249a-03_channelized/wisard_1643/soilmoisture",
				"WiSARD Channel Mirror",
				"Experiment Parameters",
				"This is the channel that mimics wisard_1645.",
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
				"0x6B",
				"Destination Address (LOW Byte)",
				"Experiment Parameters",
				"This is the low byte value for the WiSARD destination ID. This address is used to declare the destination address when creating command messages for the WiSARDNetwork. It should be written as hex and may include the leading '0x'.",
				true,
				true);
		
		
		
		getParameters().add(confMessage);
		
		getParameters().add(mirror_server);
		getParameters().add(mirror_source_name);
		
		getParameters().add(allowable_sm_threshold);
		
		getParameters().add(valveStatus_On);
		getParameters().add(valveStatus_Off);
		
		getParameters().add(commandView_On);
		getParameters().add(commandView_Off);
		
		getParameters().add(wisard_channel_orig);
		getParameters().add(wisard_channel_mirror);
		
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
		getRules().add(new Rule("runExperimentControl","This rule controls the entire experiment. If it" +
				" is not running, then the experiment won't be running.",
				true,new String[0]));
		getRules().add(new Rule("reconnectOnTimeout","If this rule is running, the control will automatically try to " +
				"reconnect on exceptions. If this rule is reset, manually reconnect to resume normal operation.",
				true,new String[0]));
		getRules().add(new Rule("ruleSMThreshold","This rule checks to see if the soil moisture " +
				"value is between a certain threshold and will then issue a command if necessary",
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
	    		if(getRule("runExperimentControl").getIsRunning()){
	    			if(getRule("Debug Logging").getIsRunning())log.write("Run() called while experiment rule running");
		    		connect();
		    		
					byte dest_high = (byte)Integer.parseInt(((String)getParameterById("destination_address_high_byte").getValue()).replace("0x", ""),16);
		    		byte dest_low = (byte)Integer.parseInt(((String)getParameterById("destination_address_low_byte").getValue()).replace("0x", ""),16);
		    		byte msgLenIndex = 10;
			        //Variables for storing fetched data and the command		    		
			        byte[] cmdMessage = 
			        	{
			        		//=======NET=======
			        		
			        		//DESTINATION ADDRESS [2]
			        		dest_high,//(Destination HI)
			        		dest_low,//(Destination LOW)
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
			        		(byte)0x03, //(Processor ID)
			        		(byte)0x04, //(Valve CMD)
			        		//PARAMETERS [2]
			        		(byte)0x00,
			        		(byte)0x00,//ON - 0x5B, OFF - 0x00
			        		(byte)0x00,
			        		(byte)0x00
			        	};
			        		
			        cmdMessage[msgLenIndex] = (byte) (cmdMessage.length+1);
			        int crc = CRC.compute_crc(ArrayUtilities.convert_to_int_array(cmdMessage));
			        byte[] cmdFinal = new byte[cmdMessage.length+2];
			        byte crc_hi_byte = (byte) (crc >> 8);
			        byte crc_low_byte = (byte) (crc & 0xFF);
			        
			        for(int i = 0; i < cmdMessage.length; i++){
			        	cmdFinal[i] = cmdMessage[i];
			        }
			        cmdFinal[cmdFinal.length-2] = crc_hi_byte;
			        cmdFinal[cmdFinal.length-1] = crc_low_byte;
			        
			        //smValue is rule specific - it is just a place holder for soil moisture data
			        byte[][] origSMVal,mirrorSMVal;
			        int STM_SM_Orig_Val = -1,STM_SM_Mirror_Val = -1;
			        //int STM_SM_Temp;
			        
			       
			        ChannelMap aMap = new ChannelMap();
			        int origSMIndex = aMap.Add((String)getParameterById("wisard_channel_orig").getValue());
			        int mirrorSMIndex = aMap.Add((String)getParameterById("wisard_channel_mirror").getValue());
			        getSink().Subscribe(aMap);	
			        
			        /*
			         * Main Loop - continually checks sink channels for soil moisture status values, sends cmds as needed
			         */
			        
				        while(!Thread.currentThread().isInterrupted())
				        {   
					        	if(getRule("ruleSMThreshold").getIsRunning()){
						        	//Fetches new data
					        			boolean sendCmd = false;
					        			aMap = getSink().Fetch(10000);		       					
										if(!aMap.GetIfFetchTimedOut()){
							            	byte[] cmdSent = new byte[1];
							            	if((origSMIndex = aMap.GetIndex((String)getParameterById("wisard_channel_orig").getValue())) != -1){
							            		//Store the data point
							            		origSMVal = aMap.GetDataAsByteArray(origSMIndex);
							            		//Convert the raw soil moisture value
							            		STM_SM_Orig_Val = ((origSMVal[0][8] << 8) + origSMVal[0][9])/50;
							            		if(getRule("Debug Logging").getIsRunning())log.write("SM Orig Value: " + STM_SM_Orig_Val);
							            	}
							            	if((mirrorSMIndex = aMap.GetIndex((String)getParameterById("wisard_channel_mirror").getValue())) != -1){
							            		//Store the data point
							            		mirrorSMVal = aMap.GetDataAsByteArray(mirrorSMIndex);
							            		//Convert the raw soil moisture value
							            		STM_SM_Mirror_Val = ((mirrorSMVal[0][8] << 8) + mirrorSMVal[0][9])/50;
							            		if(getRule("Debug Logging").getIsRunning())log.write("SM Mirror Value: " + STM_SM_Mirror_Val);
							            	}
							            			//if soil moisture is higher than upper threshold
									            	//turn off the valve
									            	//check if feasible first (81.88 should be max val)
								            	    if((STM_SM_Orig_Val >= 0 && STM_SM_Orig_Val < 82) && (STM_SM_Mirror_Val >= 0 && STM_SM_Mirror_Val < 82)){
								            	    	
									            	    if(STM_SM_Mirror_Val >= (STM_SM_Orig_Val + Integer.parseInt((String)getParameterById("allowable_sm_threshold").getValue()))){
									            	    	cmdFinal[17] = (byte)0x00; //Valve OFF            		
										            		//if(getRule("Debug Logging").getIsRunning())log.write("Valve OFF Cmd");
										        			cmdSent[0] = Byte.parseByte((String) getParameterById("commandView_Off").getValue());
										            		sendCmd = true;						            		
										            	}
								            	    
									            	
										            	//if soil moisture is less than the lower threshold and the valve is currently off
										            	//turn on the valve
									            	    if(STM_SM_Mirror_Val <= (STM_SM_Orig_Val - Integer.parseInt((String)getParameterById("allowable_sm_threshold").getValue()))){
										            		cmdFinal[17] = (byte)0x5B; //Valve ON
										            		//if(getRule("Debug Logging").getIsRunning())log.write("Valve ON Cmd");
										            		cmdSent[0] = Byte.parseByte((String) getParameterById("commandView_On").getValue());
										            		sendCmd = true;
										            	}
								            	    }
								            	    else{
								            	    	if(getRule("Debug Logging").getIsRunning())log.write("SMS to Paul Heinrich");
								            	    }
							            		
								            
							            	
							            	//command is only sent if it needs to be... this allows the loop to iterate through
							            	//multiple rules and append commands to a single packet if the experiment allows for this
							            	if(sendCmd){
										        crc = CRC.compute_crc(ArrayUtilities.convert_to_int_array(Arrays.copyOfRange(cmdFinal,0,cmdFinal.length-2)));
										        crc_hi_byte = (byte) (crc >> 8);
										        crc_low_byte = (byte) (crc & 0xFF);
										        
										        
										        cmdFinal[cmdFinal.length-2] = crc_hi_byte;
										        cmdFinal[cmdFinal.length-1] = crc_low_byte;
										        
							            		command(cmdFinal);
							            	}
										}
					        		}
					        	
				        }
					        	
					 
	    		}
	    	}catch(Exception e){
	    		StringWriter err = new StringWriter();
	        	e.printStackTrace(new PrintWriter(err));
	        	if(getRule("Debug Logging").getIsRunning())log.write(err);
	        	if(getRule("reconnectOnTimeout").getIsRunning()){
		    		try {
						Thread.sleep(10000);
						if(isRBNBConnected())
							restartThread();						
						else
							reconnect();
					} catch (InterruptedException e1) {
						StringWriter errors = new StringWriter();
			        	e1.printStackTrace(new PrintWriter(errors));
			        	if(getRule("Debug Logging").getIsRunning())log.write(errors);
					}
	        	}
	        	
	        }
	    }
		
	}

}
