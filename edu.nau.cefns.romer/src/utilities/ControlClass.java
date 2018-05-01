package utilities;
import helpers.ExperimentParameterObject;
import interfaces.ControlInterface;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;

import experiments.BenchtopDemoSM;
/**
 * Hard-Coded Control classes should extend HardCodedControlClass.
 * Classes that extend HardCodedControlClass should override the following methods:
 * \code
 * public HCExampleExperiment(String expID) {
		super(expID);
	}
	
	@Override
	public String getMessage() {
		return "Make this something to indicate this is the correct version like the date and time you last updated it";
	}
	
	@Override
	protected RBNBControl createRBNBControl(){
		return new RBNBControl();
	}


		public class RBNBControl extends HardCodedControlClass.RBNBControl{
			@Override
			public void run(){
				//put your control logic here
				//see HCExampleExperiment for an example
				//be sure to start by calling connect();
				//the logic 'while loop' must be:
				 while(!Thread.currentThread().isInterrupted()){
				 	//logic
				 }
		    }
		}
				
 * \endcode
 * 
 * 
 * @see HCExampleExperiment
 * @author jdk85
 *
 */
public class ControlClass implements ServletContextListener, java.io.Serializable, ControlInterface  {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** Required by java.io.Serializable */
	/** Name value of operating system */
	private String OS; 
	/** RBNB object that will run a control thread */
	private transient RBNBControl localControlThread; 
	/** The control thread object run in RBNBControl */
	private transient Thread RBNBControlThread; 
	/** Log file */
	public SegaLogger log; 
	/** String file used to specify absolute path for log files */
	private String logLocation = "/usr/share/tomcat7/segalogs/";
	/** HTML string to indicate RBNB source/sink connection status */
	private static String connectedStr = "<span style='color:green;'>&#x26AB Connected</span>";
	/** HTML string to indicate RBNB source/sink connection status */
	private static String notConnectedStr = "<span style='color:red;'>&#x26AB NOT Connected</span>";
	
	protected static String runningStr = "<span style='color:green;'>&#x26AB Running</span>";
	protected static String notRunningStr = "<span style='color:red;'>&#x26AB NOT Running</span>";
	/*
     * Sink/Source Variables
     */
	/** RBNB Sink Object */
	private Sink sink = new Sink();
	/** Default framesize for command source */
	private int frameSize = 1000; 
	/** RBNB Source Object*/
	private Source src =new Source(frameSize,"append",frameSize*10); 
	/** RBNB Source Object*/
	private Source local_src =new Source(frameSize,"append",frameSize*10);
	private ArrayList<ExperimentParameterObject> parameters = new ArrayList<ExperimentParameterObject>();/** Experiment parameter array list stores all parent and child experiment parameters*/
    private ArrayList<Rule> rules = new ArrayList<Rule>();/** ArrayList that holds rule objects used for keeping track of logic */


    /**
     * Constructor that takes the experiment ID as a parameter.
     * The constructor just calls the initialize() function and passes along the ID
     * @param expID - The unique identifier for the experiment
     */
    public ControlClass(String expID){
    	initialize(expID,null,null,null);   	
    }
    
    public ControlClass(String expID,String expDesc,String sinkServerAddress, String sourceServerAddress,boolean init){
    	if(init){
    		initParams(); 
    		initRules();
    	}
    	initialize(expID,expDesc,sinkServerAddress,sourceServerAddress);  
    	
    }
    

    /**
     * @see ControlInterface
     */
	public void initialize() {
		initialize("NullaryConstructorNoID","NullaryConstructorNoDesc",null,null);
	}

