package servlets;


import helpers.DataValidationHelper;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.authentication.AttributePrincipal;

import utilities.SegaLogger;

/**
 * This servlet is used to store and fetch saved request queries.
 * 
 * @author jdk85
 * 
 */
@WebServlet("/SegaDataValidationServlet")
public class SegaDataValidationServlet extends HttpServlet implements Servlet {
	/** Required by java.io.Serializable */
	private static final long serialVersionUID = 7199877521119156157L;
	/** Log object */
	
	private static SegaLogger log = new SegaLogger(
			"/usr/share/tomcat7/segalogs/SegaDataValidationServlet.txt");
	

	/**
	 * Default servlet constructor, calls parent constructor
	 * @see HttpServlet#HttpServlet()
	 */
	public SegaDataValidationServlet() {
		super();
	}
	
	private String getSQLUsername(String sql_server){
		if(sql_server.equals("mprlsrvr1.bio.nau.edu")){
			return "LoggerNet_Read";
		}
		else return null;
	}
	private String getSQLPassword(String sql_server){
		if(sql_server.equals("mprlsrvr1.bio.nau.edu")){
			return "#Kbm4xxL2";
		}
		else return null;
	}
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

	}

	/**
	 * Servlet doPost handles POST requests passed to the servlet from the web portal.
	 * This servlet handles all the requests made through the user landing page ("segaWeb/user/index.jsp")
	 * 
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		//Placeholder boolean to verify if user is administrator
		boolean isAdmin = false;
		//Placeholder strings used for parsing the request
		String redirect, rbnb_server, site_name, sql_server, sql_db, interval;
		redirect = rbnb_server = site_name = sql_server = sql_db = interval = null;

		try {
			
			//Establish whether or not the user has administrator privileges
			AttributePrincipal principal = (AttributePrincipal) request
					.getUserPrincipal();
			Map<String, Object> attributes = principal.getAttributes();
			String role = (String) attributes.get("role.name");
			if (role != null && role.contains("administrator")) {
				isAdmin = true;
			} else
				isAdmin = false;
			
			if(isAdmin){
				if((rbnb_server = request.getParameter("rbnb_server")) != null){
					if((site_name = request.getParameter("site_name")) != null){
						if((sql_server = request.getParameter("sql_server")) != null){
							if((sql_db = request.getParameter("sql_db")) != null){
								if((interval = request.getParameter("interval")) != null){
									String sql_user = getSQLUsername(sql_server);
									String sql_password =getSQLPassword(sql_server);
									DataValidationHelper dataVal = 
											new DataValidationHelper(Integer.parseInt(interval),rbnb_server,site_name,sql_server,sql_db,sql_user,sql_password);
									request.getSession().setAttribute("validate_result", dataVal.run());
								}
							}
						}
					}
				}
			}
			
			
			//Check the redirect and pass it along to the response
			if ((redirect = request.getParameter("redirect")) != null) {
				response.sendRedirect(redirect);
			}
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log.write(errors);
		}
	}

}
