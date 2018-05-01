package servlets;

import helpers.ChannelTreeRetriever;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class SegaChannelListServlet
 * This class is a helper servlet that connects to RBNB and returns a structured 
 * channel tree to the calling .jsp. This is however, still used by the wisard-serv1.egr.nau.edu
 * web portal version 1.0
 * 
 * @author jdk85
 * @deprecated This servlet has been replaced by SegaDataRequestFormServlet
 * @see SegaDataRequestFormServlet 
 */
@WebServlet("/SegaChannelListServlet")
public class SegaChannelListServlet extends HttpServlet {
       
    /** Required by java.io.Serializable */
	private static final long serialVersionUID = -682421258713741881L;

	/**
     * @see HttpServlet#HttpServlet()
     */
    public SegaChannelListServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ChannelTreeRetriever ctr;
		response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String serverAddress,portNumber;
        if((serverAddress = request.getParameter("serverAddress")) != null){
        	if((portNumber = request.getParameter("portNumber")) != null){
        		ctr = new ChannelTreeRetriever(serverAddress+":"+portNumber);
        	}else{
        		ctr = new ChannelTreeRetriever(serverAddress+":3333");
        	}
        }else{
    		ctr = new ChannelTreeRetriever();
    	}
        
        String[] channelNames = ctr.getChannelTree();
        LinkedList<String> sourceNames = new LinkedList<String>();
        for(int i = 0; i < channelNames.length; i++){
        	String tempStr = channelNames[i].substring(0,channelNames[i].lastIndexOf("/"));
        	if(sourceNames.size() == 0) sourceNames.add(tempStr);
        	else if(sourceNames.get(sourceNames.size()-1).compareToIgnoreCase(tempStr) != 0){
        		sourceNames.add(tempStr);
        		
        	}
        }
		out.println("<table width=\"600\" border=\"0\">");
		
		out.println("<tr>");
		out.println("<td><b>Plotting Utility</b></td>");
		out.println("<td><b>Dashboard Utility</b></td>");
		out.println("</tr>");
		
		out.println("<tr>");
		out.println("<td width=\"50%\" valign=\"top\">");
 
        
        out.println("<select id=\"channelMenu\">");
        for(String s : channelNames){
        	out.println("<option value=\""+s+"\">"+s+"</option>");
        }
        out.println("</select>");
        
        
        out.println("<br/><br/>");
        out.println("<input name=\"duration\" type=\"text\" id=\"duration\" value=\"60\" />");
        out.println("<select id=\"durationParam\">");
        out.println("<option value=\"Seconds\">Seconds</option>");
        out.println("<option value=\"Minutes\">Minutes</option>");
        out.println("<option value=\"Hours\">Hours</option>");
        out.println("<option value=\"Days\">Days</option>");
        out.println("</select>");
        out.println("<br/><br/>");
        out.println("<button onclick=\"renderChart()\">Add to Plot</button>");
        
        
        out.println("</td>");
        out.println("<td width=\"50%\" valign=\"top\">");
        
        
        out.println("<select id=\"sourceMenu\">");
        for(int i = 0; i < sourceNames.size();i++){
        	if(!sourceNames.get(i).contains("_Log"))out.println("<option value=\""+sourceNames.get(i)+"\">"+sourceNames.get(i)+"</option>");
        }
        out.println("</select>");
        out.println("<br/><br/>");
        out.println("<button onclick=\"dashboard()\">Generate</button>");
        
        out.println("</td>");
		out.println("</tr>");
		out.println("</table>");
        
        
        
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}

