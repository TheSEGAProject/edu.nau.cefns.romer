package experiments;
/*
Benchtop demo experiment that monitors a soil moisture channel streaming from a single-sampling WiSARD hub. The control algorithm will generate command packets that signal the WiSARD to turn on or off the irrigation valve given the soil moisture is outside of the threshold range specified.
 */


import helpers.ExperimentParameterObject;
import interfaces.ControlInterface;

import java.io.PrintWriter;
import java.io.StringWriter;

import utilities.ArrayUtilities;
import utilities.CRC;
import utilities.ControlClass;
import utilities.Experiment;
import utilities.Rule;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
/**
 * This class is a demo experiment that provides the framework to test and debug the command structure.
 * By default, none of the fields contain the necessary logic to begin actually issuing commands.
 * 
 * After the program is uploaded and compiled, enter the edit menu via experiment configuration 
 * in the segaWeb Portal. Change the values for lower and upper threshold as well as valve status
 * and command indication values. Then change the Sink Channel Soil Moisture - Name value to the 
 * channel that is to be monitored for the threshold (eg - "Spoof_Wisard_10/Triangle"). Then
 * scroll to the bottom of the page and hit reconnect to begin issuing commands. Also verify that
 * the ruleSMThreshold is running.
 * 
 * The description of the experiment is below:
 * 
 * Benchtop demo experiment that monitors a soil moisture channel streaming from a single-sampling 
 * WiSARD hub. The control algorithm will generate command packets that signal the WiSARD to turn on 
 * or off the irrigation valve given the soil moisture is outside of the threshold range specified.
 * 
 * @author jdk85
 *
 */
public class BenchtopDemoSMV2 extends ControlClass implements java.io.Serializable  { 


	

	private static final long serialVersionUID = 1194186383934964195L;
	private static String confirmation = "Last Edit: 06/16/2014 - 4:08 PM";

