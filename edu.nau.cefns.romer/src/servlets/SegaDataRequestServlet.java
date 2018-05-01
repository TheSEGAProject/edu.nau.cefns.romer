package servlets;

import helpers.DataFetchHelper;
import helpers.RBNBChannelObject;
import helpers.SampleTimestampPackage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import utilities.SegaLogger;

/**
 * Servlet implementation class SegaDataRequestServlet
 */
@WebServlet("/SegaDataRequestServlet")
public class SegaDataRequestServlet extends HttpServlet {
	/** Required by java.io.Serializable */
	private static final long serialVersionUID = -1729542503675338717L;
	/** Log file and location to write to disk */
	private static SegaLogger log = new SegaLogger(
			"/usr/share/tomcat7/segalogs/SegaDataRequestServlet.txt");

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SegaDataRequestServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */

	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		ArrayList<RBNBChannelObject> data = null;
		try {

			if (request.getSession().getAttribute("dataUpdate") != null) {
				if (request.getSession().getAttribute("dataUpdate") instanceof ArrayList<?>) {
					data = (ArrayList<RBNBChannelObject>) request.getSession()
							.getAttribute("dataUpdate");
					out.write(getJSONString(data));
					out.flush();

					

				}

			} else if (request.getSession().getAttribute("rbnbData") != null) {
				
				
				if (request.getSession().getAttribute("rbnbData") instanceof ArrayList<?>) {
					data = (ArrayList<RBNBChannelObject>) request.getSession()
							.getAttribute("rbnbData");
					
					if (response.getContentType().contains(
							"application/vnd.ms-excel")) {
						out.println("Request Parameter Name, Request Parameter Value");
						Enumeration<String> attrs = request.getSession().getAttributeNames();
						String attrName = "";
						for(;attrs.hasMoreElements();){
							attrName = attrs.nextElement();
							if(attrName.contains("selected")){
								out.println(attrName + "," + request.getSession().getAttribute(attrName));
							}
						}
						out.write("\n\n\n");
						out.println("[Column Name]:, Units");
						out.println("[Channel Name]:, String");
						out.println("[RBNB Timestamp]:, Long - milliseconds since 1970");
						out.println("[Sample Time]:, Long - milliseconds since 1970");
						out.println("[Sample Data]:, String");
						out.println("[Sample Time]:, String");
						out.println("[Sample Date String]:, String");
						out.println("[Value]:, Specified by 'Format'");
						out.println("[Format]:, String");
						out.write("\n\n\n");
						out.println("Channel Name,RBNB Timestamp,Sample Timestamp,Sample Date,Sample Time,Sample Date String,Value,Format");
						for (RBNBChannelObject c : data) {
							for (SampleTimestampPackage xyz : c.getSample_data()) {
								out.write(c.getChannel_name() + ",");
								out.write(xyz.getRbnb_timestamp() + ",");
								out.write(xyz.getSample_timestamp() + ",");
								Date d = new Date(xyz.getSample_timestamp());
								SimpleDateFormat sdf = new SimpleDateFormat(
										"M/d/Y");
								out.write(sdf.format(d) + ",");
								sdf = new SimpleDateFormat("H:mm:ss:SSS");
								out.write(sdf.format(d) + ",");
								sdf = new SimpleDateFormat(
										"EEE MMM d H:mm:ss z y");
								out.write(sdf.format(d) + ",");
								
								if(c.getSample_type_ID() == 10){
									out.write("[");
									byte[] tempByteArr = (byte[])xyz.getSample_data();
									for(int i = 0; i < tempByteArr.length; i++){
										int onum = tempByteArr[i];
										int num = (onum < 0 ? 0xFF+onum+1 : onum);										
										out.write((num<16 ? "0x0" : "0x") + Integer.toHexString(num).toUpperCase());
										if(i != tempByteArr.length-1)out.write(" ");
									}
									out.write("],");
								}
								else{
									out.write(xyz.getSample_data() + ",");
									
								}
								out.write(c.getSample_type_name()+"\r\n");
								out.flush();
							}
						}
					} else if (response.getContentType().contains("text/csv")) {
						out.println("Request Parameter Name, Request Parameter Value");
						Enumeration<String> attrs = request.getSession().getAttributeNames();
						String attrName = "";
						for(;attrs.hasMoreElements();){
							attrName = attrs.nextElement();
							if(attrName.contains("selected")){
								out.println(attrName + "," + request.getSession().getAttribute(attrName));
							}
						}
						out.write("\n\n\n");
						out.println("Channel Name,RBNB Timestamp,Sample Timestamp,Value,Format");
						for (RBNBChannelObject c : data) {
							for (SampleTimestampPackage xyz : c.getSample_data()) {
								out.write(c.getChannel_name() + ",");
								out.write(xyz.getRbnb_timestamp() + ",");
								out.write(xyz.getSample_timestamp() + ",");
								if(c.getSample_type_ID() == 10){
									out.write("[");
									byte[] tempByteArr = (byte[])xyz.getSample_data();
									for(int i = 0; i < tempByteArr.length; i++){
										int onum = tempByteArr[i];
										int num = (onum < 0 ? 0xFF+onum+1 : onum);
										out.write((num<16 ? "0x0" : "0x") + Integer.toHexString(num).toUpperCase());
										if(i != tempByteArr.length-1)out.write(" ");
									}
									out.write("],");
								}
								else{
									out.write(xyz.getSample_data() + ",");
								}
								out.write(c.getSample_type_name()+"\r\n");
								out.flush();
							}
						}

					}
				}

			}
			out.close();

		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log.write(errors);
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String selectedServerAddrName, selectedChannels, selectedDataIntervalName, selectedStartDate, selectedEndDate, selectedTimeIntervalValue, 
			selectedOutputStyle,  selectedDataInterval, action;
		selectedServerAddrName=selectedChannels=selectedDataIntervalName=selectedStartDate=selectedEndDate=selectedTimeIntervalValue= 
			selectedOutputStyle=selectedDataInterval=action=null;
		
		String update_data_interval, update_server_ip, update_channel_list, update_output_style;
		update_data_interval = update_server_ip = update_channel_list = update_output_style = null;

		String[] parsedChannels = null;
		String redirect = null;
		ArrayList<RBNBChannelObject> data = null;

		try {

			if ((update_data_interval = request
					.getParameter("update_data_interval")) != null) {
				if ((update_server_ip = request
						.getParameter("update_server_ip")) != null) {
					if ((update_channel_list = request
							.getParameter("update_channel_list")) != null) {
						parsedChannels = update_channel_list.split(",");
						if ((update_output_style = request
								.getParameter("update_output_style")) != null) {
							DataFetchHelper dataHelper = new DataFetchHelper(
									update_server_ip);
							dataHelper.connect("UpdateDataRequestSink");
							int interval = Integer
									.parseInt(update_data_interval);
							data = dataHelper
									.getData(
											parsedChannels,
											(System.currentTimeMillis() / 1000 - interval),
											interval, "absolute");
							dataHelper.disconnect();
							request.getSession().setAttribute("dataUpdate",data);
							request.getSession().setAttribute("outputStyleUpdate", update_output_style);
							request.getSession().setAttribute("updating","true");
							response.setContentType("text/html");
							doGet(request, response);

						}
					}
				}
			}
			if ((action = request.getParameter("action")) != null && action.equals("play")) {
				/*
				 * TODO:
				 * This is so terrible.... so the issue is that client-side jsp generates parameters. Great.
				 * Then we set request attributes that the jsp the servlet redirects to can read.
				 * For forwaring, we're setting attributes not parameters. When you hit submit, the form
				 * generates parameters that are parsed here. So the cheap, tacky, awful work around is to check
				 * if the action is play and do request.getSession().getAttribute() instead of request.getParameter()
				 * 
				 * Again, this is terrible... please, please, please find an elegant solution
				 */
				log.write("ACTION: " + action + " " + redirect);
				if ((selectedServerAddrName = (String)request.getSession().getAttribute("selectedServerAddrName")) != null) {
					log.write("ADDR: " + selectedServerAddrName);
					if ((selectedChannels = (String)request
							.getAttribute("selectedChannels")) != null) {
						parsedChannels = selectedChannels.split(",");
						log.write("Channels: " + selectedChannels);
						if ((selectedOutputStyle = (String)request
								.getSession().getAttribute("selectedOutputStyle")) != null) {
							if (selectedOutputStyle.equals("Dashboard Style")) {
								DataFetchHelper dataHelper = new DataFetchHelper(
										selectedServerAddrName);
								dataHelper.connect("TempDataRequestSink");

								data = dataHelper.getData(parsedChannels, 0., 0.,
										"newest");
								dataHelper.disconnect();

								request.getSession().setAttribute("rbnbDataJSON", getJSONString(data));
								request.getSession().setAttribute("rbnbData", data);
							} else {
								if ((selectedDataInterval = (String)request
										.getSession().getAttribute("selectedDataInterval")) != null) {
									if (selectedDataInterval.equals("0")) {
										SimpleDateFormat sdf = new SimpleDateFormat(
												"EEE MMM d H:m:s z y");
										if ((selectedStartDate = (String)request
												.getSession().getAttribute("selectedStartDate")) != null) {
											if ((selectedEndDate = (String)request
													.getSession().getAttribute("selectedEndDate")) != null) {
												DataFetchHelper dataHelper = new DataFetchHelper(
														selectedServerAddrName);
												dataHelper
														.connect("TempDataRequestSink");
												Long start = (sdf.parse(
														selectedStartDate)
														.getTime() / 1000);
												Long end = (sdf.parse(
														selectedEndDate)
														.getTime() / 1000);
												Long duration = end - start;
												data = dataHelper.getData(
														parsedChannels, start,
														duration, "absolute");
												dataHelper.disconnect();
												request.getSession().setAttribute("rbnbDataJSON", getJSONString(data));
												request.getSession().setAttribute("rbnbData", data);
											}
										}

									}
									if (selectedDataInterval.equals("1")) {
										if ((selectedDataIntervalName = (String)request
												.getSession().getAttribute("selectedDataIntervalName")) != null) {
											if ((selectedTimeIntervalValue = (String)request
													.getSession().getAttribute("selectedTimeIntervalValue")) != null) {
												DataFetchHelper dataHelper = new DataFetchHelper(
														selectedServerAddrName);
												dataHelper
														.connect("TempDataRequestSink");
												if (selectedDataIntervalName
														.equals("From Time Now")) {
													Double duration = Double
															.parseDouble(selectedTimeIntervalValue);
													data = dataHelper
															.getData(
																	parsedChannels,
																	(System.currentTimeMillis() / 1000.0 - duration),
																	duration,
																	"absolute");
												} else if (selectedDataIntervalName
														.equals("From Data Point")) {
													data = dataHelper
															.getData(
																	parsedChannels,
																	0,
																	Double.parseDouble(selectedTimeIntervalValue),
																	"newest");
												}

												dataHelper.disconnect();
												if(data != null){
													request.getSession().setAttribute("rbnbDataJSON", getJSONString(data));
													request.getSession().setAttribute("rbnbData", data);
												}
											}
										}
									}
								}
							}

						}

					}

				}

				if ((selectedOutputStyle = (String)request
						.getSession().getAttribute("selectedOutputStyle")) != null) {
					if (selectedOutputStyle.equals("Plotting Utility")) {
						redirect = "/segaWeb/data/plotting/segaplotting.jsp";
					} else if (selectedOutputStyle.equals("Sortable Table")) {
						redirect = "/segaWeb/data/table/segadatatable.jsp";
					} else if (selectedOutputStyle.equals("Dashboard Style")) {
						redirect = "/segaWeb/data/dashboard/segadatadashboard.jsp";
					} else if (selectedOutputStyle.equals("Download as CSV")
							|| selectedOutputStyle.equals("Download as Excel")) {
						try {
							if (data != null) {
								if (selectedOutputStyle.equals("Download as CSV")) {
									response.setContentType("text/csv");
								} else {
									response.setContentType("application/vnd.ms-excel");
								}
								SimpleDateFormat sdf =new SimpleDateFormat("YYYY-MM-dd_HHmmss");
								response.setHeader("Content-disposition","attachment;filename=" + sdf.format(new Date(System.currentTimeMillis())) + "-segadata.csv;");
								response.setCharacterEncoding("UTF-8");
							}
						} catch (Exception e) {
							StringWriter errors = new StringWriter();
							e.printStackTrace(new PrintWriter(errors));
							log.write(errors);
						}
					}
				}
			}
			else{
				if ((selectedServerAddrName = request.getParameter("selectedServerAddrName")) != null) {
					if ((selectedChannels = request
							.getParameter("selectedChannels")) != null) {
						parsedChannels = selectedChannels.split(",");
	
						if ((selectedOutputStyle = request
								.getParameter("selectedOutputStyle")) != null) {
							if (selectedOutputStyle.equals("Dashboard Style")) {
								DataFetchHelper dataHelper = new DataFetchHelper(
										selectedServerAddrName);
								dataHelper.connect("TempDataRequestSink");
	
								data = dataHelper.getData(parsedChannels, 0., 0.,
										"newest");
								dataHelper.disconnect();
	
								request.getSession().setAttribute("rbnbDataJSON", getJSONString(data));
								request.getSession().setAttribute("rbnbData", data);
							} else {
								if ((selectedDataInterval = request
										.getParameter("selectedDataInterval")) != null) {
									if (selectedDataInterval.equals("0")) {
										SimpleDateFormat sdf = new SimpleDateFormat(
												"EEE MMM d H:m:s z y");
										if ((selectedStartDate = request
												.getParameter("selectedStartDate")) != null) {
											if ((selectedEndDate = request
													.getParameter("selectedEndDate")) != null) {
												DataFetchHelper dataHelper = new DataFetchHelper(
														selectedServerAddrName);
												dataHelper
														.connect("TempDataRequestSink");
												Long start = (sdf.parse(
														selectedStartDate)
														.getTime() / 1000);
												Long end = (sdf.parse(
														selectedEndDate)
														.getTime() / 1000);
												Long duration = end - start;
												data = dataHelper.getData(
														parsedChannels, start,
														duration, "absolute");
												dataHelper.disconnect();
												request.getSession().setAttribute("rbnbDataJSON", getJSONString(data));
												request.getSession().setAttribute("rbnbData", data);
											}
										}
	
									}
									if (selectedDataInterval.equals("1")) {
										if ((selectedDataIntervalName = request
												.getParameter("selectedDataIntervalName")) != null) {
											if ((selectedTimeIntervalValue = request
													.getParameter("selectedTimeIntervalValue")) != null) {
												DataFetchHelper dataHelper = new DataFetchHelper(
														selectedServerAddrName);
												dataHelper
														.connect("TempDataRequestSink");
												if (selectedDataIntervalName
														.equals("From Time Now")) {
													Double duration = Double
															.parseDouble(selectedTimeIntervalValue);
													data = dataHelper
															.getData(
																	parsedChannels,
																	(System.currentTimeMillis() / 1000.0 - duration),
																	duration,
																	"absolute");
												} else if (selectedDataIntervalName
														.equals("From Data Point")) {
													data = dataHelper
															.getData(
																	parsedChannels,
																	0,
																	Double.parseDouble(selectedTimeIntervalValue),
																	"newest");
												}
	
												dataHelper.disconnect();
												if(data != null){
													request.getSession().setAttribute("rbnbDataJSON", getJSONString(data));
													request.getSession().setAttribute("rbnbData", data);
												}
											}
										}
									}
								}
							}
	
						}
	
					}
	
				}
	
				if ((selectedOutputStyle = request
						.getParameter("selectedOutputStyle")) != null) {
					if (selectedOutputStyle.equals("Plotting Utility")) {
						redirect = "/segaWeb/data/plotting/segaplotting.jsp";
					} else if (selectedOutputStyle.equals("Sortable Table")) {
						redirect = "/segaWeb/data/table/segadatatable.jsp";
					} else if (selectedOutputStyle.equals("Dashboard Style")) {
						redirect = "/segaWeb/data/dashboard/segadatadashboard.jsp";
					} else if (selectedOutputStyle.equals("Download as CSV")
							|| selectedOutputStyle.equals("Download as Excel")) {
						try {
							if (data != null) {
								if (selectedOutputStyle.equals("Download as CSV")) {
									response.setContentType("text/csv");
								} else {
									response.setContentType("application/vnd.ms-excel");
								}
								SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD_H-mm-ss");
								response.setHeader("Content-disposition","attachment;filename=segadata_" + sdf.format(new Date(System.currentTimeMillis())) + ".csv;");
								response.setCharacterEncoding("UTF-8");
								doGet(request, response);
							}
						} catch (Exception e) {
							StringWriter errors = new StringWriter();
							e.printStackTrace(new PrintWriter(errors));
							log.write(errors);
						}
					}
				}
			}

			if (redirect != null){
				response.sendRedirect(redirect);
			}
				
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log.write(errors);
			// TODO redirect to error page
		}
	}
	