	/**
	 * @see ControlInterface
	 */
	public void initialize(String expID, String expDesc, String sinkServerAddress, String sourceServerAddress){
		updateParameter("expID",expID);		
		log = new SegaLogger(logLocation + expID+ "_Log.txt");
		log.write("Initializing Experiment");
		
		updateParameter("expDesc",expDesc);
		
		if(sinkServerAddress == null) updateParameter("sinkServerAddress",setupLocalIP());
		else updateParameter("sinkServerAddress",sinkServerAddress);
		if(sinkServerAddress == null) updateParameter("sinkServerAddress",setupLocalIP());
		else updateParameter("sourceServerAddress",sourceServerAddress);
		
		
		updateParameter("timeZone","US/Arizona");
		updateParameter("sourceName",expID + "_ControlSource");
    	updateParameter("sourceChannelCommandName","Commands");
    	updateParameter("sourceChannelCommandViewName","Status");
    	updateParameter("sinkName",expID + "_ControlSink");
    	
    	log.write("Experiment Configuration: \n\tSink Address: " + sinkServerAddress + "\n\tSource Address: " + sourceServerAddress + "\n\tTimezone: " + getParameterToStringById("timeZone") +
    			"\n\tSource Name: " + getParameterToStringById("sourceName") + "\n\tSink Name:" + getParameterToStringById("sinkName"));
    	
    	startThread();
	}
	
	/**
	 * @see ControlInterface
	 */
	public void initParams(){		

		ExperimentParameterObject expID = new ExperimentParameterObject("expID","UndefinedExperimentID","Experiment ID","General Experiment Parameters","Unique String used to identify the experiment", true,false);
		ExperimentParameterObject expDesc = new ExperimentParameterObject("expDesc","Undefined Description","Experiment Description","General Experiment Parameters","A brief overview of the experiment that describes the control algorithm and how it works",true,false);
		
		ExperimentParameterObject runningStatus = new ExperimentParameterObject("runningStatus",notRunningStr,"Experiment Status","General Experiment Parameters","This parameter indicates whether the experiment loop is currently running",false,false);
		
		ExperimentParameterObject sinkServerAddress = new ExperimentParameterObject("sinkServerAddress","127.0.0.1","Sink Server Address","General Experiment Parameters","The RBNB address that the data sink should connect to", true,true);
		ExperimentParameterObject sourceServerAddress = new ExperimentParameterObject("sourceServerAddress","127.0.0.1","Source Server Address","General Experiment Parameters","The RBNB address that the command source should connect to", true,true);
		
		ExperimentParameterObject timeZone = new ExperimentParameterObject("timeZone","US/Arizona","Timezone","General Experiment Parameters","Timezone that the exeperiment should use to label samples",true,true);
		
		ExperimentParameterObject sourceName = new ExperimentParameterObject("sourceName","Source Name","Source Name","Control Configuration","This is automatically generated by adding '_ControlSource' to the end of the experiment ID",false,false);
		ExperimentParameterObject sinkName = new ExperimentParameterObject("sinkName","Sink Name","Sink Name","Control Configuration","This is automatically generated by adding '_ControlSink' to the end of the experiment ID",false,false);
		
		ExperimentParameterObject sourceChannelCommandName = new ExperimentParameterObject("sourceChannelCommandName","Commands","Command Channel Name","Control Configuration","The channel name used to identify the command channel",true,true);
		ExperimentParameterObject sourceChannelCommandViewName = new ExperimentParameterObject("sourceChannelCommandViewName","CommandViewer","Command Viewer Channel Name","Control Configuration","The channel name used to identify the command viewer channel",true,true);

		ExperimentParameterObject sourceConnectionStatus = new ExperimentParameterObject("sourceConnectionStatus",notConnectedStr,"RBNB Source Connection Status","RBNB Control Status","This parameter is used to keep track of the command source's connection status",false,false);
		ExperimentParameterObject localSourceConnectionStatus = new ExperimentParameterObject("localSourceConnectionStatus",notConnectedStr,"RBNB Local Source Connection Status","RBNB Control Status","This parameter is used to keep track of the command source's connection status",false,false);
		ExperimentParameterObject sinkConnectionStatus = new ExperimentParameterObject("sinkConnectionStatus",notConnectedStr,"RBNB Sink Connection Status","RBNB Control Status","This parameter is used to keep track of the control sink's connection status",false,false);
		
		ExperimentParameterObject reconnectTimeout = new ExperimentParameterObject("reconnectTimeout","60","Reconnect Timeout (seconds)","RBNB Control Status","This parameter is used to assert the number of seconds to wait before attempting a reconnect",true,false);
		ExperimentParameterObject reconnectAttempts = new ExperimentParameterObject("reconnectAttempts","60","Reconnect Attempt Limit","RBNB Control Status","This parameter is used to determine the maximum number of reconnect attempts before quitting",true,false);

		parameters.add(expID);
		parameters.add(expDesc);
		
		parameters.add(runningStatus);
		
		parameters.add(sinkServerAddress);
		parameters.add(sourceServerAddress);		
		
		parameters.add(timeZone);
		
		parameters.add(sourceName);
		parameters.add(sinkName);
		
		parameters.add(sourceChannelCommandName);
		parameters.add(sourceChannelCommandViewName);
		
		parameters.add(sourceConnectionStatus);
		parameters.add(localSourceConnectionStatus);
		parameters.add(sinkConnectionStatus);
		
		parameters.add(reconnectTimeout);
		parameters.add(reconnectAttempts);
		
	}

