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
*  Provides Queuing and L3 Quality of Service Policies to a 
*  Virtualized Network using DiffServ class based model, and certain OVS queuing techniques
*  MySQL DB is needed for persistent storage of QoS policies.
**/

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPacket;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;
import net.floodlightcontroller.storage.IStorageSourceService;
//import net.floodlightcontroller.qos.QoSDBStorageSource;
import net.floodlightcontroller.qos.QoSPolicy;
import net.floodlightcontroller.qos.QoSTypeOfService;
import net.floodlightcontroller.core.IFloodlightProviderService;
//import org.codehaus.jackson.JsonParseException;
//import org.codehaus.jackson.JsonParser;
//import org.codehaus.jackson.JsonToken;
//import org.codehaus.jackson.map.MappingJsonFactory;

import java.util.ArrayList;
//import org.openflow.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QoS implements IQoSService, IFloodlightModule, IStaticFlowEntryPusherService,
		IOFMessageListener {
	
	protected IFloodlightProviderService floodlightProvider;
	protected List<QoSPolicy> policies;
	protected List<QoSPolicy> e2ePolicies;
	protected List<QoSTypeOfService> services;
	protected IRestApiService restApi;
	protected IStorageSourceService storageSource;
	protected static Logger logger;
	
	public boolean enabled;
	public boolean is_queueing;
	
	public static final Byte default_qos = 0x00;
	
	//*****************************************************************
	//*****************************************************************
	//** Un-implemented persistence storage. Using storage source first
	//protected QoSDBStorageSource storageSource = new QoSDBStorageSource(); //stores QoS delegations
	//*****************************************************************
	//*****************************************************************
	
	public static final String TABLE_NAME = "controller_qos";
	public static final String COLUMN_POLID = "policyid";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_MATCH_PROTOCOL = "protocol";
	public static final String COLUMN_MATCH_INGRESSPRT = "ingressport";
	public static final String COLUMN_MATCH_IPDST = "ipdst";
	public static final String COLUMN_MATCH_IPSRC = "ipsrc";
	public static final String COLUMN_MATCH_VLANID = "vlanid";
	public static final String COLUMN_MATCH_ETHSRC = "ethsrc";
	public static final String COLUMN_MATCH_ETHDST = "ethdst";
	public static final String COLUMN_MATCH_TCPUDP_SRCPRT = "tcpudpsrcport";
	public static final String COLUMN_MATCH_TCPUDP_DSTPRT = "tcpudpdstport";
	public static final String COLUMN_NW_TOS = "nw_tos";
	public static final String COLUMN_QUEUE = "queue";
	public static final String COLUMN_IS_E2E= "e2e";
	public static String ColumnNames[] = { COLUMN_POLID,
										   COLUMN_NAME, 
										   COLUMN_MATCH_PROTOCOL,
										   COLUMN_MATCH_INGRESSPRT,
										   COLUMN_MATCH_IPDST,
										   COLUMN_MATCH_IPSRC,
										   COLUMN_MATCH_VLANID,
										   COLUMN_MATCH_ETHSRC,
										   COLUMN_MATCH_ETHDST,
										   COLUMN_MATCH_TCPUDP_SRCPRT,
										   COLUMN_MATCH_TCPUDP_DSTPRT,
										   COLUMN_NW_TOS,
										   COLUMN_QUEUE,
										   COLUMN_IS_E2E,
										   };
	
	public static final String TOS_TABLE_NAME = "controller_qos_tos";
	public static final String COLUMN_SID = "serviceid";
	public static final String COLUMN_SNAME = "servicename";
	public static final String COLUMN_TOSBITS = "tosbits";
	public static String TOSColumnNames[] = {COLUMN_SID,
											 COLUMN_SNAME,
											 COLUMN_TOSBITS};
	
	
	@Override
	public String getName() {
		return "qostool";
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// Needed for ordering of packet_in processing chain
		// UNUSED
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// Should process packet before going to forwarding. 
        return (type.equals(OFType.PACKET_IN) &&
        		name.equals("forwarding"));
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		Collection<Class<? extends IFloodlightService>> l = 
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IQoSService.class);
        return l;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>,
        IFloodlightService> m = 
        new HashMap<Class<? extends IFloodlightService>,
        IFloodlightService>();
        // We are the class that implements the service
        m.put(IQoSService.class, this);
        return m;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		//This module should depend on FloodlightProviderService
		Collection<Class<? extends IFloodlightService>> l =
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IStorageSourceService.class);
        l.add(IRestApiService.class);
		return l;
	}
	
	/**
     * Reads the policies from the storage and creates a sorted arraylist ofQoSPolicy
     * from them.
     * @return the sorted arraylist of Policy instances (rules from storage)
     */
    protected ArrayList<QoSPolicy> readPoliciesFromStorage() {
        ArrayList<QoSPolicy> l = new ArrayList<QoSPolicy>();
        // *****************************************
        // *****************************************
        //TODO 
        // *****************************************
        // *****************************************
		return l;
    }
    
    /**
     * Reads the types of services from the storage and creates a sorted arraylist of QoSTypeOfService
     * from them.
     * @return the sorted arraylist of Type of Service instances (rules from storage)
     */
    protected ArrayList<QoSTypeOfService> readServicesFromStorage() {
        ArrayList<QoSTypeOfService> l = new ArrayList<QoSTypeOfService>();
        // *****************************************
        // *****************************************
        //TODO 
        // *****************************************
        // *****************************************
		return l;
    }

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		logger = LoggerFactory.getLogger(QoS.class);
		storageSource = context.getServiceImpl(IStorageSourceService.class);
        restApi = context.getServiceImpl(IRestApiService.class);
        policies = new ArrayList<QoSPolicy>();
        e2ePolicies = new ArrayList<QoSPolicy>();
        services = new ArrayList<QoSTypeOfService>();
        logger = LoggerFactory.getLogger(QoS.class);
        
        // start disabled
        enabled = false;
	}
	
	@Override
	public void startUp(FloodlightModuleContext context) {
		// initialize REST interface
        restApi.addRestletRoutable(new QoSWebRoutable());
        // start qos if enabled at bootup
        if (this.enabled == true) {
            floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
        }
        //debug
        logger.debug("Creating QoS tables");
        // storage, create table and read rules
        storageSource.createTable(TABLE_NAME, null);
        storageSource.setTablePrimaryKeyName(TABLE_NAME, COLUMN_POLID);
        //avoid thread issues for concurrency
        synchronized (policies) {
            this.policies = readPoliciesFromStorage(); 
            }
        storageSource.createTable(TOS_TABLE_NAME, null);
        storageSource.setTablePrimaryKeyName(TOS_TABLE_NAME, COLUMN_SID);
        synchronized (services) {
            this.services = readServicesFromStorage(); 
            }
        
        // create default "Best Effort" service
        // most networks use this as default, adding here for defaulting
        QoSTypeOfService service = new QoSTypeOfService();
        service.name = "Best Effort";
        service.tos = (byte)0x00;
        service.sid = service.genID();
        this.addService(service);
		
		//*****************************************************************
		//*****************************************************************
		//** Un-implemented persistence storage. Using storage source first
		//storageSource.connectToDB();
		//this.loadPolicies(); //from DB
		//*****************************************************************
		//*****************************************************************
		//** Un-implemented persistence storage. Using storage source first
		
	}
	
	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		
		// do not process packet if not enabled
		if (!this.enabled) return Command.CONTINUE;
		
		String payloadStr = new String();
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
                IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		
		
		//TESTTING, REDO THIS!
		IPacket payLoad = eth.getPayload();
		if ( payLoad instanceof IPv4) {
				IPv4 ip = (IPv4) eth.getPayload();
				byte diffServValue = ip.getDiffServ();
				logger.debug("DiffServ value is {}", diffServValue);
		}
		else{
				payLoad = eth.getPayload();
				payloadStr = payLoad.toString();
				logger.debug("Not a Layer 3 Packet, Packet Payload is {}" ,payloadStr);
		}
		
		//************************************
		//************************************
		// Perform matching on policies, output "getting QoS, rulname, ToS or Queue
		// logger recieving <types of qos> qos in this network
		//************************************
		//************************************
		
		return Command.CONTINUE;
	}

	@Override
	public void addFlow(String name, OFFlowMod fm, String swDpid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteFlow(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteFlowsForSwitch(long dpid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAllFlows() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Map<String, OFFlowMod>> getFlows() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, OFFlowMod> getFlows(String dpid) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Allow access to enable module
	 * @param boolean
	 */
	@Override
	public void enableQoS(boolean enable) {
		logger.info("Setting QoS to {}", enabled);
        this.enabled = enable;	
	}
	
	/**
	 * Return whether of not the module is enabled
	 */
	@Override
	public boolean isEnabled(){
	    	return this.enabled;
	}
	/**
	 * Return a List of Quality of Service Policies
	 */
	@Override
	public List<QoSPolicy> getPolicies() {
		return this.policies;
	}
	
	/**
	 * Return a list of policies that provide end to end QoS
	 * @return List
	 */
	@Override
	public List<QoSPolicy> getE2EPolicies() {
		return this.e2ePolicies;
	}

	/**
	 * Add a service class to use in policies
	 * Used to make ToS/DiffServ Bits human readable. 
	 * Bit notation 000000 becomes "Best Effort"
	 * @param QoSTypeOfService
	 */
	@Override
	public void addService(QoSTypeOfService service) {
		//debug
		logger.debug("Adding Service to List and Storage");
		//create the UID
		service.sid = service.genID();
		
		//check tos bits are within bounds
        if (service.tos >= (byte)0x00 && service.tos <= (byte)0x3F ){
        	try{
        		//Add to the list of services
        		this.services.add(service);
        		
        		//add to the storage source
        		Map<String, Object> serviceEntry = new HashMap<String,Object>();
        		serviceEntry.put(COLUMN_SID, Integer.toString(service.sid));
        		serviceEntry.put(COLUMN_SNAME, service.name);
        		serviceEntry.put(COLUMN_TOSBITS, Byte.toString(service.tos));
        		
        		//ad to storage
        		storageSource.insertRow(TOS_TABLE_NAME, serviceEntry);
        		
        	}catch(Exception e){
        		logger.debug("Error adding service, error: {}" ,e);
        	}
        }
        else{
        	logger.debug("Type of Service must be 0-64");
        }
	}
	
	/**
	 * Returns a list of services available for Network Type of Service
	 * @return List
	 */
	
	@Override
	public List<QoSTypeOfService> getServices() {
		return this.services;
	}

	/**
	 * Removes a Network Type of Service
	 */
	@Override
	public void deleteService(int sid) {
		/**
		// TODO called when @DELETE HTTP
		* gets id from a list of SERVICE in web
		* remove from list and from storage
		**/
	}
	
	/**
	 * Adds a policy to a single switch
	 * @param QoSPolicy policy
	 * @param String sw
	 */
	@Override
	public void addPolicy(QoSPolicy policy, long swid) {
		/**
		// TODO Auto-generated method stub
		* called by web routable if add -> sw dpid
		* generate policyid
		* 
		* adds a policy to *policies and storageSource
		* and addToNetwork because it will loop through switches
		**/
	}
	
	/**
	 * Delete policy from a single switch
	 * @param policyid
	 * @param sw
	 */
	@Override
	public void deletePolicy(int policyid, long swid) {
		/**
		// TODO Auto-generated method stub
		* called by web routable if delete -> sw dpid
		* removes policy from list and storage
		* removes all flows that match the policy
		**/
	}
	
	/** Adds a policy to all switches
	 *  @author wallnerryan
	 *  @overloaded
	**/
	public void addPolicy(QoSPolicy policy){
		/**
		//TODO add to all switches in network
		* get switches in network, each switch add policy
		* uses addPolicy*
		* called by web routable if add -> all
		* create a flow out of a policy
		* push flow to context
		*
		* FUTURES
		* (catch) what if a switch does not support queuing or tos? ofconfig? 
		**/
	}
	
	/** Deletes a policy to all switches
	 *  @author wallnerryan
	 *  @overloaded
	**/
	public void deletePolicy(long policyid){
		/**
		//TODO add to all switches in network
		* get switches in network, each switch add policy
		* uses deletePolicy*
		* called by web routable if delete -> all
		* remove from storage and list
		* remove the flows that match the policy.name b/c policy.name == flow name
		*
		**/
	}
	
	/** Creates qos along a routed path
	 *  @author wallnerryan
	**/
	public void addEndToEndQoS(long srcSwId, long dstSwId, QoSPolicy policy){
		//TODO possibly integrate with IRoutingService?
		// called by web routable if  host1/to/host2
		// used addToSwitch from hops (switched) in the end to end path
	}
	
	//*********************************************************************************
	//*********************************************************************************
	
}
