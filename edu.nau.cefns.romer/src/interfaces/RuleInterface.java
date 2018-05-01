package interfaces;


/**
 * An interface that is used by the rule class to guarantee that
 * every rule has necessary methods
 * 
 * @author jdk85
 *
 */
public interface RuleInterface {
	/**
	 * Getter for the rule name that also functions as display name
	 * @return String representation of the rule name
	 */
	public abstract String getName();
	/**
	 * Getter for a list of channels associated with the rule
	 * @return String array containing RBNB channel names
	 */
	public abstract String[] getChannels();
	/**
	 * Getter for a description of the rule 
	 * This is used by the web interface info button
	 * @return String blob describing the rule
	 */
	public abstract String getDescription();
	/**
	 * Getter returns true if the rule is running (ON)
	 * or false if it is paused (OFF)
	 * @return Boolean running state of the rule
	 */
	public abstract boolean getIsRunning();
	/**
	 * Getter returns a string (in HTML) representation of the rule state
	 * This is used by the web interface to display the rule state
	 * @return String representation of rule state
	 */
	public abstract String isRunningString();
	/**
	 * Setter method to set the display name of the rule
	 * @param str
	 */
	public abstract void setName(String str);
	/**
	 * Setter method for the rule description
	 * @param str
	 */
	public abstract void setDescription(String str);
	/**
	 * Setter method to set rule state
	 * @param b
	 */
	public abstract void setIsRunning(boolean b);
	/**
	 * This method checks the rule state and toggles 
	 * running/not running.
	 * 
	 */
	public abstract void toggleRule();
	
	
	
}