	/**
     * @see ControlInterface
     */
	public boolean isRBNBConnected(){
		if(getSink().VerifyConnection()){
        	updateParameter("sinkConnectionStatus",connectedStr);
		}
		else updateParameter("sinkConnectionStatus",notConnectedStr);
		
		if(getSink().VerifyConnection()){
        	updateParameter("sourceConnectionStatus",connectedStr);
		}
		else updateParameter("sourceConnectionStatus",notConnectedStr);
		
		if(getLocalSource().VerifyConnection()){
        	updateParameter("localSourceConnectionStatus",connectedStr);
		}
		else updateParameter("localSourceConnectionStatus",notConnectedStr);
		
		if(getSink().VerifyConnection() && getSource().VerifyConnection() && getLocalSource().VerifyConnection()) return true;
		else return false;
	}

	/**
	 * @see ControlInterface
	 */
	public void startThread(){
		log.write("Starting Thread");
    	localControlThread = createRBNBControl(); 
		RBNBControlThread = new Thread(localControlThread);
		RBNBControlThread.setName(getParameterToStringById("expID") + " - Control Thread");
		RBNBControlThread.setDaemon(true);
		RBNBControlThread.start();
		
    	if(RBNBControlThread.isAlive()){
    		log.write(RBNBControlThread.getName() + " - Initialized");
    		updateParameter("runningStatus",runningStr);
    	}
    	else{
    		log.write(RBNBControlThread.getName() + " - Failed to start");
    		updateParameter("runningStatus",notRunningStr);
    	}
    }
	
	/**
	 * This method restarts the thread
	 * @return
	 */
	public void restartThread(){
		log.write("Restarting Thread...");
		stopThread();
		startThread();

	}
	
	/**
	 * @see ControlInterface
	 */
	public void stopThread() {
		log.write("Shutting down control thread...");
		updateParameter("runningStatus",notRunningStr);
		try{
			
    		if(RBNBControlThread != null && (RBNBControlThread.isAlive() || !RBNBControlThread.isInterrupted()) ){
    			log.write("Interrupting " + RBNBControlThread.getName());
    			RBNBControlThread.interrupt(); 
    			try {
					RBNBControlThread.join(60000);
				} catch (InterruptedException e) {
					StringWriter errors = new StringWriter();
		        	e.printStackTrace(new PrintWriter(errors));
		        	log.write(e.toString());
				}
    			RBNBControlThread = null;
    			localControlThread = null;
				log.write("Experiment " + getParameterToStringById("expID") + ".RBNBControlThread successfully interrupted");
			}
    		else
    			log.write("Experiment " + getParameterToStringById("expID") + ".RBNBControlThread has been previously interrupted.");
    		
		}catch(Exception e){
			StringWriter errors = new StringWriter();
        	e.printStackTrace(new PrintWriter(errors));
        	log.write(errors.toString());
    		log.write("Unhandled Exception in disconnect(): " + e.toString());
		}
		
		log.write("Returning from stopThread()");
	}

