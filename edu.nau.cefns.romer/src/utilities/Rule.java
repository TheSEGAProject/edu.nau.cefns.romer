package utilities;

import interfaces.RuleInterface;


/**
 * A 'Rule' in the segaWeb Portal is the bit of logic that dictates how an experiment behaves. 
 * For example an experiment that is to check soil moisture values and issue on/off commands might
 * only have a single rule that contains the logic to check certain channels and compare those values
 * to threshold values and subsequently issue commands if needed. The idea behind using this format
 * is that rules should be able to be turned on or off whenever an experimenter needs them to, but also
 * provides a way to write logic after the experiment is up and running and add it on the fly.
 * @author jdk85
 *
 */
public class Rule implements java.io.Serializable, RuleInterface {
	/** Required by java.io.Serializable */
	private static final long serialVersionUID = -6740049263481252973L;
	/** Plain text name of the rule */
	private String name = ""; 
	/** Plain text description of the rule */
	private String description = "";
	 /** State boolean used to toggle the rule on or off */
	private boolean isRunning = false;
	/** String array of the channels associated with the rule - currently unimplemented */
	private String[] channels;
	
	/**
	 * Constructor takes in the starting parameters of the rule
	 * @param name
	 * @param description
	 * @param isRunning
	 */
	public Rule(String name, String description, boolean isRunning, String[] channels){
		this.name = name;
		this.description = description;
		this.isRunning = isRunning;
		this.channels = channels;
	}
	/**
	 * 
	 * @return name of the Rule
	 */
	public String getName(){
		return name;
	}
	/**
	 * 
	 * @return String[] of channel names needed for exp
	 */
	public String[] getChannels(){
		return channels;
	}
	/**
	 * 
	 * @return description of the rule
	 */
	public String getDescription(){
		return description;
	}
	/**
	 * 
	 * @return current state of the rule
	 */
	public boolean getIsRunning(){
		return isRunning;
	}
	/**
	 * Used in the edit table to make it easier to recognize if a rule is currently running.
	 * Rules that are on will return the name of the rule is green text, while stopped rules 
	 * will return red text. 
	 * @return String that contains the rule status
	 */
	public String isRunningString(){
		if(isRunning) return "<span style=\"color:green\">"+name+"</span> is currently running.";
		else return "<span style=\"color:red\">"+ name + "</span> is NOT currently running.";
	}
	/**
	 * 
	 * @param str - the name of the rule
	 */
	public void setName(String str){
		name = str;
	}
	/**
	 * 
	 * @param str - description of the rule
	 */
	public void setDescription(String str){
		description = str;
	}
	
	/**
	 * Mostly unused but this a method that allows the calling function to force the state
	 *  of the rule instead of toggling it
	 *  @see toggleRule()
	 * @param b - boolean - true meaning the rule is on, false turns it off
	 */
	public void setIsRunning(boolean b){
		isRunning = b;
	}
	/**
	 * Used to change the state of the run, checks the current state and either turns 
	 * a rule on or off depending on the current state
	 */
	public void toggleRule(){
		if(isRunning)isRunning = false;
		else isRunning = true;
	}
	
	
	
}

