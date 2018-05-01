package interfaces;

import helpers.ExperimentParameterObject;

import java.util.ArrayList;

import servlets.SegaExperimentServlet;
import utilities.ControlClass;
import utilities.Experiment;
import utilities.Rule;
import experiments.BenchtopDemoSM;



/**
 * Interface for writing control programs.
 * 
 * This interface is used by the SegaExperimentServlet compiler to ensure uploaded
 * Java programs have all the necessary functions. It is written as an interface so that methods
 * that aren't needed don't need to be overridden. Future implementations may 
 * transform this to a class since most of the methods will be the same for many different experiments
 * and they can just be extended, but for now it seems like keeping it as an interface is easier.
 * 
 * 
 * @see SegaExperimentServlet
 * @see Experiment
 * @see ControlClass
 * @author jdk85
 *
 */
public interface ControlInterface {	

	/**
     * Convenience method to return if both sink and source are already connected
     * Also updates the RBNB connection status parameters
     * @return True if both source and sink are connected, false if source or sink is not connected
     */
	public abstract boolean isRBNBConnected();
    /**
     * Creates a new thread that runs the control logic.
     * The createRBNBControl() method should be overridden in any class that extends ControlClass. That way the appropriate instance
     * of RBNBControl is created. This function then starts the thread and logs whether or it was successfully started.
     * @return String indicating success or failure of starting the thread
     */
	public abstract void startThread();
	/**
	 * This method is very similar to disconnect except that RBNB connections are not closed
	 * @see disconnect()
	 * @return
	 */
	public abstract void stopThread();
	/**
	 * This method restarts the thread by calling stopThread() and startThread()
	 * @see stopThread()
	 * @see startThread()
	 * @return String result representing the result of restarting the thread
	 */
	public abstract void restartThread();
	/**
	 * This method is used to validate whether the SAPIException thrown should 
	 * attempt reconnecting and, if so, tries to reconnect to RBNB after the time
	 * out specified by the experiment parameters. It also checks to see if that experiment 
	 * has reached a limit of reconnect attempts.
	 * @param error
	 * @return
	 */
	public abstract boolean validateReconnectAttempt(String error,int attempts);
	/**
	 * Function used to interrupt the currently running thread and reconnect to RBNB.
	 * By interrupting the thread, the main loop in RBNB control should fall out. Joining the thread
	 * allows the program to wait to make sure it has actually ended the thread before continuing. If 30
	 * seconds time out and the thread still hasn't ended, the program continues executing which may be problematic
	 * in the future, and there is probably a safer way to implement this
	 * @return String indicating success or failure of reconnect
	 */
	public abstract void reconnect();
	/**
	 * Thread-safe method to disconnect from RBNB and interrupt the RBNBControlThread
	 * The method will wait up to 30 seconds for the thread to successfully die, after
	 * which it resets the associated thread objects and logs its progress as well as 
	 * returning a string containing the result of the operation
	 * 
	 * @return String indicating success or failure of disconnect 
	 */
	public abstract void disconnect();
	/**
	 * Getter for a message string that is unique to the experiment upload to indicate
	 * that it is the expected version of the control code. The message string is actually defined 
	 * in the experiment object
	 * 
	 * @see BenchtopDemoSM
	 * @see BencthopDemoSMV2
	 * @see ControlClass
	 * @return Message string (e.g. "Last edit: 01/01/1970 12:00PM")
	 */
	public abstract String getMessage();
	
	
	
    /**
     * Should never be called, but will pass along a nullary ID to initialize(String expID) if for some reason an experiment is missing it's ID.
     * This is used as a form of error handling
     * @return Result of initialization
     */
	public abstract void initialize();	
	
	/**
	 * This functions as the constructor but is explicitly called when experiments are reloaded by the SegaExperimentServlet.
	 * This function takes in the experiment ID and creates a log for the control as well as sets up variables that shouldn't change.
	 * After configuring and logging the setup, it starts the main thread for running the control.
	 * 
	 * @param expID - The unique identifier for the experiment
	 * @param expDesc - String blob describing the experiment
	 * @param sinkServerAddress - The RBNB address for the data sink (where to look to fetch data)
	 * @param sourceServerAddress - The RBNB address for the data source (where to send control commands to)
	 * 
	 * @see ControlClass for implementation
	 * @return Result of initialization
	 */
	public abstract void initialize(String expID,String expDesc,String sinkServerAddress,String sourceServerAddress);	
	