	/**
	 * 
	 * @see ControlInterface
	 */
    public boolean validateReconnectAttempt(String error,int attempts){
    	
    	if(error.equals("Nesting java.io.InterruptedIOException")){
    		//DO NOT try to reconnect
    		log.write("Error thrown by " + getParameterToStringById("expID") + " caused by java.io.InterruptedIOException");
		}
		else if(error.equals("Nesting java.lang.InterruptedException")){
			//This can get thrown on a shutdown or restart during a fetch timeout
			//DO NOT try to reconnect
			log.write("Error thrown by " + getParameterToStringById("expID") + " caused by java.lang.InterruptedException");
		}
		else if(error.equals("Nesting java.lang.IllegalStateException")){		
			//This can happen when source channels already exist and the experiment class tries to reconnect
			//DO NOT try to reconnect
			log.write("Error thrown by " + getParameterToStringById("expID") + " caused by java.lang.IllegalStateException");
		}
		else if(error.equals("Nesting java.net.SocketException")){
			//This can happen when RBNB goes down while operating			
			log.write("Error thrown by " + getParameterToStringById("expID") + " caused by java.net.SocketException");
			//Attempt Reconnect
			if(attempts < Integer.parseInt(getParameterToStringById("reconnectAttempts"))){
				try{
					log.write("Reconnecting in " + Integer.parseInt(getParameterToStringById("reconnectTimeout")) + " seconds\tAttempt " + attempts + "/" + getParameterToStringById("reconnectAttempts"));
					Thread.sleep(Integer.parseInt(getParameterToStringById("reconnectTimeout"))*1000);
					return true;
				}catch(InterruptedException e){
					log.write("Interrupted during reconnect attempt");
				}
			}
			else
				log.write("Maximum number of reconnect attempts reached (" + attempts + ")");
				//SMS Paul Heinrich
		}
		else if(error.equals("Nesting java.net.ConnectException")){
			//This can happen when RBNB is down during initial connection
			log.write("Error thrown by " + getParameterToStringById("expID") + " caused by java.net.ConnectException");
			if(attempts < Integer.parseInt(getParameterToStringById("reconnectAttempts"))){
				try{
					log.write("Reconnecting in " + Integer.parseInt(getParameterToStringById("reconnectTimeout")) + " seconds...");
					Thread.sleep(Integer.parseInt(getParameterToStringById("reconnectTimeout"))*1000);
					return true;
				}catch(InterruptedException e){
					log.write("Interrupted during reconnect attempt");
				}
			}
			else
				log.write("Maximum number of reconnect attempts reached (" + attempts + ")");
				//SMS Paul Heinrich
		}
		else if(error.equals("Nesting java.io.EOFException")){
			//This can happen when RBNB is down during initial connection
			log.write("Error thrown by " + getParameterToStringById("expID") + " caused by java.io.EOFException");
			if(attempts < Integer.parseInt(getParameterToStringById("reconnectAttempts"))){
				try{
					log.write("Reconnecting in " + Integer.parseInt(getParameterToStringById("reconnectTimeout")) + " seconds\tAttempt " + attempts + "/" + getParameterToStringById("reconnectAttempts"));
					Thread.sleep(Integer.parseInt(getParameterToStringById("reconnectTimeout"))*1000);
					return true;
				}catch(InterruptedException e){
					log.write("Interrupted during reconnect attempt");
				}
			}
			else
				log.write("Maximum number of reconnect attempts reached (" + attempts + ")");
				//SMS Paul Heinrich
		}
		else{
			log.write("===== UNHANDLED Error thrown by " + getParameterToStringById("expID") + " caused by com.rbnb.sapi.SAPIException - " + error);
		}
    	return false;
    }
	/**
	 * @see ControlInterface
	 */
	public void reconnect(){
    	try{
    		shutdown();
    		log.write("Restarting RBNBControl thread...");
			startThread();
			log.write(getParameterToStringById("expID") + " Successfully Reconnected");
    		
    	}catch(SecurityException e){
    		StringWriter errors = new StringWriter();
        	e.printStackTrace(new PrintWriter(errors));
        	log.write(e.toString());
    		log.write("Security Exception Thrown during reconnect");
    	}catch(Exception e){
    		StringWriter errors = new StringWriter();
        	e.printStackTrace(new PrintWriter(errors));
        	log.write(errors.toString());
        	log.write("Unhandled exception thrown during reconnect");
    	}
    }
	/**
	 * @see ControlInterface
	 */
	public void disconnect() {
		try{
			if(isRBNBConnected()){
				getSink().CloseRBNBConnection();
				getSource().Detach();	
				getLocalSource().Detach();
				log.write("Experiment " + getParameterToStringById("expID") + " RBNB connections successfully disconnected.");			    		
			}
			else
				log.write("RBNB already disconnected");
			
		}catch(Exception e){
			StringWriter errors = new StringWriter();
        	e.printStackTrace(new PrintWriter(errors));
        	log.write(errors.toString());
    		log.write("Unhandled Exception in disconnect(): " + e.toString());
		}
	}
	public void shutdown(){
		try{
    		log.write("Stopping RBNBControl Thread...");
    		stopThread();
    		log.write("Closing RBNB Connections...");
    		disconnect();    
    		log.write(getParameterToStringById("expID") + " shutdown successfully.");
	    		
    	}catch(SecurityException e){
    		StringWriter errors = new StringWriter();
        	e.printStackTrace(new PrintWriter(errors));
        	log.write(e.toString());
    		log.write("Security Exception Thrown during shutdown");
    	}catch(Exception e){
    		StringWriter errors = new StringWriter();
        	e.printStackTrace(new PrintWriter(errors));
        	log.write(errors.toString());
        	log.write("Unhandled exception thrown during shutdown");
    	}
	    
	}
	/**
	 * This function logs the creation of an RBNBControl object and returns a new instance of RBNBControl.
	 * @return A new instance of RBNBControl class
	 */
	protected RBNBControl createRBNBControl(){
		log.write("createRBNBControl() called from " + this.getClass().getName());
		return new RBNBControl();
	}
	