	@SuppressWarnings("unchecked")
	public String getJSONString(ArrayList<RBNBChannelObject> rbco){
		JSONArray channelObjs = new JSONArray();
		for (RBNBChannelObject co : rbco) {
			JSONObject channel = new JSONObject();
			channel.put("channel_name", co.getChannel_name());
			channel.put("sample_type_ID", co.getSample_type_ID());
			channel.put("sample_type_name", co.getSample_type_name());
			JSONArray xyzData = new JSONArray();
			for (SampleTimestampPackage xyz : co.getSample_data()) {
				JSONObject dp = new JSONObject();
				dp.put("rbnb_timestamp", xyz.getRbnb_timestamp());
				dp.put("sample_timestamp",xyz.getSample_timestamp());
				if (co.getSample_type_ID() == 10) {
					byte[] dtArr = (byte[]) xyz.getSample_data();
					JSONArray bArr = new JSONArray();
					for (int i = 0; i < dtArr.length; i++) {
						bArr.add(dtArr[i]);
					}
					dp.put("sample_data", bArr);
				} else {									
					dp.put("sample_data", xyz.getSample_data());
				}
	
				xyzData.add(dp);
			}
			channel.put("xyzData", xyzData);
			channelObjs.add(channel);
		}
		return channelObjs.toJSONString();
	}

}
