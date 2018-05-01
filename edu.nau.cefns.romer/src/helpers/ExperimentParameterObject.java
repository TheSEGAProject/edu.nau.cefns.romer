package helpers;

import experiments.BenchtopDemoSMV2;

/**
 * This is a container class used to store unique experiment parameter objects.
 * The web interface iterates over an arraylist of these objects to allow
 * the user to view or edit an arbitrary number of experiment parameters.
 * 
 * As an example, an experiment parameter object might be a WiSARD channel name for
 * monitoring soil moisture. The parameter object might then look like the following:
 * idName - "wisard_channel"
 * value - "egr249a-11_channelized/wisard_1641/"
 * displayName - "WiSARD Channel Name"
 * category - "Experiment Parameters"
 * description - "This is the full channel name for the WiSARD to check sample data, note that it does not include the SP or transducer ID."
 * isEditable - true
 * reqReconnect - false
 * 
 * When adding these objects to the experiment parameter array, the variable name and idName should be the same.
 * All of the above variables can (and probably should) be sent to the constructor, but a null object constructor and setters could
 * also be used.
 * 
 * @author jdk85
 * @see BenchtopDemoSMV2 
 */
public class ExperimentParameterObject implements java.io.Serializable {

	/** Required by java.io.Serializable*/
	private static final long serialVersionUID = -1825765526369811060L;
	/** The idName is the same as the local variable name used to store the parameter when adding it to the expobj array*/
	private String idName;
	/** The parameter could be any object, but will be treated as a string in almost all cases*/
	private Object value;
	/** The String used to label the parameter by the web interface*/
	private String displayName;
	/** Unused as of now, but this can be used to separate parameters into categories when displaying*/
	private String category;
	/** The description of the parameter can be seen by clicking the info box on the web interface*/
	private String description;
	/** A boolean that determines whether a use is allowed to change the value for this parameter*/
	private boolean isEditable;
	/** If true, the experiment must reconnect to RBNB in order for the changes to take place*/
	private boolean reqReconnect;	
	
	/**
	 * The only constructor for this class expects the parameter to be fully qualified on creation.
	 * If circumstances require, null can be passed for each argument and then setters can be used to 
	 * initialize each variable but this is not recommended and could be considered unsafe.
	 * 
	 * 
	 * @param idName
	 * @param value
	 * @param displayName
	 * @param category
	 * @param description
	 * @param isEditable
	 * @param reqReconnect
	 */
	public ExperimentParameterObject(String idName,Object value,String displayName, String category, String description, boolean isEditable,boolean reqReconnect){		
		this.setIdName(idName);
		this.setValue(value);
		this.setCategory(category);
		this.setDescription(description);
		this.setDisplayName(displayName);
		this.setIsEditable(isEditable);
		this.setReqReconnect(reqReconnect);
	}
	
	/*
	 * GETTERS/SETTERS
	 */
	
	public boolean getIsEditable() {
		return isEditable;
	}
	public void setIsEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}
	public boolean getReqReconnect() {
		return reqReconnect;
	}
	public void setReqReconnect(boolean reqReconnect) {
		this.reqReconnect = reqReconnect;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getIdName() {
		return idName;
	}
	public void setIdName(String idName) {
		this.idName = idName;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