	/**
	 * This method is intended to override the servlet context when starting up but since
	 * the initialize() function gets called whenever the servlet boots up, this isn't used.
	 */
    public void contextInitialized(ServletContextEvent event) {
    	
    }
    
    /**
     * Overrides the servlet contextDestroyed method to make sure that any threads are shut down properly.
     * @param event - A servlet event indicating a shutdown command
     */
    public void contextDestroyed(ServletContextEvent event) {
    	log.write("Context Destroyed Override - ControlClass");
    	shutdown();
    }

   

	
	/*
	 * GETTERS
	 */
	/**
	 * This method returns the log object for this particular instance of HardCodedControl.
	 * This log file is used by the RBNB control object so that it can write to the same location. When writing
	 * the RBNB control code, this method can be used to retrieve the log object to write error or status messages.
	 * @return
	 */
	public SegaLogger getLog(){
		return log;
	}
	/**
	 * This method returns the 
	 * @return
	 */
	public Sink getSink(){
		return sink;
	}
	
	public Source getSource(){
		return src;
	}
	
	public Source getLocalSource(){
		return local_src;
	}
	
	/**
	 * @see ControlInterface
	 */
	public String getMessage() {
		return "Default Message - Override";
	}

	public String setupLocalIP(){
		String sinkServerAddress;
		try{
			if (isUnix()) sinkServerAddress = getIP();			
			else sinkServerAddress = java.net.InetAddress.getLocalHost().getHostAddress() +":3333";
			return sinkServerAddress;
		}catch (Throwable t) { 
			System.out.println("THROWN");
			sinkServerAddress="setup didn't work"; 
			return sinkServerAddress;
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean isUnix() {
		 
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
 
	}
	/**
	 * 
	 * @return
	 * @throws SocketException
	 */
	public String getIP() throws SocketException 
    {
    String ipAddr = "";
    Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets))
        {
        if(netint.getDisplayName().equals("eth0"))
        {
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) 
                {
        ipAddr = inetAddress.toString();
        ipAddr = ipAddr.substring(1);
                }
        }
        }
        return ipAddr + ":3333";
    }
	
	
	
	




