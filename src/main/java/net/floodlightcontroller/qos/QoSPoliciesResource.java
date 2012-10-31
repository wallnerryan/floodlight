package net.floodlightcontroller.qos;

/**
* Copyright 2012 Marist College, New York

* Author Ryan Wallner (ryan.wallner1@marist.edu)
* 
*  Licensed under the Apache License, Version 2.0 (the "License"); you may
*  not use this file except in compliance with the License. You may obtain
*  a copy of the License at
*
*         http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
*  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
*  License for the specific language governing permissions and limitations
*  under the License.
*    
*  Provides Queuing and L2/L3 Quality of Service Policies to a 
*  Virtualized Network using DiffServ class based model, and certain OVS queuing techniques
*  This modules provides overlapping flowspace for policies that governed by their priority
*  as in the firewall flowspace. This QoS modules acts in a proactive manner haveing to abide
*  by existing "Policies" within a network.
*
**/

import java.io.IOException;
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
	public static Logger logger = LoggerFactory.getLogger(QoSPoliciesResource.class);
	
	
	@Get("json")
	public Object handleRequest(){
		IQoSService qos = 
                (IQoSService)getContext().getAttributes().
                get(IQoSService.class.getCanonicalName());

		// gets the list of policies currently being implemented
        return qos.getPolicies();
	}
	
	 /**
     * Takes a QoS Policy Rule string in JSON format and parses it into
     * our firewall rule data structure, then adds it to the qos polcies storage.
     * @param fmJson The qos policy entry in JSON format.
     * @return A string status message
     */
    @Post
    public String add(String qosJson) {
    	IQoSService qos = 
    			(IQoSService)getContext().getAttributes().
    			get(IQoSService.class.getCanonicalName());
    	
    	//create empty policy
    	QoSPolicy policy;
    	try{
    		policy = jsonToPolicy(qosJson);
    	}
    	catch(IOException e){
    		logger.error("Error Parsing Quality of Service Policy to JSON: {}, Error: {}", qosJson, e);
    		e.printStackTrace();
    		return "{\"status\" : \"Error! Could not parse polocy, see log for details.\"}";
    	}
    	
    	/**
		 * 	TODO
		 *  !!!!!!!!!!!!!!!!!!!!!!!!!!
		 */
    	
    	String status = null;
    	status = "Policy Added";
    	return ("{\"status\" : \"" + status + "\"}");
    }
    
    @Delete
    public String delete(String qosJson) {
    	
    	String status = null;
    	status = "Policy Deleted";
    	return ("{\"status\" : \"" + status + "\"}");
    }
    
    public static QoSPolicy jsonToPolicy(String pJson) throws IOException{
		QoSPolicy policy = new QoSPolicy();
		
		/**
		 * 	TODO
		 *  !!!!!!!!!!!!!!!!!!!!!!!!!!
		 */
		
    	return policy;
    }
    


}
