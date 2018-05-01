package helpers;

import servlets.SegaDataRequestFormServlet;


/**
 * This helper class is used to store info returned from a SQL database containing valid servers.
 * The server can be for RBNB, MySQL, or any other SEGA related data store.
 * 
 * These objects are used to provide valid server locations for the web users.
 * The original implementation used serverIp to connect, but a safer alternative is
 * to use the serverName field (e.g. romer.cefns.nau.edu:333 instead of 134.114.xx.xx) 
 *
 * @see SegaDataRequestFormServlet
 * @author jdk85
 */
public class SegaServerObject implements java.io.Serializable {

	
	/** Required by java.io.Serializable */
	private static final long serialVersionUID = 4004327447902380606L;
	/** Auto-incrementing primary key server ID */
	private String serverId = null;
	/** DNS name of the server (e.g. romer.cefns.nau.edu:333) NOTE: this should include a port*/
	private String serverName = null;
	/** Common name of the server to be displayed (e.g. SEGA Data Center Server)*/
	private String serverCommonName = null;
	/** The IP address of the server NOTE: this should not be relied upon, use serverName instead */
	private String serverIp = null;
	/** A description of the server */
	private String serverDescription = null;
		/**
		 * A generic constructor that associates all server object variables
		 * @param serverId
		 * @param serverName
		 * @param serverCommonName
		 * @param serverIp
		 * @param serverDescription
		 */
		public SegaServerObject(String serverId, String serverName, String serverCommonName, String serverIp, String serverDescription) {
			this.serverId = serverId;
			this.serverName = serverName;
			this.serverCommonName = serverCommonName;
			this.serverIp = serverIp;
			this.serverDescription = serverDescription;
		}
		/*
		 * GETTERS/SETTERS      
		 */

	    public void setserverId(String serverId) {
	        this.serverId = serverId;
	    }
	    public void setServerName(String serverName) {
	        this.serverName = serverName;
	    }
	    public void setServerCommonName(String serverCommonName) {
	        this.serverCommonName = serverCommonName;
	    }
	    public void setServerIp(String serverIp) {
	        this.serverIp = serverIp;
	    }
	    public void setServerDescription(String serverDescription) {
	        this.serverDescription = serverDescription;
	    }
	    
	    

	    public String getserverId() {
	    	return serverId;
	    }
	    public String getServerName() {
	    	return serverName;
	    }
	    public String getServerCommonName() {
	    	return serverCommonName;
	    }
	    public String getServerIp() {
	        return serverIp;
	    }
	    public String getServerDescription() {
	    	return serverDescription;
	    }

	}