/**
 * This inner class is meant to provide the base functionality for classes that extend ControlClass
 * Normally, an experiment will have an inner class that extends ControlClass.RBNBControl
 * This way, the experiment specific code only has to override the run() and include the 
 * necessary control logic within that method where all the backend processes (connecting to RBNB, sending commands)
 * are handled by this class
 * @author jdk85
 *
 */
	public class RBNBControl extends Thread implements Runnable{


    	protected ChannelMap srcMap,statusMap;
		
		public void run(){
			log.write(Thread.currentThread().getName() + " initialized from " + this.getClass().getName());
		}
	    
		/**
		 * Convenience method for ensuring that RBNB source and sinks safely connect
		 * @throws SAPIException
		 */
		protected boolean connect() throws SAPIException{
			if(!isRBNBConnected()){			
				log.write(getParameterToStringById("expID") + " connecting to RBNB...");
				getSink().CloseRBNBConnection();
				getSource().Detach();
				getLocalSource().Detach();
				
				sink = new Sink();
				src = new Source(frameSize,"append",frameSize*10); 
				local_src = new Source(frameSize,"append",frameSize*10);
				
		        getSink().OpenRBNBConnection(getParameterToStringById("sinkServerAddress"), getParameterToStringById("sinkName"));
		        getLocalSource().OpenRBNBConnection(getParameterToStringById("sinkServerAddress"), getParameterToStringById("sourceName")+"_local");
		        getSource().OpenRBNBConnection(getParameterToStringById("sourceServerAddress"), getParameterToStringById("sourceName"));
		        
		        if(getSink().VerifyConnection()){
		        	updateParameter("sinkConnectionStatus",connectedStr);
		        	log.write("Sink connected successfully");
				}
				else{
					log.write("Sink unable to connect to RBNB");
					updateParameter("sinkConnectionStatus",notConnectedStr);
				}
				
				if(getSource().VerifyConnection()){
					log.write("Source connected successfully");
					updateParameter("sourceConnectionStatus",connectedStr);
					srcMap = new ChannelMap();
			        srcMap.Add(getParameterToStringById("sourceChannelCommandName"));
				}
				else{
					updateParameter("sourceConnectionStatus",notConnectedStr);
					log.write("Source unable to connect to RBNB");
				}
				
				if(getLocalSource().VerifyConnection()){
					log.write("Local source connected successfully");
					updateParameter("localSourceConnectionStatus",connectedStr);
			        statusMap = new ChannelMap();
			        statusMap.Add("Status");
			        				      
				}
				else{
					updateParameter("localSourceConnectionStatus",notConnectedStr);
					log.write("Local source unable to connect to RBNB");
				}
			
			}
			else
				log.write("connect() called while RBNB already connected");
			
			if(isRBNBConnected()) return true;
			else return false;
		
		}
		/**
		 * Convenience method for adding a CRC to a message and flushing it to the command 
		 * source channel in a single function call
		 * @param message
		 * @throws SAPIException
		 */
		public void addCrcAndFlush(byte[] message) throws SAPIException{
			int crc = CRC.compute_crc(ArrayUtilities.convert_to_int_array(message));
	        byte crc_hi_byte = (byte) (crc >> 8);
	        byte crc_low_byte = (byte) (crc & 0xFF);
	        
	        byte[] cmdFinal = new byte[message.length+2];
	        for(int i = 0; i < message.length; i++){
	        	cmdFinal[i] = message[i];
	        }
	        cmdFinal[cmdFinal.length-2] = crc_hi_byte;
	        cmdFinal[cmdFinal.length-1] = crc_low_byte;
	        
	        srcMap.PutDataAsByteArray(srcMap.GetIndex(getParameterToStringById("sourceChannelCommandName")), cmdFinal); //cmds channel	        	
        	src.Flush(srcMap); 
		}
		/**
		 * Command method used to flush a byte array containing the WiSARD message
		 * as well as a command sent confirmation byte array indicating when the command
		 * was sent
		 * @param cmdMessage
		 * @param cmdSent
		 * @throws SAPIException 
		 */
		public void command(byte[] cmdMessage) throws SAPIException{
				//sends command to RBNB
				
				srcMap.PutDataAsByteArray(srcMap.GetIndex(getParameterToStringById("sourceChannelCommandName")), cmdMessage); //cmds channel	        	
	        	src.Flush(srcMap);       	
		}
		/**
		 * Command method used to flush a String for status containing the WiSARD message
		 * 
		 * @param String the status to post
		 * @throws SAPIException 
		 */
		public void postStatusAsString(String status) throws SAPIException{				
				statusMap.PutDataAsString(statusMap.GetIndex("Status"), status);
	        	local_src.Flush(statusMap);       	
		}
		/**
		 * Posts a single byte of information to the status channel
		 * @param status
		 * @throws SAPIException
		 */
		public void postStatusAsByte(byte[] status) throws SAPIException{				
			statusMap.PutDataAsInt8(statusMap.GetIndex("Status"), status);
			local_src.Flush(statusMap);       	
		}
		/**
		 * Posts information to the status channel as an integer
		 * @param status
		 * @throws SAPIException
		 */
		public void postStatusAsInt(int[] status) throws SAPIException{
			statusMap.PutDataAsInt32(statusMap.GetIndex("Status"), status);
			local_src.Flush(statusMap);
		}
    }