	/**
	 * DemoExp Constructor
	 * The constructor simply calls the parent constructor
	 * @param expID
	 */
	public BenchtopDemoSMV2(String expID, String expDesc, String sinkServerAddress,String sourceServerAddress,boolean init) {
		super(expID,expDesc,sinkServerAddress,sourceServerAddress,init);
	}
	/**
	 * This method overrides the parent ControlClass method initParams() to add experiment-specific parameters.
	 * When the experiment is uploaded or compiled after a Tomcat restart, it calls the parent initParams() 
	 * method to initialize general experiment parameters and then will initialize its own parameters
	 * 
	 * The parameters themselves are stored in an array list owned by the ControlClass parent class that 
	 * allows all the parameters pertaining to this experiment to be accessed in a single location via 
	 * the web portal 
	 * 
	 * @see ExperimentParameterObject
	 * @see ControlClass
	 * @see Experiment
	 */
	@Override
	public void initParams(){
		super.initParams();
		ExperimentParameterObject confMessage = new ExperimentParameterObject("confMessage",confirmation,"Confirmation Message","ExperimentParameters","The message parameter is used to verify that the correct version of the Java source code was uploaded",false,false);
		
		ExperimentParameterObject lowerSM_threshold = new ExperimentParameterObject("lowerSM_threshold","10","Lower Soil Moisture Threhold","Experiment Parameters","This parameter sets the lower threshold value for the soil moisture. If the sensor reading drops below this value, a 'Valve ON' command is generated and sent.",true,false);
		ExperimentParameterObject upperSM_threshold = new ExperimentParameterObject("upperSM_threshold","20","Upper Soil Moisture Threhold","Experiment Parameters","This parameter sets the upper threshold value for the soil moisture. If the sensor reading is above this value, a 'Valve OFF' command is generated and sent.",true,false);
		
		ExperimentParameterObject valveStatus_On = new ExperimentParameterObject("valveStatus_On","1","Valve Status ON","Experiment Parameters","This parameter defines what value to check for on the valve status channel to determine if the valve is already ON",true,false);
		ExperimentParameterObject valveStatus_Off = new ExperimentParameterObject("valveStatus_Off","0","Valve Status OFF","Experiment Parameters","This parameter defines what value to check for on the valve status channel to determine if the valve is already OFF",true,false);
		
		ExperimentParameterObject commandView_On = new ExperimentParameterObject("commandView_On","1","Command Status ON","Experiment Parameters","When an ON command is generated, this value is sent to the 'Command View' channel. This is mostly used for debugging and verification and has no impact on the actual control.",true,false);
		ExperimentParameterObject commandView_Off = new ExperimentParameterObject("commandView_Off","0","Command Status OFF","Experiment Parameters","When an OFF command is generated, this value is sent to the 'Command View' channel. This is mostly used for debugging and verification and has no impact on the actual control.",true,false);
		
		ExperimentParameterObject wisard_channel = new ExperimentParameterObject("wisard_channel","exp249a-03_channelized/wisard_1641/","WiSARD Channel Name","Experiment Parameters","This is the full channel name for the WiSARD to check sample data, note that it does not include the SP or transducer ID.",true,false);
		ExperimentParameterObject soil_moisture_sensor_channel = new ExperimentParameterObject("soil_moisture_sensor_channel","sp_2/transducer_3","Soil Moisture Channel Name","Experiment Parameters","This is the transducer name for the channelized RBNB data. It is concatenated with the WiSARD channel name to when data is actually fetched",true,false);
		
		ExperimentParameterObject destination_address_high_byte = new ExperimentParameterObject("destination_address_high_byte","0x06","Destination Address (HI Byte)","Experiment Parameters","This is the high byte value for the WiSARD destination ID. This address is used to declare the destination address when creating command messages for the WiSARDNetwork. It should be written as hex and may include the leading '0x'.",true,true);
		ExperimentParameterObject destination_address_low_byte = new ExperimentParameterObject("destination_address_low_byte","0x69","Destination Address (LOW Byte)","Experiment Parameters","This is the low byte value for the WiSARD destination ID. This address is used to declare the destination address when creating command messages for the WiSARDNetwork. It should be written as hex and may include the leading '0x'.",true,true);
		
		getParameters().add(confMessage);
		
		getParameters().add(lowerSM_threshold);
		getParameters().add(upperSM_threshold);
		
		getParameters().add(valveStatus_On);
		getParameters().add(valveStatus_Off);
		
		getParameters().add(commandView_On);
		getParameters().add(commandView_Off);
		
		getParameters().add(wisard_channel);
		getParameters().add(soil_moisture_sensor_channel);
		
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
				" is not running, then the experiment won't be running."
				,true,new String[0]));
		getRules().add(new Rule("reconnectOnTimeout","If this rule is running, the control will automatically try to " +
				"reconnect on exceptions. If this rule is reset, manually reconnect to resume normal operation."
				,true,new String[0]));
		String [] channels = {(String)getParameterById("wisard_channel").getValue()+(String)getParameterById("soil_moisture_sensor_channel").getValue()};
		getRules().add(new Rule("ruleSMThreshold","This rule checks to see if the soil moisture " +
				"value is between a certain threshold and will then issue a command if necessary"
				,true,channels));
		
		
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
			        byte[][] smValue;
			        int STM_SM_One_Val;
			        int STM_SM_Temp;
			        
			       
			        ChannelMap aMap = new ChannelMap();
			        int aMapIndexSM = aMap.Add((String)getParameterById("wisard_channel").getValue()+(String)getParameterById("soil_moisture_sensor_channel").getValue());
			        getSink().Subscribe(aMap);
			        
			        /*
			         * Main Loop - continually checks sink channels for soil moisture status values, sends cmds as needed
			         */
			        
				        while(!Thread.currentThread().isInterrupted())
				        {   
				        	try{
					        	if(getRule("ruleSMThreshold").getIsRunning()){
						        	//Fetches new data
					        		
					        			boolean sendCmd = false;
							            aMap = getSink().Fetch(10000);				       					
										if(!aMap.GetIfFetchTimedOut()){
							            	byte[] cmdSent = new byte[1];
							            	if(aMapIndexSM != -1){
							            		smValue = aMap.GetDataAsByteArray(aMapIndexSM);
							            		
							            		//Convert the raw soil moisture value
							            		STM_SM_One_Val = ((smValue[0][8] << 8) + smValue[0][9])/50;
							            		
							            		//Convert the raw temperature value to degrees C
							            		
							            		STM_SM_Temp = (smValue[0][10] << 8) + smValue[0][11];
							            		if(STM_SM_Temp > 900) STM_SM_Temp = 900 + 5*(STM_SM_Temp - 900);
							            		STM_SM_Temp = (STM_SM_Temp - 400)/10;
							            			
							            		
							            		if(getRule("Debug Logging").getIsRunning())log.write("SM Value: " + STM_SM_One_Val);
							            		if(getRule("Debug Logging").getIsRunning())log.write("5TM Temp: " + STM_SM_Temp);
								            	
							            		
							            		
								            	//if soil moisture is higher than upper threshold
								            	//turn off the valve
								            	//check if feasible first (81.88 should be max val)
							            	    if(STM_SM_One_Val >= 0 && STM_SM_One_Val < 82){
							            	    	
								            	    if(STM_SM_One_Val >= Integer.parseInt((String)getParameterById("upperSM_threshold").getValue())){
								            	    	cmdFinal[17] = (byte)0x00; //Valve OFF            		
									            		//if(getRule("Debug Logging").getIsRunning())log.write("Valve OFF Cmd");
									        			cmdSent[0] = Byte.parseByte((String) getParameterById("commandView_Off").getValue());
									            		sendCmd = true;						            		
									            	}
							            	    
								            	
									            	//if soil moisture is less than the lower threshold and the valve is currently off
									            	//turn on the valve
									            	if(STM_SM_One_Val <= Integer.parseInt((String)getParameterById("lowerSM_threshold").getValue()) ){
									            		cmdFinal[17] = (byte)0x5B; //Valve ON
									            		//if(getRule("Debug Logging").getIsRunning())log.write("Valve ON Cmd");
									            		cmdSent[0] = Byte.parseByte((String) getParameterById("commandView_On").getValue());
									            		sendCmd = true;
									            	}
							            	    }
							            	    else{
							            	    	if(getRule("Debug Logging").getIsRunning())log.write("SMS to Paul Heinrich");
							            	    }
								            }
							            	
							            	//command is only sent if it needs to be... this allows the loop to iterate through
							            	//multiple rules and append commands to a single packet if the experiment allows for this
							            	if(sendCmd)command(cmdFinal);
										}			        	
					        	}
					        }catch(SAPIException e){
					        	//This breaks the while loop and is caught by the outer try/catch
					        	//The reason for doing this is so that any other type of exceptions are caught
					        	//within the while loop so that the error can be logged and the experiment can continue on
				        		throw e;
				        	}catch(Exception e){
				        		StringWriter err = new StringWriter();
					        	e.printStackTrace(new PrintWriter(err));
					        	if(getRule("Debug Logging").getIsRunning())log.write(err);	
				        	}
			        }
	    		}
	    	}catch(SAPIException e){
	    		StringWriter err = new StringWriter();
	        	e.printStackTrace(new PrintWriter(err));
	        	if(getRule("Debug Logging").getIsRunning())log.write(err);
	        	if(getRule("reconnectOnTimeout").getIsRunning()){
		    		try {
						Thread.sleep(10000);
						reconnect();
					} catch (InterruptedException e1) {
						StringWriter errors = new StringWriter();
			        	e1.printStackTrace(new PrintWriter(errors));
			        	if(getRule("Debug Logging").getIsRunning())log.write(errors);
					}
	        	}
	    	}catch(ArrayIndexOutOfBoundsException e){
    			StringWriter err = new StringWriter();
	        	e.printStackTrace(new PrintWriter(err));	        	
	        	if(getRule("Debug Logging").getIsRunning())log.write(err);
	    	}catch(NullPointerException e){
	    		StringWriter err = new StringWriter();
	        	e.printStackTrace(new PrintWriter(err));	        	
	        	if(getRule("Debug Logging").getIsRunning())log.write(err);
	        	if(getRule("Debug Logging").getIsRunning())log.write("Most likely thrown by getParemeterById(), check configuration");
	        	
	        }catch(Exception e){
	    		StringWriter err = new StringWriter();
	        	e.printStackTrace(new PrintWriter(err));
	        	if(getRule("Debug Logging").getIsRunning())log.write(err);
	        	
	        }
		}
	}

	
}