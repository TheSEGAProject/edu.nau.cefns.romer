package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import utilities.ArrayUtilities;
import utilities.CRC;
import utilities.PacketGenerator;
import utilities.SegaLogger;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;


/**
 * Servlet implementation class WisardBuildServlet
 */
@WebServlet("/CmdGeneratorServlet")
public class CmdGeneratorServlet extends HttpServlet {
	/** Required by java.io.Serializable */
	private static final long serialVersionUID = 5199097670843989396L;
	/** Log object */
	private static SegaLogger log = new SegaLogger(
			"/usr/share/tomcat7/segalogs/CmdGeneratorServlet.txt");
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CmdGeneratorServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getSession().removeAttribute("cmd_result");
	
		String redirect,garden_ip,cmd_type,hub_high,hub_low,dest_high,dest_low,processor,transducer,command;
		redirect=garden_ip=hub_high=hub_low=processor=transducer=command=null;
		dest_high=dest_low="--";
		byte[] b = null;
		if((garden_ip = request.getParameter("ip_address")) != null){
			try{
				if( (hub_high = request.getParameter("hub_high")) != null &&
					(hub_low = request.getParameter("hub_low")) != null &&
					(dest_high = request.getParameter("dest_high")) != null &&
					(dest_low = request.getParameter("dest_low")) != null){
					
					if((cmd_type = request.getParameter("cmd_type")) != null){
						if(cmd_type.equals("reset")){
							b = PacketGenerator.Reset_Command_Packet(hub_high, hub_low, dest_high, dest_low);
						}
						else{
							if((command = request.getParameter("command")) != null){
								if(cmd_type.equals("rssi")){
									b = PacketGenerator.RSSI_Command_Packet(hub_high, hub_low, dest_high, dest_low, command);
								}
								else if(cmd_type.equals("valve")){
									if((processor = request.getParameter("processor")) != null &&
											(transducer = request.getParameter("transducer")) != null){
										b = PacketGenerator.Valve_Command_Packet(hub_high, hub_low, dest_high, dest_low, processor, transducer, command);
									}
								}
							}
							
						}
					}
				 
				}
				else
					request.getSession().setAttribute("cmd_result",hub_high + " " + hub_low + " " + dest_high + " " + dest_low + " " + processor + " " + transducer + " " + command);
				if(b != null){
					addCrcAndFlush(garden_ip,b);
					log.write("Command sent to 0x"+dest_high + dest_low + " @ " + new Date(System.currentTimeMillis()).toString());
					request.getSession().setAttribute("cmd_result","Command sent to 0x"+dest_high + dest_low + " @ " + new Date(System.currentTimeMillis()).toString());
				}
				else{
					log.write("Command sent to 0x"+dest_high + dest_low + " @ " + new Date(System.currentTimeMillis()).toString());
					request.getSession().setAttribute("cmd_result","Command sent to 0x"+dest_high + dest_low + " @ " + new Date(System.currentTimeMillis()).toString());
				}
			}catch(Exception e){
				request.getSession().setAttribute("cmd_result", e.getMessage());
				StringWriter errors = new StringWriter();
	        	e.printStackTrace(new PrintWriter(errors));
	    		log.write(errors);
			}
			
		}
		if((redirect=request.getParameter("redirect")) != null){
			response.sendRedirect(redirect);
		}
	}
	
	
	public void addCrcAndFlush(String garden_ip,byte[] message) throws SAPIException{
		int crc = CRC.compute_crc(ArrayUtilities.convert_to_int_array(message));
        byte crc_hi_byte = (byte) (crc >> 8);
        byte crc_low_byte = (byte) (crc & 0xFF);
        
        byte[] cmdFinal = new byte[message.length+2];
        for(int i = 0; i < message.length; i++){
        	cmdFinal[i] = message[i];
        }
        cmdFinal[cmdFinal.length-2] = crc_hi_byte;
        cmdFinal[cmdFinal.length-1] = crc_low_byte;
        
        Source src = new Source(100,"append",1000);
        src.OpenRBNBConnection(garden_ip,"CommandGenerator_ControlSource");
        ChannelMap sMap = new ChannelMap();
        int index = sMap.Add("Commands");
        
        sMap.PutDataAsByteArray(index, cmdFinal); //cmds channel	        	
    	src.Flush(sMap);
    	src.Detach();
    	
	}

}
