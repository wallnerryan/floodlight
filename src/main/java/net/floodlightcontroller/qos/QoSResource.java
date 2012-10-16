package net.floodlightcontroller.qos;


import net.floodlightcontroller.qos.IQoSService;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.restlet.resource.Post;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QoSResource extends ServerResource{
	  protected static Logger log = LoggerFactory.getLogger(QoSResource.class);
	
	
	 @Get("json")
	 public Object handleRequest() {
		 String op = (String) getRequestAttributes().get("op");
	        IQoSService qos = 
	                (IQoSService)getContext().getAttributes().
	                get(IQoSService.class.getCanonicalName());
	        
	        if (op.equalsIgnoreCase("enable")) {
	            qos.enableQoS(true);
	            return "{\"status\" : \"success\", \"details\" : \"QoS Enabled\"}";
	        }else if (op.equalsIgnoreCase("status")) {
	            return qos.isEnabled();
	        }else if (op.equalsIgnoreCase("disable")) {
	        	qos.enableQoS(false);
	         return "{\"status\" : \"success\", \"details\" : \"QoS Disabled\"}";
	        }
		 
		 return "{\"status\" : \"failure\", \"details\" : \"invalid operation\"}";
	 }

}
