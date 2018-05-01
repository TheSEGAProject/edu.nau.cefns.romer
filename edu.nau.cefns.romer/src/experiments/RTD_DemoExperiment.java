package experiments;

import java.io.PrintWriter;
import java.io.StringWriter;

import utilities.ControlClass;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;



public class RTD_DemoExperiment extends ControlClass  {


	/**
	 * 
	 */
	private static final long serialVersionUID = -5349623644000720513L;


	public RTD_DemoExperiment(String expID) {
		super(expID);
	}
	
	@Override
	public String getMessage() {
		return "Demo Experiment - Last Edit: 07/9/2013 - 3:30 PM";
	}
	
	@Override
	protected RBNBControl createRBNBControl(){
		return new RBNBControl();
	}


		public class RBNBControl extends ControlClass.RBNBControl{
			public int findInArray(String[] arr, String value){
				
				for(int i = 0; i < arr.length; i++){
					if(arr[i].equalsIgnoreCase(value)) return i;
				}
				return -1;
			}
			@Override
			public void run(){
	    	try{ 
	    		connect();
		        //Variables for storing fetched data and the command
		        byte[] data = new byte[16];	
		        
		        ChannelMap aMap = new ChannelMap();
		        String temp = "rtd_channelizer/uniqueID";
		        aMap.Add(temp);
		        getSink().Subscribe(aMap);
		        /*
		         * Main Loop - continually checks sink channels for soil moisture status values, sends cmds as needed
		         */
		        	log.write("Entering Thread");
			        while(!Thread.currentThread().isInterrupted())
			        {   
			        	//Zero out data array
			        	for(int i = 0; i < data.length; i++){
			        		data[i] = (byte)0x00;
			        	}
			        	
			            
			            //Fetches new data
			            aMap = getSink().Fetch(10);
						
						if(!aMap.GetIfFetchTimedOut()){
			            	
			            	byte	cmdType = 0;
			            	int[] cmdSent = {33};
			            	long[] id = aMap.GetDataAsInt64(aMap.GetIndex(temp));
			            	if(id.length > 0){
			            		
			            		
				            	short[] timestamp = new short[8];
				                // Convert to a byte array
				                byte[] bytes = Long.toHexString(id[0]).toUpperCase().getBytes();
				                byte[] newBytes = new byte[16];
				                int lenDiff = newBytes.length - bytes.length;
				                // Convert to byte array
				                for(int i = 0; i < bytes.length; i++) {
				                        if(bytes[i] >= 65) newBytes[i+lenDiff] = (byte)(bytes[i] - 55);
				                        else newBytes[i+lenDiff] = (byte)(bytes[i] - 48);
				                }
				                for(int i = 0; i < timestamp.length; i++) {
				                        timestamp[i] = (short)((newBytes[2*i] << 4) + newBytes[(2*i)+1]);
				                        data[i] = (byte)timestamp[i];
				                }
				            	
				            	//command is only sent if it needs to be... this allows the loop to check any possible actions it
				            		//must perform and package and send them all as a single message (basically by ORing the type and updating the data array)
				            	//command(data,cmdType,cmdSent);
			            	}
			
			            }
		           
		        }
	    	}catch(SAPIException e){
	    		StringWriter err = new StringWriter();
	        	e.printStackTrace(new PrintWriter(err));
	        	log.write(e.toString());
	    		try {
					Thread.sleep(10000);
					reconnect();
				} catch (InterruptedException e1) {
					StringWriter errors = new StringWriter();
		        	e1.printStackTrace(new PrintWriter(errors));
		        	log.write(e1.toString());
				}
	        	
	        	
	    	}catch(Exception e){
	    		StringWriter err = new StringWriter();
	        	e.printStackTrace(new PrintWriter(err));
	        	log.write(e.toString());
	    		try {
	    			Thread.sleep(10000);
	    			reconnect();
				} catch (InterruptedException e1) {
					StringWriter errors = new StringWriter();
		        	e1.printStackTrace(new PrintWriter(errors));
		        	log.write(e1.toString());
				}
	        	
	
	    	}
		}
	}
	

	
}




