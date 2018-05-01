package experiments;
/*
This experiment monitors a specified soil moisture channel and tries to mirror that value to another. An allowable range is set that determines whether commands need to be sent. This initial version does not check for valve status.
 */

import helpers.ExperimentParameterObject;
import interfaces.ControlInterface;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;

import utilities.ControlClass;
import utilities.Experiment;
import utilities.Rule;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
/**
 * @author jdk85
 *
 */
public class LFSR_Experiment extends ControlClass implements java.io.Serializable  { 


	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String confirmation = "Last Edit: 08/26/2014 - 1:30 PM";
	private int attempts = 0;
	/**
	 * DemoExp Constructor
	 * The constructor simply calls the parent constructor
	 * @param expID
	 */
	public LFSR_Experiment(String expID, String expDesc, String sinkServerAddress,String sourceServerAddress,boolean init) {
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
				"exp249a-03_channelized/wisard_1648/proc_2/dg_1",
				"Source Channel Name to Monitor",
				"Experiment Parameters",
				"This parameter specifies the channel to monitor",
				true,
				false);
		
		
		
		
		getParameters().add(confMessage);
		
		getParameters().add(monitor_channel_name);
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
			       
			        ChannelMap aMap = new ChannelMap();
			        int monitorChannelIndex = aMap.Add((String)getParameterById("monitor_channel_name").getValue());
			        getSink().Subscribe(aMap);
			        int[] data_val,seq_nums;
			        seq_nums = new int[21];
			        Arrays.fill(seq_nums, -1);
			        
			        int sequence_number = -1;
			        Date wis_time;
			        
			        long failCount = 0;
			        long packetCount = 0;
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
											packetCount++;
											wis_time = new Date((long)(data_val[0] + 946710000)*1000);
											sequence_number = data_val[1];											
											if(seq_nums[10] == -1){
												seq_nums[10] = sequence_number;
												postStatusAsString("Initialized");
											}
											else if(sequence_number > seq_nums[10] && sequence_number < seq_nums[10] + 10){
												int temp = seq_nums[10];
												seq_nums[(10 - (sequence_number-temp))] = sequence_number;
												for(int i = 0; i < sequence_number - temp; i++){
													if(seq_nums[20] < 0 ){
														postStatusAsString("FAILURE [" + wis_time.toString() + "] Lost Packet (5 minute timeout reached)\t"+ ++failCount + "/" + packetCount + " packets lost");				
													}																							
													System.arraycopy(seq_nums, 0, seq_nums, 1, 20);
													seq_nums[0] = - 1;
													
												}							
											}
											else if(sequence_number < seq_nums[10] && sequence_number > seq_nums[10] - 10){
												int temp = seq_nums[10] - sequence_number;
												seq_nums[10+temp] = sequence_number;
												
											}									
											else{
												postStatusAsString("FAILURE [" + wis_time.toString() + "] Received value outside of allowable range\t"+ ++failCount + "/" + packetCount + " packets lost");												
											}
										
										}
										else 
											log.write("Data_val not length 2");
					            		
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
			
		public int fetchNextNumber(int currentSeed){
			
			String seedString = String.format("%32s", Integer.toBinaryString(currentSeed)).replace(' ', '0');			
			int tempBit1 = seedString.charAt(0)^seedString.charAt(1);
			int tempBit2 = seedString.charAt(21)^seedString.charAt(31);
			int oneBit = tempBit1^tempBit2;
			oneBit = oneBit^1;
			currentSeed =  (currentSeed >> 1);
			currentSeed = (oneBit == 0) ? currentSeed & ~(1 << 31) : currentSeed | (1 << 31);
			//System.out.println("0x"+Integer.toHexString(currentSeed).toUpperCase());
			return currentSeed;
		}
		
	}

}
