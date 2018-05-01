package helpers;

import servlets.SegaDataRequestFormServlet;

/**
 * A generic key/value pair object
 * This is only used by SegaDataReqeustFormServlet to return
 * server types and their display names, but this could easily be deleted and
 * XYDataPointObject could be used instead - they are the same class with "x,y" 
 * instead of "key,value"
 * 
 * @author jdk85
 * @see SegaDataRequestFormServlet
 */
public class KeyValueObject implements java.io.Serializable {

	private static final long serialVersionUID = -9098429558434007351L;
	
	//Local object variables
	/** Object stores key */
	private Object key = null;
	/** Object stores value */
	private Object value = null;
		/**
		 * Constructor assigns key and value to the local key/value variables
		 * @param key
		 * @param value
		 */
		public KeyValueObject(Object key, Object value) {
			this.key = key;
			this.value = value;
		}
		
		/*
		 * GETTERS/SETTERS
		 */
		public void setKey(Object key) {
	        this.key = key;
	    }
	    public void setValue(Object value) {
	        this.value = value;
	    }
	    
	    public Object getKey() {
	    	return key;
	    }
	    public Object getValue() {
	    	return value;
	    }
	    
	

}
