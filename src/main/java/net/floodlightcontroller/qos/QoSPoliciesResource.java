package net.floodlightcontroller.qos;


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


public class QoSPoliciesResource extends ServerResource {
	public static Logger log = LoggerFactory.getLogger(QoSPoliciesResource.class);
	
	
	@Get("json")
	public Object handleRequest(){
		IQoSService qos = 
                (IQoSService)getContext().getAttributes().
                get(IQoSService.class.getCanonicalName());

		// gets the list of policies currently being implemented
        return qos.getRules();
	}
	 /**
     * Takes a QoS Policy Rule string in JSON format and parses it into
     * our firewall rule data structure, then adds it to the qos polcies storage.
     * @param fmJson The qos policy entry in JSON format.
     * @return A string status message
     */
    @Post
    public String store(String qosJson) {
    	
    	String status = null;
    	status = "Policy Added";
    	return ("{\"status\" : \"" + status + "\"}");
    }
    
    @Delete
    public String delete(String qosJson) {
    	
    	String status = null;
    	status = "Policy Added";
    	return ("{\"status\" : \"" + status + "\"}");
    }


}