	/**
	 * This method initializes the generic experiment parameter array.
	 * Every experiment should have at least these parameters in addition to 
	 * any custom experiment parameters that are added in the experiment code.
	 * The experiment code will make a super(initParams()) call before adding it's 
	 * own unique parameters
	 * 
	 * @see ControlClass
	 * @see BenchtopDemoSM
	 * 
	 * @return none
	 * 
	 */
	public abstract void initParams();
	
	/**
	 * This method iterates over the experiment parameter array
	 * searching for the experiment parameter object using 
	 * its id string
	 * @see ControlClass
	 * @see ExperimentParameterObject
	 * @return An ExperimentParameterObject if the string id matches, otherwise null
	 * 
	 */
	public abstract ExperimentParameterObject getParameterById(String id);
	
	/**
	 * This is a helper method used to return the parameter id as
	 * a string representation. It is used mostly to log any 
	 * activity associated with an experiment
	 * 
	 * @see ControlClass
	 * @return A string representation of the parameter id
	 */
	public abstract String getParameterToStringById(String id);
	

	/**
	 * This is setter function to change the value of an experiment parameter
	 * given that it exists. If it doesn't, an error is internally recorded 
	 * as this method does not return anything.
	 * 
	 * @see ControlClass
	 * @return none
	 */
	public abstract void updateParameter(String idName,Object newVal);
	
	/**
	 * A helper method used to return the arraylist containing all of the experiment 
	 * parameters. This is used by experiment child classes to append experiment
	 * specific parameters to the parent class experiment parameter array object
	 * 
	 * @see BenchtopDemoSM
	 * @see ControlClass
	 * @see Experiment
	 * 
	 * @return An array list containing all experiment parameters
	 */
	
	public abstract ArrayList<ExperimentParameterObject> getParameters();
	
	/**
	 * Unimplemented setter for parameter array. This could be used in future revisions to 
	 * duplicate experiments from the web interface instead of uploading similar generic experiments
	 * 
	 * @param parameters
	 * @return none
	 */
	public abstract void setParameters(ArrayList<ExperimentParameterObject> parameters);
	
	/**
	 * Used to add rules to the rule arraylist in either the Experiment,ControlClass, or the experiment
	 * code.
	 * 
	 * @see Rule
	 * @see Experiment
	 * @see ControlClass
	 * @see BencthopDemoSM
	 * 
	 * @return none
	 */
	public abstract void initRules();
	
	/**
	 * Simple method used to return the rule object for a corresponding string
	 * The string is simply the rule name
	 * 
	 * @param str identifies a rule by name
	 * @see Rule
	 * @see ControlClass
	 * @see BenchtopDemoSM
	 * 
	 * @return Rule object if found, otherwise null
	 */
	public abstract Rule getRule(String str);
	
	/**
	 * Simple getter method to retrieve the rule arraylist.
	 * Should be implemented in the experiment code
	 * 
	 * @see Rule
	 * @see SegaExperimentServlet
	 * 
	 * @return Array list of an experiment's rule objects
	 */
	public abstract ArrayList<Rule> getRules();

	/**
	 * Simple setter (sort of) method to toggle the state
	 * of a rule. If isRunning is true, this method sets it
	 * to false and vice versa.
	 * 
	 * @see Rule
	 * @see SegaExperimentServlet
	 * 
	 * @return Array list of an experiment's rule objects
	 */
	public abstract void toggleRule(String ruleName);
	/**
	 * This is used to recreate the log file whenever the 
	 * experiment name is changed
	 */
	public abstract void recreateLog();
	/**
	 * Convenience method that calls and logs stopThread()
	 * and disconnect()
	 * @see stopThread()
	 * @see disconnect()
	 */
	public abstract void shutdown();
}