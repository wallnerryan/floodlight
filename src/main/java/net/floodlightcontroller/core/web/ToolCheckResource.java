package net.floodlightcontroller.core.web;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class ToolCheckResource extends ServerResource {
	
    	public static class ToolCheckInfo{
    		
    	protected static Properties prop = new Properties();
        protected String[] tools;
        protected String is_enabled;
        protected String currentTool;
        protected String toolName;
        
    		
    	public HashMap<String,String> getTools(){
    		HashMap<String,String> toolSet = 
    				new HashMap<String,String>();
    	try {
			//load a properties file
			prop.load(new FileInputStream("src/main/resources/tools.properties"));
			tools = prop.getProperty("tools").split(",");
			
			//Return only the enabled tools
			for (int i=0; i<tools.length; i++){
				//Tool information from properties file
				currentTool = tools[i];
				is_enabled = prop.getProperty(tools[i]);
				toolSet.put(currentTool, is_enabled);
				if (is_enabled.contains("enabled")){
					if (currentTool == "qos"){toolName = "QoS";}
					else if (currentTool == "firewall"){toolName = "Firewall";}
					else {toolName = null;}
					if (classExists("net.floodlightcontroller."+currentTool+"."+toolName)){
						//*************************************
						//*************************************
						//TODO call each tool's (enable method)
						//*************************************
						//*************************************
						}
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			}
    	System.out.println(toolSet);
    	return toolSet;
    	}
    	
    	//Courtesy of Jacob
    	public boolean classExists(String className)
    	{
    	try {
    	Class.forName (className);
    	return true;
    	}
    	catch (ClassNotFoundException exception) {
    	return false;
    	}
    	}
    	
    	public ToolCheckInfo(){
    		this.getTools();
    	}
    }
    	
    	@Get("json")
        public ToolCheckInfo toolCheck() {
    		//return a simple list of tool for the webUI to get
            return new ToolCheckInfo();
        }
}
