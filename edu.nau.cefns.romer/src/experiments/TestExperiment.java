package experiments;



import helpers.ExperimentParameterObject;

import java.io.PrintWriter;
import java.io.StringWriter;

import utilities.ControlClass;
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
 * channel that is to be monitored for the threshold (ie - "Spoof_Wisard_10/Triangle"). Then
 * scroll to the bottom of the page and hit reconnect to begin issuing commands. Also verify that
 * the ruleSMThreshold is running.
 * 
 * @author jdk85
 *
 */
public class TestExperiment extends ControlClass implements java.io.Serializable  { 


	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1194186383934964195L;
	

	/**
	 * DemoExp Constructor
	 * The constructor simply calls the parent constructor and then initializes the rule list
	 * @param expID
	 */
	public TestExperiment(String expID, String expDesc, String sinkServerAddress,String sourceServerAddress) {
		super(expID,expDesc,sinkServerAddress,sourceServerAddress,false);
		initRules();
	}
	public void initParams(){		
		
		ExperimentParameterObject lowerSM_threshold = new ExperimentParameterObject("lowerSM_threshold","10","Lower Soil Moisture Threhold","","",true,false);
		ExperimentParameterObject upperSM_threshold = new ExperimentParameterObject("upperSM_threshold","20","Upper Soil Moisture Threhold","","",true,false);
		
		ExperimentParameterObject valveStatus_On = new ExperimentParameterObject("valveStatus_On","1","Valve Status ON","","",true,false);
		ExperimentParameterObject valveStatus_Off = new ExperimentParameterObject("valveStatus_Off","0","Valve Status OFF","","",true,false);
		
		ExperimentParameterObject commandView_On = new ExperimentParameterObject("commandView_On","1","Command Status ON","","",true,false);
		ExperimentParameterObject commandView_Off = new ExperimentParameterObject("commandView_Off","0","Command Status OFF","","",true,false);
		
		ExperimentParameterObject soilMoisture_channel = new ExperimentParameterObject("soilMoisture_channel","src_channelizer/wisard_0/soil_moisture","Soil Moisture Channel Name","","",true,false);
		ExperimentParameterObject valveStatus_channel = new ExperimentParameterObject("valveStatus_channel","src_channelizer/wisard_0/valve_status","Valve Status Channel Name","","",true,false);

		getParameters().add(lowerSM_threshold);
		getParameters().add(upperSM_threshold);
		
		getParameters().add(valveStatus_On);
		getParameters().add(valveStatus_Off);
		
		getParameters().add(commandView_On);
		getParameters().add(commandView_Off);
		
		getParameters().add(soilMoisture_channel);
		getParameters().add(valveStatus_channel);
	}
	/**
	 * This method is used whenever an experiment first start or is restarted.
	 * Depending on the manner in which rules can be added, this method will most likely have to be changed
	 */
	@Override
	public void initRules() {
		String [] channels = {(String)getParameterById("soilMoisture_channel").getValue()};
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
		return "SM Example Using ExperimentParameter Objects - Last Edit: 4/24/2014 - 11:26PM";
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
	    		connect();
		        //Variables for storing fetched data and the command
		        byte[] cmdMessage = 
		        	{
		        		//=======NET=======
		        		//DESTINATION ADDRESS [2]
		        		(byte)0x00,//(Destination)
		        		(byte)0x00,
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
		        		(byte)0x00,
		        		(byte)0x00,//(WiSARD ID)
		        		//MESSAGE PAYLOAD LENGTH [1]
		        		(byte)0x09,
		        		
		        		//=======MESSAGE PAYLOAD=======
		        		
		        		//DATA ELEMENT ID [1]
		        		(byte)0x01, //(CMD packet)
		        		//DATA PAYLOAD LENGTH [1]
		        		(byte)0x06,
		        		//DATA ELEMENT VERSION [1]
		        		(byte)0x6E, //??
		        		
		        		//=======DATA ELEMENT PAYLOAD=======
		        		
		        		//COMMAND ID [2]
		        		(byte)0x02, //(SP Board Position)
		        		(byte)0x04, //(Valve CMD)
		        		//PARAMETERS [2]
		        		(byte)0x00,
		        		(byte)0x00,//ON - 0x5B, OFF - 0x00
		        		(byte)0x00,
		        		(byte)0x00
		        	};
		        		
		        	
		        //smValue is rule specific - it is basically a place holder for soil moisture data
		        int[] smValue;
		        
            	
		        ChannelMap aMap = new ChannelMap();
		        aMap.Add((String)getParameterById("soilMoisture_channel").getValue());
		        getSink().Subscribe(aMap);
		        
		        /*
		         * Main Loop - continually checks sink channels for soil moisture status values, sends cmds as needed
		         */
		        
			        while(!Thread.currentThread().isInterrupted())
			        {   
			        	
			        	if(getRule("ruleSMThreshold").getIsRunning()){
				        	//Fetches new data
			        		
				            aMap = getSink().Fetch(10000);
				            int aMapIndexSM = aMap.GetIndex((String)getParameterById("soilMoisture_channel").getValue());						
							if(!aMap.GetIfFetchTimedOut()){
								boolean sendCmd = false;
				            	byte[] cmdSent = new byte[1];
				            	if(aMapIndexSM != -1){
				            		smValue = aMap.GetDataAsInt32(aMapIndexSM);
				            		
					            	//if soil moisture is more than upper threshold and the valve is currently on
					            	//turn off the valve
					            	//check if feasible first (81.88 should be max val)
				            	    if(smValue[0] >= 0 && smValue[0] <= 82){
					            	    if(smValue[0] > Integer.parseInt((String)getParameterById("upperSM_threshold").getValue())){
						            		cmdMessage[17] = (byte)0x00; //Valve OFF            		
						            		
						        			cmdSent[0] = Byte.parseByte((String) getParameterById("commandView_Off").getValue());
						            		sendCmd = true;						            		
						            	}
				            	    
					            	
						            	//if soil moisture is less than the lower threshold and the valve is currently off
						            	//turn on the valve
						            	if(smValue[0] < Integer.parseInt((String)getParameterById("lowerSM_threshold").getValue())){
						            		cmdMessage[17] = (byte)0x5B; //Valve ON
						
						            		cmdSent[0] = Byte.parseByte((String) getParameterById("commandView_On").getValue());
						            		sendCmd = true;
						            	}
				            	    }
					            }
				            	//command is only sent if it needs to be... this allows the loop to check any possible actions it
			            		//must perform and package and send them all as a single message (basically by ORing the type and updating the data array)
			            	if(sendCmd)command(cmdMessage);
							}
				        }
		        }
	    	}catch(SAPIException e){
	    		StringWriter err = new StringWriter();
	        	e.printStackTrace(new PrintWriter(err));
	        	log.write(err);
	    		try {
					Thread.sleep(10000);
					reconnect();
				} catch (InterruptedException e1) {
					StringWriter errors = new StringWriter();
		        	e1.printStackTrace(new PrintWriter(errors));
		        	log.write(errors);
				}
	    	}catch(NullPointerException e){
	    		StringWriter err = new StringWriter();
	        	e.printStackTrace(new PrintWriter(err));	        	
	        	log.write(err);
	        	log.write("Most likely thrown by getParemeterById(), check configuration");
	        	
	        }catch(Exception e){
	    		StringWriter err = new StringWriter();
	        	e.printStackTrace(new PrintWriter(err));
	        	log.write(err);
				
	        }
		}
	}

	
}




