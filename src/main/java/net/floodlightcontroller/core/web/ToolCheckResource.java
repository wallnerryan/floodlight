package net.floodlightcontroller.core.web;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.floodlightcontroller.firewall.IFirewallService;
import net.floodlightcontroller.qos.IQoSService;

public class ToolCheckResource extends ServerResource {
	public static Logger logger = LoggerFactory.getLogger(ToolCheckInfo.class);
	
    	public class ToolCheckInfo{
    		IQoSService qos = 
                    (IQoSService)getContext().getAttributes().
                    get(IQoSService.class.getCanonicalName());
    		IFirewallService firewall = 
                    (IFirewallService)getContext().getAttributes().
                    get(IFirewallService.class.getCanonicalName());
    		
    		protected Properties prop = new Properties();
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
			
    				
    				/** Feature request: add the turn on on start-up properties
    				 *  to the init function of the tool using the getName() functions
    				 *  as the name of the tool, use this name for the properties
    				 *  file too. **/
    				
    				//Return tools
    				for (int i=0; i<tools.length; i++){
    					//Tool information from properties file
    					currentTool = tools[i];
    					is_enabled = prop.getProperty(tools[i]);
    					toolSet.put(currentTool, is_enabled);
    					//System.out.println(is_enabled);
    					if (is_enabled.equals("enabled")){
    						//Enable the tools
    						if (currentTool.equals("qos")){
    							qos.enableQoS(true);
    						}
    						else if (currentTool.equals("firewall")){
    							firewall.enableFirewall(true);
    						}
    							logger.debug("Tool {} enabled on startup", currentTool);
    					}
    					else{logger.debug("Tool {} is disabled on startup", currentTool);}
    				}
    			} catch (FileNotFoundException e) {
    				e.printStackTrace();
    			} catch(IOException e) {
    				e.printStackTrace();
    			}
    			logger.debug("Toolset is {}",toolSet);
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
