package helpers;

import servlets.RequestFormServlet;
import servlets.SegaDataRequestServlet;

/**
 * A generic key/value class used for a few different functions
 * but was written to store RBNB data where x is the timestamp
 * and y is the datapoint.
 * 
 * @author jdk85
 * @see SegaDataRequestServlet
 * @see DataFetchHelper
 * @see RBNBChannelObject
 * @see RequestFormServlet
 */
public class XYDataPointObject implements java.io.Serializable {
	/** Required by java.io.Serializable */
	private static final long serialVersionUID = -9098429558434007351L;
	
	/** Typically used to store timestamp (also functions as key) */
	private Object x = null;
	/** Typically used to store datapoint (also functions as value)*/
	private Object y = null;
		/**
		 * Generic constructor accepts x and y as Objects
		 * Typically, x is timestamp info and y is the datapoint
		 * When using XYDataPointObject as a key/value pair, x should
		 * be the key and y the value
		 * @param x key object
		 * @param y value object 
		 */
		public XYDataPointObject(Object x, Object y) {
			this.x = x;
			this.y = y;
		}
		/*
		 * GETTERS/SETTERS
		 */
		public void setX(Object x) {
	        this.x = x;
	    }
	    public void setY(Object y) {
	        this.y = y;
	    }
	    
	    public Object getX() {
	    	return x;
	    }
	    public Object getY() {
	    	return y;
	    }
	    
	

}