/**
 * @see ControlInterface
 */
@Override
public ExperimentParameterObject getParameterById(String id) {
	for(ExperimentParameterObject epo : parameters){
		if(epo.getIdName().equalsIgnoreCase(id))
			return epo;
	}
	return null;
}
/**
 * @see ControlInterface
 */
@Override
public String getParameterToStringById(String id){
	for(ExperimentParameterObject epo : parameters){
		if(epo.getIdName().equalsIgnoreCase(id))
			return epo.getValue().toString();
	}
	return null;
}

/**
 * @see ControlInterface
 */
@Override
public void updateParameter(String idName, Object newVal) {
	try{
		getParameterById(idName).setValue(newVal);	
	}catch(Exception e){
		StringWriter errors = new StringWriter();
    	e.printStackTrace(new PrintWriter(errors));
    	log.write("UPDATE PARAMETER ERROR -- " + idName + " returned null");
		log.write(errors.toString());
	}
}

/**
 * @see ControlInterface
 */
@Override
public ArrayList<ExperimentParameterObject> getParameters() {	
	return parameters;
}

/**
 * @see ControlInterface
 */
@Override
public void setParameters(ArrayList<ExperimentParameterObject> parameters) {
	this.parameters = parameters;
	
}
/**
 * This gets overridden by the experiment itself
 * 
 * @see BenchtopDemoSM
 * @see ControlInterface
 */
@Override
public void initRules(){
	
}

/**
 * @see ControlInterface
 */
@Override
public Rule getRule(String str) {
	for(Rule r : getRules()){
		if(r.getName().equalsIgnoreCase(str)){
			return r;
		}
	}
	return null;

}
/**
 * @see ControlInterface
 */
@Override
public ArrayList<Rule> getRules() {
	return rules;
}

/**
 * @see ControlInterface
 */
@Override
public void toggleRule(String ruleName) {
	Rule temp;
	if((temp = getRule(ruleName)) != null){
		temp.toggleRule();
	}
}

/**
 * @see ControlInterface
 */
public void recreateLog(){
	log.close();
	log = new SegaLogger(logLocation + getParameterToStringById("expID")+ "_Log.txt");
}
/**
 * 
 * @param parameter_name
 * @return
 */
public byte getHexParameterAsByte(String parameter_name){
	String temp;
	if((temp = (String)getParameterById(parameter_name).getValue()) != null){
		return (byte)Integer.parseInt(temp.replace("0x", ""),16);
	}
	else return -1;
			
}




  	
}




