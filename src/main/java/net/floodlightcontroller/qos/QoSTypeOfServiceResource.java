package net.floodlightcontroller.qos;

import net.floodlightcontroller.qos.IQoSService;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class QoSTypeOfServiceResource extends ServerResource {
	public static Logger log = LoggerFactory.getLogger(QoSTypeOfServiceResource.class);
	
	
	@Get("json")
	public Object handleRequest(){
		IQoSService qos = 
                (IQoSService)getContext().getAttributes().
                get(IQoSService.class.getCanonicalName());

		// gets the list of policies currently being implemented
        return qos.getRules();
	}
	 /**
     *
     * @param tosJson The qos policy entry in JSON format.
     * @return A string status message
     */
    @Post
    public String store(String tosJson) {
    	
    	String status = null;
    	status = "Type Of Service Added";
    	return ("{\"status\" : \"" + status + "\"}");
    }
    
    @Delete
    public String delete(String tosJson) {
    	
    	String status = null;
    	status = "Type Of Service Deleted";
    	return ("{\"status\" : \"" + status + "\"}");
    }


}
