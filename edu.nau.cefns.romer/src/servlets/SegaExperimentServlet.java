package servlets;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;

import utilities.ControlClass;
import utilities.Experiment;
import utilities.SegaLogger;


/**
 * Servlet implementation class SegaExperimentServlet
 */
@WebServlet("/SegaExperimentServlet")
public class SegaExperimentServlet extends HttpServlet {

    
	/** Required by java.io.Serializable */
	private static final long serialVersionUID = -4006212757470146274L;
	/** Relative path used to specify the temporary upload folder location */
    private static final String UPLOAD_DIRECTORY = "tmp/uploads";
    /** The package name used when compiling and saving uploaded experiments */
    private static final String PACKAGE_DIR = "experiments";
    /** The memory threshold specifying whether to store in cache or save directly to disk */
    private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 3;  // 3MB
    /** The max file size for the uploaded .java or .class file */
    private static final int MAX_FILE_SIZE      = 1024 * 1024 * 10; // 10MB
    /** The maximum size for a complete request*/
    private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 15; // 15MB
    /** Static array list that stores all the experiment objects and acts as a container for each experiment control thread */
	private static ArrayList<Experiment> experiments = new ArrayList<Experiment>(); 
	/** Log file and location to write to disk */
	private static SegaLogger log;
	/** Root directory for Tomcat */
	private String rootDir = "/usr/share/tomcat7";
	/** Absolute path for the location of the log file, uses root directory and adds log folder*/
	private String logLocation = rootDir + "/segalogs/";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SegaExperimentServlet() {
        super();
    }
    /**
     * 
     * @return
     */
    public ArrayList<Experiment> getExperiments(){
    	return experiments;
    }
    /**
     * 
     * @param experiments
     */
    public void setExperiments(ArrayList<Experiment> experiments){
    	SegaExperimentServlet.experiments = experiments;
    }
    

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		request.getSession().removeAttribute("errorMsg");
		PrintWriter out = response.getWriter();
		String action,expID,expDesc,fieldID,newVal;
		
		
		if((action = request.getParameter("action")) != null){
			
			if(action.compareToIgnoreCase("create") == 0){
				
				if((expID = request.getParameter("expID")) != null && (expDesc = request.getParameter("expDesc")) != null){
					if(!expIDExists(expID)){
					 Experiment exp = new Experiment(expID,expDesc);
					 experiments.add(exp);
					 log.write("New Experiment Created: " + expID);
					 saveConfiguration();
					}
					else out.write("Error: Experiment ID Already Exists");
				}
			}
			
			else if(action.compareToIgnoreCase("fetch") == 0){
				
			}
			else if(action.compareToIgnoreCase("getExpMenu") == 0){
				out.write(getExperimentsMenu());
			}
			else if(action.compareToIgnoreCase("getExpEditMenu") == 0){
			}
			else if(action.compareToIgnoreCase("editExp") == 0){
				if((expID = request.getParameter("expID")) != null){					
				}
				else out.write("Error: Problem retrieving experiment information");
			}
			else if(action.compareToIgnoreCase("toggleRule") == 0){
				if((expID = request.getParameter("expID")) != null){
					if((fieldID = request.getParameter("ruleName")) != null){
						getExpByID(expID).getControl().getRule(fieldID).toggleRule();
						log.write(expID + ">>Rule Toggled>>RULE: " + fieldID);
						saveConfiguration();
					}
				}
			}
			else if(action.compareToIgnoreCase("editVal") == 0){
				if((expID = request.getParameter("expID")) != null){
					if((fieldID = request.getParameter("fieldID")) != null){
						if((newVal = request.getParameter("newVal")) != null){
							if(fieldID.compareToIgnoreCase("expID") == 0){
								if(!expIDExists(newVal)){
									getExpByID(expID).setExpId(newVal);
									getExpByID(newVal).getControl().updateParameter("expID",newVal);
									getExpByID(newVal).getControl().updateParameter("sourceName",newVal + "_ControlSource");
									getExpByID(newVal).getControl().updateParameter("sinkName",newVal + "_ControlSink");
									getExpByID(newVal).getControl().recreateLog();
									getExpByID(newVal).getControl().reconnect();
								}
								else 
									out.write("Error: Experiment ID Already Exists");
								
								log.write(expID + ">>Experiment ID>>MODIFIED: " + newVal);
								saveConfiguration();
							}
							else if(fieldID.compareToIgnoreCase("expDesc") == 0){
								getExpByID(expID).getControl().updateParameter("expDesc",newVal);
								log.write(expID + ">>Experiment Description>>MODIFIED: " + newVal);
								saveConfiguration();
							}
							else if(fieldID.compareToIgnoreCase("sinkServerAddress") == 0){
								getExpByID(expID).getControl().updateParameter("sinkServerAddress",newVal);
								log.write(expID + ">>Sink Server Address>>MODIFIED: " + newVal);
								saveConfiguration();
							}
							else if(fieldID.compareToIgnoreCase("sourceServerAddress") == 0){
								getExpByID(expID).getControl().updateParameter("serverSourceAddress",newVal);
								log.write(expID + ">>Source Server Address>>MODIFIED: " + newVal);
								saveConfiguration();
							}

							else{
								try{
									getExpByID(expID).getControl().updateParameter(fieldID, newVal);
								
								/*
								try {
									Object[] tempArgs = new Object[1];
										if(getExpByID(expID).getControl().getGetterMethod(fieldID).getReturnType().equals(int.class)){
											 tempArgs[0] = Integer.parseInt(newVal);
										}									
										else  tempArgs[0] = newVal;
										
										getExpByID(expID).getControl().getSetterMethod(fieldID).invoke(getExpByID(expID).getControl(), tempArgs);
										*/
									log.write(expID + ">>" + fieldID + ">>MODIFIED: " + newVal);									
									
									saveConfiguration();
								} catch (IllegalArgumentException e) {
									StringWriter errors = new StringWriter();
						        	e.printStackTrace(new PrintWriter(errors));
						        	log.write(errors.toString());
						        	out.write(e.toString());								
								}catch(Exception e){
									StringWriter errors = new StringWriter();
						        	e.printStackTrace(new PrintWriter(errors));
						        	log.write(errors.toString());
						        	out.write(e.toString());
								}
							
							}

						}
						else out.write("Error: Problem retrieving experiment information");
					}
					else out.write("Error: Problem retrieving experiment information");
				
								
				}
				else out.write("Error: Problem retrieving experiment information");
			}
			else if(action.compareTo("remove")==0){
				if((expID = request.getParameter("expID")) != null){	
					request.getSession().removeAttribute("editingExpId");
					request.getSession().removeAttribute("experimentParams");
					request.getSession().removeAttribute("experimentRules");
					 out.write(removeExperiment(expID));			 
				}
			}
			else if(action.compareTo("reconnect")==0){
				if((expID = request.getParameter("expID")) != null){					
					 if(getExpByID(expID).getControl()!=null){
						out.write("Reconnecting...");
						getExpByID(expID).getControl().reconnect();	
					 }
					 else out.write(expID + " Control is not yet configured.");
				}
			}
			
		}
	
		
		
	
	}

	/**
	 * @see HttpServlet#(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action,redirect,successfulResult="";
		
		action=redirect=null;
		request.getSession().removeAttribute("errorMsg");
		Map<String,Object> attributes = null;
		if (request.getUserPrincipal() != null) {
			AttributePrincipal principal = (AttributePrincipal) request.getUserPrincipal();			
			attributes = principal.getAttributes();			
		}
		
		redirect = request.getParameter("redirect");
				
		if(request.getContentType().contains("multipart/form-data")){
			try{
				successfulResult = successfulResult.concat("<br/>Multipart/form-data received");
					boolean isAdmin = false;
					Iterator<String> attributeNames = attributes.keySet().iterator();
			        for (;attributeNames.hasNext();) {
						String attributeName = attributeNames.next();
						
						if(attributeName.equalsIgnoreCase("role.name")){
							if(((String)attributes.get(attributeName)).contains("administrator")){
								isAdmin = true;
							}
						}
					}
			        if(!isAdmin){
			        	log.write("Access error: a non-administrator (" + request.getRemoteUser() + ") attemped to upload a file");
			        	request.getSession().setAttribute("errorMsg", "<br/>You must have adminstrator privileges to upload files");
			        	return;
			        }
					
					//exit if file is not multipart/form-data
			        if (!ServletFileUpload.isMultipartContent(request)) {  
			        	log.write("Error: Form must have enctype=multipart/form-data.");
			        	request.getSession().setAttribute("errorMsg", "<br/>Encoding Type must be multipart/form-data");		        	
			            return;
			        }
			       
			        // configures upload settings
			        DiskFileItemFactory factory = new DiskFileItemFactory();
			        // sets memory threshold - beyond which files are stored in disk
			        factory.setSizeThreshold(MEMORY_THRESHOLD);
			        // sets temporary location to store files
			        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
			        
			        ServletFileUpload upload = new ServletFileUpload(factory);
			         
			        // sets maximum size of upload file
			        upload.setFileSizeMax(MAX_FILE_SIZE);
			         
			        // sets maximum size of request (include file + form data)
			        upload.setSizeMax(MAX_REQUEST_SIZE);
			 
			        // constructs the directory path to store upload file
			        // this path is relative to application's directory
			        String uploadPath = getServletContext().getRealPath("")
			                + File.separator + UPLOAD_DIRECTORY;
			         
			        // creates the directory if it does not exist
			        File uploadDir = new File(uploadPath);
			        if (!uploadDir.exists()) {
			            uploadDir.mkdirs();
			        }
			 
			        
			            // parses the request's content to extract file data
			            List<FileItem> formItems = upload.parseRequest(request);
			            String expID = "ID",expDesc = "Description",sinkServerAddress="Sink Address", sourceServerAddress = "Source Address";
			            
			            if (formItems != null && formItems.size() > 0) {
			                // iterates over form's fields
			                for (FileItem item : formItems) {
			                    // processes only fields that are not form fields
			                    if (!item.isFormField()) {
			                        String fileName = new File(item.getName()).getName();
			                        String filePath = uploadPath + File.separator + fileName;
			                        File storeFile = new File(filePath);
			                        File classesFile = new File(getServletContext().getRealPath("")+ File.separator + "WEB-INF" + File.separator + "classes" + File.separator + fileName.substring(0,fileName.lastIndexOf("."))+".class");
			                        if(classesFile.exists()){
			                        	request.getSession().setAttribute("errorMsg","<br/>Class file already exists. Please rename to a unique identifier.");
			                			break;
			                        }
			 
			                        // saves the file on disk
			                        item.write(storeFile);
			                        successfulResult = successfulResult.concat("<br/>Attempting to load class from: "+ filePath + " named " + fileName);
			                        if(fileName.contains(".java")){
			                        	
			                        	    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			                        	    //JavaCompiler compiler = new EclipseCompiler();
			                        	    List<String> optionList = new ArrayList<String>();
				                        	 // set compiler's classpath to be same as the runtime's
			                        	    
					                        optionList.addAll(Arrays.asList("-d",uploadPath));
					                        optionList.addAll(Arrays.asList("-classpath",System.getProperty("java.class.path")));
				                        	optionList.addAll(Arrays.asList("-cp",getServletContext().getRealPath("")+ File.separator + "WEB-INF" + File.separator + "lib" + File.separator + "rbnb.jar" + ":"
				                        	 + getServletContext().getRealPath("")+ File.separator + "WEB-INF" + File.separator + "lib" + File.separator + "segaWeb.jar" + ":"
				                        	 +  rootDir + "/lib/servlet-api.jar"));
				                        	 
			                        	    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
			           
			                        	    List<File> sourceFileList = new ArrayList<File>();
			                        	    sourceFileList.add(storeFile);
			                        	    
			                        	    Iterable<? extends JavaFileObject> compilationUnits = fileManager
			                        	        .getJavaFileObjectsFromFiles(sourceFileList);
			                        	    try {
			                        	    	FileWriter errorStream = new FileWriter(logLocation + "/CompilerErrors.txt");                        	    	
			                        	        boolean successful = compiler.getTask(errorStream, fileManager, null, optionList, null, compilationUnits).call();
			                        	        if(successful)successfulResult = successfulResult.concat("<br/>" + request.getSession().getAttribute("errorMsg") + "<br/><span style='color:green'>Successfully compiled " + fileName + "</span>");
			                        	        else{
			                        	        	request.getSession().setAttribute("errorMsg","<br/>"+request.getSession().getAttribute("errorMsg") + "<br/><span style='color:red'>Failed to compile " + fileName + "</span>");
			                        	        	break;
			                        	        }
			                        	        
			                        	    }catch(Exception e){
			                        	    	StringWriter errors = new StringWriter();
			                                	e.printStackTrace(new PrintWriter(errors));
			                            		log.write(errors.toString());
			                        	
			                        	    }
			                        	    finally {
			                        	    	fileManager.close();
			                        	    }
			                        	    
			                            
			                        }

			                        
			                        URL fileURL = new File(filePath.substring(0,filePath.lastIndexOf("/"))).toURI().toURL();
			                        log.write("Loading uploaded files from " + fileURL.getPath());
			                        successfulResult = successfulResult.concat("<br/>" + request.getSession().getAttribute("errorMsg") + "<br/>Loading uploaded files from " + fileURL.getPath());
			                        URL rbnbURL = new File(getServletContext().getRealPath("")+ File.separator + "WEB-INF" + File.separator + "lib" + File.separator + "rbnb.jar").toURI().toURL();
			                        URL segaWebHelperURL = new File(getServletContext().getRealPath("")+ File.separator + "WEB-INF" + File.separator + "lib" + File.separator + "segaWeb.jar").toURI().toURL();
			                        URL servletURL = new File(rootDir + "/lib/servlet-api.jar").toURI().toURL();
			                        URL urls [] = {fileURL,rbnbURL,segaWebHelperURL,servletURL};
			                       
			                        //TODO: Clear the classloader cache here
			                        this.getClass().getClassLoader().clearAssertionStatus(); 
			                        
			                        URLClassLoader ucl = new URLClassLoader(urls, this.getClass().getClassLoader());   
			                        
			                        //This assumes that the class is always part of the experiments package...which it should be
			                        Class<?> cl = ucl.loadClass(PACKAGE_DIR + "." + fileName.substring(0, fileName.lastIndexOf(".")));
			                        
			                        ControlClass temp = (ControlClass) cl.getDeclaredConstructor(String.class,String.class,String.class,String.class,boolean.class).newInstance(expID,expDesc,sinkServerAddress,sourceServerAddress,true);       
			                        ucl.close();
									Experiment exp = new Experiment(expID,expDesc,sinkServerAddress,sourceServerAddress);
									
									
									exp.setControl(temp);
									exp.setControlFileName(fileName.substring(0,fileName.indexOf(".")));
									experiments.add(exp); 
									successfulResult = successfulResult.concat("<br/>" + request.getSession().getAttribute("errorMsg") + "<br/><span style='color:green'>Successfully added " + expID + " as an experiment.</span><br/>Confirmation Message: " + exp.getMessage());
									log.write("New Experiment Created: " + expID);
									
									//Experiment is successfully added at this point, move the class file in the webapp class directory
									

									File classesDir = new File(getServletContext().getRealPath("")+ File.separator + "WEB-INF" + File.separator + "classes" + File.separator + PACKAGE_DIR);
									//Move .class Files to WEB-INF/classes/experiments
									successfulResult = successfulResult.concat("<br/>" + request.getSession().getAttribute("errorMsg") + "<br/>Moving files from " + uploadPath + File.separator + PACKAGE_DIR + File.separator + " to " + classesDir);
									File uploadFolder = new File(uploadPath + File.separator + PACKAGE_DIR + File.separator);
									for(File f : uploadFolder.listFiles()){
										if(f.getName().contains(".class")){
											f.renameTo(new File(classesDir, f.getName()));
										}
									}
									temp = null;
									ucl = null;
									saveConfiguration();     
									     
			                    }
			                    else{
			                    	String fieldName = item.getFieldName();
			                    	if(fieldName.compareToIgnoreCase("expID") == 0){
			                    		expID = item.getString();
			                    		if(expIDExists(expID)){
			                    			request.getSession().setAttribute("errorMsg","<br/>Experiment ID Already Exists");
			                    			break;
			                    			
			                    		}
			                    	}
			                    	else if(fieldName.compareToIgnoreCase("expDesc") == 0) expDesc = item.getString();
			                    	else if(fieldName.compareToIgnoreCase("sinkServerAddress") == 0) sinkServerAddress = item.getString();
			                    	else if(fieldName.compareToIgnoreCase("sourceServerAddress") == 0) sourceServerAddress = item.getString();
			                    	else if(fieldName.compareToIgnoreCase("redirect") == 0) redirect = item.getString();
			                    }
			                   
			                }
			            
			        }
			        }catch (MalformedURLException e) {
			        	request.getSession().setAttribute("errorMsg","<br/>There was a MalformedURLException error: " + e.getLocalizedMessage());
			        	StringWriter errors = new StringWriter();
			        	e.printStackTrace(new PrintWriter(errors));
			        	log.write(errors.toString());
			        }catch (ClassNotFoundException e) {
			        	request.getSession().setAttribute("errorMsg","<br/>There was a ClassNotFoundException error: " + e.getLocalizedMessage() +"<br/>" + e.getMessage());
			        	StringWriter errors = new StringWriter();
			        	e.printStackTrace(new PrintWriter(errors));
			        	log.write(errors.toString());
			        }catch(InstantiationException e){
			        	request.getSession().setAttribute("errorMsg",
			        			"Instantiation Exception - Possible Causes: <br/> " +
			                    "1. The class object represents an abstract class, an interface, an array class, a primitive type, or void <br/>" +
			                    "2. The class has no nullary constructor <br/>" +
			                    "3. The class does not implement ControlInterface or is not of type ControlSourceSink <br/>" +
			                    "More information available <a href='http://docs.oracle.com/javase/6/docs/api/java/lang/InstantiationException.html'>here</a>"+ "<br/><br/><br/>");
			            
			        	StringWriter errors = new StringWriter();
			        	e.printStackTrace(new PrintWriter(errors));
			        	log.write(errors.toString());
			        }catch (Exception e) {            
			        	StringWriter errors = new StringWriter();
			        	e.printStackTrace(new PrintWriter(errors));
			        	log.write(errors.toString());
			        	request.getSession().setAttribute("errorMsg","<br/>Unknown Error: " + errors.toString());
			        }finally{
			        	FileUtils.cleanDirectory(new File(getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY)); 
			        	log.write(successfulResult.concat("<br/>Cleaning directory " + getServletContext().getRealPath("") + File.separator + UPLOAD_DIRECTORY));
			        	request.getSession().setAttribute("refreshExpMenu", "true");
			        }
			        
			        
				}
		else if((action = request.getParameter("action")) != null){
				if(action.equals("getExperimentList")){				
					request.getSession().setAttribute("experiments",getExperiments(attributes));
					if(request.getSession().getAttribute("experiments") == null){
						request.getSession().removeAttribute("experimentParams");
						request.getSession().removeAttribute("experimentRules");
					}
					request.getSession().setAttribute("refreshExpMenu","false");
				}
				if(action.equals("getExperimentParameters")){
					String expID = null;
					if((expID = request.getParameter("experimentSelectMenu")) != null){
						request.getSession().setAttribute("editingExpId", expID);
						request.getSession().setAttribute("experimentParams", getExpByID(expID).getControl().getParameters());
						request.getSession().setAttribute("experimentRules", getExpByID(expID).getControl().getRules());
					}
					else{
						request.getSession().removeAttribute("editingExpId");
						request.getSession().removeAttribute("experimentParams");
						request.getSession().removeAttribute("experimentRules");
					}
				}
		}
		
		if(redirect != null){
			response.sendRedirect(redirect);
		}
    
	}
	
	/**
     * Init() checks for saved arraylist containing experiment configs, also sets up logger
     */
	@Override
    public void init(){
    	
    	try{ 
    		log = new SegaLogger(logLocation + "SegaExperimentServlet.txt");
    		log.write("Loading Experiment Configuration...");
	    	// Read from disk using FileInputStream
	    	FileInputStream fileInput = new FileInputStream(logLocation + "ExperimentArrayList.data");
	
	    	// Read object using ObjectInputStream
	    	ObjectInputStream objectInput = new ObjectInputStream(fileInput);
	
	    	// Read an object
	    	Object obj = objectInput.readObject();
	    	objectInput.close();
	    	fileInput.close();
	    	if (obj instanceof ArrayList<?>)
	    	{
	    		// Cast object to an ArrayList<Experiments>
				@SuppressWarnings("unchecked")
				ArrayList<Experiment> exps = (ArrayList<Experiment>) obj;	    		
	    	
	    		if(!experiments.equals(exps) && exps.size() >= 1){
	    			experiments = exps;
		    		log.write("Successfully Loaded The Following Experiments:\r\n");
		    		for(Experiment e : experiments){
		    			e.getControl().initialize(e.getExpId(),e.getDescription(),e.getSinkAddress(),e.getSourceAddress());
		    			log.write("\t" + e.getExpId());
		    			
		    		}
	    		}
	    		else{
	    			log.write("No experiments loaded");
	    			
	    		}
	    	}
	    	else 
	    		log.write(logLocation + "ExperimentArrayList.data NOT an instance of ArrayList");

    	}catch(FileNotFoundException e){
    		StringWriter errors = new StringWriter();
        	e.printStackTrace(new PrintWriter(errors));
    		log.write(errors.toString());
    	}catch(IOException e){
    		StringWriter errors = new StringWriter();
        	e.printStackTrace(new PrintWriter(errors));
    		log.write(errors.toString());
    	}catch(ClassNotFoundException e){
    		StringWriter errors = new StringWriter();
        	e.printStackTrace(new PrintWriter(errors));
    		log.write(errors.toString());
    	}catch(Exception e){
    		StringWriter errors = new StringWriter();
        	e.printStackTrace(new PrintWriter(errors));
    		log.write(errors.toString());
    	}
    	
    }
	
	public boolean expIDExists(String expID){
		for(Experiment e : experiments){
			if(e.getExpId().compareToIgnoreCase(expID) == 0) return true;			
		}
		return false;
	}
	
	public static Experiment getExpByID(String expID){
		for(Experiment e : experiments){
			if(e.getExpId().compareToIgnoreCase(expID) == 0) return e;			
		}
		return null;
	}
	
	public void saveConfiguration(){
		log.write("Saving current configuration as \"ExperimentArrayList.data\"");
		try{
			// Write to disk with FileOutputStream
			FileOutputStream fileOutput = new FileOutputStream(logLocation + "ExperimentArrayList.data");
	
			// Write object with ObjectOutputStream
			ObjectOutputStream objectOutput = new ObjectOutputStream (fileOutput);
	
			// Write object out to disk
			objectOutput.writeObject (experiments);
			objectOutput.flush();
			objectOutput.close();
			fileOutput.close();
		}catch(IOException e){
			StringWriter errors = new StringWriter();
        	e.printStackTrace(new PrintWriter(errors));
    		log.write(errors.toString());
		}catch(Exception e){
			StringWriter errors = new StringWriter();
        	e.printStackTrace(new PrintWriter(errors));
    		log.write(errors.toString());
		}
		
	}
	
	public static ArrayList<Experiment> getExperiments(Map<String,Object> attributes){	
		ArrayList<Experiment> exps = new ArrayList<Experiment>();	
		String role = (String)attributes.get("role.name");
		if(role.contains("administrator") && experiments.size() >= 1){	
			return experiments;
		}
		else if(experiments.size() >=1 ){
			Iterator<String> attributeNames = attributes.keySet().iterator();
			
	        for (;attributeNames.hasNext();) {
				String attributeName = attributeNames.next();
				
				if(attributeName.equalsIgnoreCase("experiment_name")){
					exps.add(getExpByID((String)attributes.get(attributeName)));
				}
			}
	        return exps;	        
		}     
		else 
			return null;
		
	}

	public String getExperimentsMenu(){
		String result = "";

		if(experiments.size() > 0){
			result = result.concat("<select id=\"experimentMenu\" style=\"max-width:290px;min-width:200px;\">");
			for(Experiment e : experiments){
				result = result.concat("<option value=\""+e.getExpId()+"\">"+e.getExpId()+"</option>");
			}
			result = result.concat("</select>");
		}
		else result = "No Experiments Saved";
		return result;
		
	}
	
	
	public String removeExperiment(String expID){
		try{
			if(getExpByID(expID).getControl()!= null){
				String fileName = getExpByID(expID).getControlFileName();
				getExpByID(expID).getControl().shutdown();
				
				if(experiments.remove(experiments.indexOf(getExpByID(expID))) != null){
					log.write("Deleted Experiment: " + expID);
					File classesFolder = new File(getServletContext().getRealPath("")+ File.separator + "WEB-INF" + File.separator + "classes" + File.separator + "experiments");
					for(File f : classesFolder.listFiles()){
						if(f.getName().contains(fileName)){
							f.delete();
						}
					}
					log.write("Removed File: " + fileName);
					saveConfiguration();
					return "<br/><br/>Successfully removed " + expID + "<br/>";		
				}
				else{
					log.write("Failed to delete: " + expID);
					return "Could not remove " + expID + "<br/>";
				}
			}
			else{
				if(experiments.remove(getExpByID(expID))){
					log.write("Deleted Experiment: " + expID);
					saveConfiguration();
					return "Successfuly Removed " + expID + "<br/>";
				}
				else{
					log.write("Failed to delete: " + expID);
					return "Could not remove " + expID + "<br/>";
				}
			}
			
			
		}catch(Exception e){
			StringWriter errors = new StringWriter();
        	e.printStackTrace(new PrintWriter(errors));
    		log.write(errors.toString());
    		return errors.toString();
		}
		
		
	}
	
	@Override
	public void destroy(){
		log.write("Destroy override received - SegaExperimentServlet");
		saveConfiguration();
		for(Experiment e : experiments){
			log.write("Disconnecting " + e.getExpId());
			e.getControl().shutdown();
		}

	}
	public ArrayList<String> getExperimentList(Map<String,Object> attributes){	
		ArrayList<String> exps = new ArrayList<String>();	
		String role = (String)attributes.get("role.name");
		if(role.contains("administrator")){	
			for(Experiment e : experiments){
				exps.add(e.getExpId());
			}
		}
		else{
			Iterator<String> attributeNames = attributes.keySet().iterator();
			
	        for (;attributeNames.hasNext();) {
				String attributeName = attributeNames.next();
				
				if(attributeName.equalsIgnoreCase("experiment_name")){
					exps.add((String)attributes.get(attributeName));
				}
			}
	        
		}
		
		return exps;
        
		
	}


}
