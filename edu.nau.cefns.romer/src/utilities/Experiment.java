package utilities;

import interfaces.ControlInterface;
import servlets.SegaExperimentServlet;

/**
 * The experiment class is used as an object wrapper to provide access to a control program.
 * The experiment wrapper allows to easily modify high-level experiment variables such as experiment
 * ID, description, and type, while also providing an interface variable to store the control program. 
 * @author jdk85
 *
 */
public class Experiment implements java.io.Serializable {
	
	/** Required by java.io.Serializable*/
	private static final long serialVersionUID = -5353247291883536923L;
	/** A String ID for the experiment - guaranteed to be unique as verified when the experiment is uploaded*/
	private String expId=""; 
	/** A String blob description of the experiment, can be seen in the experiment parameter page on the web interface*/
	private String expDescription = "";
	/** The address (or the domain name) for the experiment sink*/
	private String sinkAddress = "";
	/** The address (or the domain name) for the experiment source*/
	private String sourceAddress = "";
	/** A control interface  object (what actually gets assigned here is a ControlClass object, but for future revisions any object that extends ControlInterface can be used)*/
	private ControlInterface control;
	/** The filename as it appears in the web app (/WEB-INF/classes/experiments/...) */
	private String controlFileName;
	/** Boolean true if the control has been assigned, false otherwise*/
	private boolean controlExists = false;
	
	/**
	 * This is a safety feature that is used to keep Tomcat from trying to load the experiments
	 * array more than once. By default, the array is loaded on startup but Tomcat can attempt to 
	 * reload it once a user calls the SegaExperimentServlet from the web interface.
	 * 
	 * The method functions as a way to check if this experiment object is actually the same as one 
	 * passed to it. This is necessary because using a generic .equals() call will result in false
	 * because the experiment objects, although the same, will appear as different because of the 
	 * way Tomcat attempts to load the experiment array.
	 * 
	 * @see SegaExperimentServlet
	 */
	@Override
	public boolean equals(Object obj){
		if(obj == null) return false;
		if(obj == this) return true;
		if(!(obj instanceof Experiment)) return false;
		Experiment exp = (Experiment) obj;
	    if(this.expId.equals(exp.expId) 
	          && this.expDescription.equals(exp.expDescription)
	          && this.sinkAddress.equals(exp.sinkAddress)
	          && this.sourceAddress.equals(exp.sourceAddress)){    	 
	        return true;
	    }
	    return false;
	}
	
	
    /**
     * Constructor that creates the experiment object.
     * 
     * @param expID
     * @param expDescription
     */
    public Experiment(String expId, String expDescription){
    	this.expId = expId;
    	this.expDescription = expDescription;
    	
    }
    /**
     * Constructor creates the experiment object, but also accepts sink/source addresses.
     * 
     * @param expId
     * @param expDescription
     * @param sinkAddress
     * @param sourceAddress
     */
    public Experiment(String expId, String expDescription, String sinkAddress, String sourceAddress){
    	this.expId = expId;
    	this.expDescription = expDescription;
    	this.sinkAddress = sinkAddress;
    	this.sourceAddress = sourceAddress;
    	
    }
    
    /**
     * Fetches and returns the experiment ID associated with this particular experiment object.
     * @return Experiment ID
     */
    public String getExpId(){
    	return expId;
    }
    /**
     * Fetches and returns the experiment description associated with this particular experiment object.
     * @return Experiment Description
     */
    public String getDescription(){
    	return expDescription;
    }

    /**
     * Fetches and returns the control file name associated with this experiment.
     * This function is used whenever the remove() function is called so it can properly delete associated
     * .class files in the WEB-INF/ folder.
     * @return String containing the file name for the control class.
     */
    public String getControlFileName(){
    	return controlFileName;
    }
    /**
     * Confirmation message associated with the creation of the control program.
     * A user should override the getMessage() method from the ControlInterface to ensure
     * that the control uploaded or running is the intended version. The message returned
     * should contain something unique like a date and time.
     * @return String containing a unique message from the control class
     */
    public String getMessage(){
    	return control.getMessage();
    }


    

    /**
     * Setter for experiment ID.
     * @param expID
     */
    public void setExpId(String expId){    	
    	this.expId = expId;
    }
    /**
     * Setter for experiment description.
     * @param expDescription
     */
    public void setExpDescription(String expDescription){
    	this.expDescription = expDescription;
    }
    /**
     * Setter for experiment control file name.
     * @see getControlFileName()
     * @param fileName
     */
    public void setControlFileName(String controlFileName){
    	this.controlFileName = controlFileName;
    }




	public String getSinkAddress() {
		return sinkAddress;
	}


	public void setSinkAddress(String sinkAddress) {
		this.sinkAddress = sinkAddress;
	}


	public String getSourceAddress() {
		return sourceAddress;
	}


	public void setSourceAddress(String sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

    /**
     * Setter to pass in a ControlInterface object.
     * This will associate whatever control object is passed here with this particular experiment.
     * The controlExists field is used when retrieving edit options for the control.
     * @param control 
     * @return Success or failure message
     */
	public String setControl(ControlInterface control){
		try{
    		this.control = control;
    		setControlExists(true);
    		return "Control successfully added";
    	}catch(Exception e){
    		return "Control error";
    	}
	}
	
	
    /**
     * Returns an instance of the control object.
     * This is usually called when iterating through the Experiments array in SegaExperimentServlet.
     * @return An object that implements ControlInterface
     */
	public ControlInterface getControl(){
		return control;
	}


	public boolean isControlExists() {
		return controlExists;
	}


	public void setControlExists(boolean controlExists) {
		this.controlExists = controlExists;
	}
	





	
    





}
