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
*  LIMITED: End-to-End policies are only supported for a single OF-Cluster
*  
**/

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionEnqueue;
import org.openflow.protocol.action.OFActionNetworkTypeOfService;
import org.openflow.protocol.action.OFActionType;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;
import net.floodlightcontroller.storage.IStorageSourceService;
import net.floodlightcontroller.qos.QoSPolicy;
import net.floodlightcontroller.qos.QoSTypeOfService;
import net.floodlightcontroller.core.IFloodlightProviderService;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QoS implements IQoSService, IFloodlightModule,
		IOFMessageListener {
	
	protected IFloodlightProviderService floodlightProvider;
	protected IStaticFlowEntryPusherService flowPusher;
	protected List<QoSPolicy> policies;
	protected List<QoSTypeOfService> services;
	protected IRestApiService restApi;
	protected IStorageSourceService storageSource;
	protected Properties props = new Properties();
	protected String[] tools;
	protected static Logger logger;
	
	public boolean enabled;
	
	public static final String TABLE_NAME = "controller_qos";
	public static final String COLUMN_POLID = "policyid";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_MATCH_PROTOCOL = "protocol";
	public static final String COLUMN_MATCH_ETHTYPE = "eth-type";
	public static final String COLUMN_MATCH_INGRESSPRT = "ingressport";
	public static final String COLUMN_MATCH_IPDST = "ipdst";
	public static final String COLUMN_MATCH_IPSRC = "ipsrc";
	public static final String COLUMN_MATCH_VLANID = "vlanid";
	public static final String COLUMN_MATCH_ETHSRC = "ethsrc";
	public static final String COLUMN_MATCH_ETHDST = "ethdst";
	public static final String COLUMN_MATCH_TCPUDP_SRCPRT = "tcpudpsrcport";
	public static final String COLUMN_MATCH_TCPUDP_DSTPRT = "tcpudpdstport";
	public static final String COLUMN_NW_TOS = "nw_tos";
	public static final String COLUMN_SW = "switche";
	public static final String COLUMN_QUEUE = "queue";
	public static final String COLUMN_ENQPORT = "equeueport";
	public static final String COLUMN_PRIORITY = "priority";
	public static final String COLUMN_SERVICE = "service";
	public static String ColumnNames[] = { COLUMN_POLID,
			COLUMN_NAME,COLUMN_MATCH_PROTOCOL, COLUMN_MATCH_ETHTYPE,COLUMN_MATCH_INGRESSPRT,
			COLUMN_MATCH_IPDST,COLUMN_MATCH_IPSRC,COLUMN_MATCH_VLANID,
			COLUMN_MATCH_ETHSRC,COLUMN_MATCH_ETHDST,COLUMN_MATCH_TCPUDP_SRCPRT,
			COLUMN_MATCH_TCPUDP_DSTPRT,COLUMN_NW_TOS,COLUMN_SW,
			COLUMN_QUEUE,COLUMN_ENQPORT,COLUMN_PRIORITY,COLUMN_SERVICE,};
	
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
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return false;
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
        l.add(IStaticFlowEntryPusherService.class);
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
		
		//initiate services
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		flowPusher = context.getServiceImpl(IStaticFlowEntryPusherService.class);
		logger = LoggerFactory.getLogger(QoS.class);
		storageSource = context.getServiceImpl(IStorageSourceService.class);
        restApi = context.getServiceImpl(IRestApiService.class);
        policies = new ArrayList<QoSPolicy>();
        services = new ArrayList<QoSTypeOfService>();
        logger = LoggerFactory.getLogger(QoS.class);
        // start disabled
        // can be overridden by tools.properties.
        enabled = false;
        try {
			//load a properties file
			props.load(new FileInputStream("src/main/resources/tools.properties"));
			tools = props.getProperty("tools").split(",");
			System.out.println(props.getProperty("qos"));
			if(props.getProperty("qos").equalsIgnoreCase("enabled")){
				logger.info("Enabling QoS on Start-up. Edit tools.properties to change this.");
				this.enableQoS(true);
			}
        }catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void startUp(FloodlightModuleContext context) {
		// initialize REST interface
        restApi.addRestletRoutable(new QoSWebRoutable());
        // start qos if enabled at bootup
        if (this.enabled == true) {
        	//TODO
        	//NOT NEEDED, BUT CAN DO SOMETHING INTERESTING
            floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
        }
        //debug
        logger.debug("Creating QoS tables");
        
        //Storage for policies
        storageSource.createTable(TABLE_NAME, null);
        storageSource.setTablePrimaryKeyName(TABLE_NAME, COLUMN_POLID);
        //avoid thread issues for concurrency
        synchronized (policies) {
            this.policies = readPoliciesFromStorage(); 
            }
        
        //Storage for services
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
	}
	
	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		
		// do not process packet if not enabled
		if (!this.enabled) return Command.CONTINUE;
		
		switch (msg.getType()) {
        case FLOW_MOD:
            break;
        default:
            return Command.CONTINUE;
        }
		// if the flowmod is a policy
		//************************************
		//************************************
		// Perform matching on policies, output "getting QoS, rulname, ToS or Queue
		// logger recieving <types of qos> qos in this network
		//************************************
		//************************************
		
		return Command.CONTINUE;
	}
	
	/**
	 * Allow access to enable module
	 * @param boolean
	 */
	@Override
	public void enableQoS(boolean enable) {
		logger.info("Setting QoS to {}", enable);
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
        		//un-ordered, naturally a short list
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
	 * Add a policy-flowMod to all switches in network
	 * @param policy
	 */
	@Override
	public void addPolicyToNetwork(QoSPolicy policy) {
		OFFlowMod flow = policyToFlowMod(policy);
		logger.info("adding policy-flow {} to all switches",flow.toString());
		//add to all switches
		Map<Long, IOFSwitch> switches = floodlightProvider.getSwitches();
		//simple check
		if(!(switches.isEmpty())){
			// NEEDS OPTIMIZATION (push to context?)
			for(IOFSwitch sw : switches.values()){
				if(!(sw.isConnected())){
					break;// cannot add
				}
				//logger.info("Switch : {} DPID : {}",sw.getStringId(), sw.getId());
				logger.info("Add flow Name: {} Flow: {} Switch "+ sw.getStringId(), 
				policy.name, flow.toString());
				flowPusher.addFlow(policy.name, flow, sw.getStringId());	
			}
		}
	}
	
	/**
	 * Adds a policy to a switch (dpid)
	 * @param QoSPolicy policy
	 * @param String sw
	 */
	@Override
	public void addPolicy(QoSPolicy policy, String swid) {
		OFFlowMod flow = policyToFlowMod(policy);
		logger.info("Adding policy-flow {} to switch {}",flow.toString(),swid);
		//add to all switches
		flowPusher.addFlow(policy.name, flow, swid);
	}
	
	/**
	 * 
	 * @param policy
	 */
	@Override
	public void deletePolicyFromNetwork(QoSPolicy policy) {
	
	}
	
	
	/**
	 * Delete policy from a switch (dpid)
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
	 * 	Called when sws = "all"
	 *  @author wallnerryan
	 *  @overloaded
	**/
	@Override
	public void addPolicy(QoSPolicy policy){
		//debug
		logger.debug("Adding Policy to List and Storage");
		//create the UID
		policy.policyid = policy.genID();
		
		int p = 0;
		for (p = 0; p < this.policies.size(); p++){
			//check if empy
			if(this.policies.isEmpty()){
				//p is zero
				break;
			}
			//starts at the first(lowest) policy based on priority
			//insertion sort, gets hairy when n # of switches increases. 
			//larger networks may need a merge sort.
			if(this.policies.get(p).priority >= policy.priority){
				//this keeps i to the correct positions to place new policy in
				break;
			}
		}
		if (p <= this.policies.size()) {
			this.policies.add(p, policy);
			} else {
				this.policies.add(policy);
	        }	
		//Add to the storageSource
		Map<String, Object> policyEntry = new HashMap<String, Object>();
		policyEntry.put(COLUMN_POLID, Long.toString(policy.policyid));
		policyEntry.put(COLUMN_NAME, policy.name);
		policyEntry.put(COLUMN_MATCH_PROTOCOL, Short.toString(policy.protocol));
		policyEntry.put(COLUMN_MATCH_ETHTYPE, Short.toString(policy.ethtype));
		policyEntry.put(COLUMN_MATCH_INGRESSPRT, Short.toString(policy.ingressport));
		policyEntry.put(COLUMN_MATCH_IPSRC, Integer.toString(policy.ipsrc));
		policyEntry.put(COLUMN_MATCH_IPDST, Integer.toBinaryString(policy.ipdst));
		policyEntry.put(COLUMN_MATCH_VLANID, Short.toString(policy.vlanid));
		policyEntry.put(COLUMN_MATCH_ETHSRC, policy.ethsrc);
		policyEntry.put(COLUMN_MATCH_ETHDST, policy.ethdst);
		policyEntry.put(COLUMN_MATCH_TCPUDP_SRCPRT, Short.toString(policy.tcpudpsrcport));
		policyEntry.put(COLUMN_MATCH_TCPUDP_DSTPRT, Short.toString(policy.tcpudpdstport));
		policyEntry.put(COLUMN_NW_TOS, policy.service);
		policyEntry.put(COLUMN_SW, policy.sw);
		policyEntry.put(COLUMN_QUEUE, Short.toString(policy.queue));
		policyEntry.put(COLUMN_ENQPORT, Short.toString(policy.enqueueport));
		policyEntry.put(COLUMN_PRIORITY, Short.toString(policy.priority));
		policyEntry.put(COLUMN_SERVICE, policy.service);
		storageSource.insertRow(TABLE_NAME, policyEntry);
		
		/**
		* Possibly a place to add a list of switches to add to
		**/
		
		//regex for dpid string, this can/needs to be more elegent. Maybe use of a Matcher
		final String dpidPattern = "^[\\d|\\D][\\d|\\D]:[\\d|\\D][\\d|\\D]:[\\d|\\D][\\d|\\D]" +
				":[\\d|\\D][\\d|\\D]:[\\d|\\D][\\d|\\D]:[\\d|\\D][\\d|\\D]:[\\d|\\D][\\d|\\D]:[\\d|\\D][\\d|\\D]$";
		
		if (policy.sw.equals("all")){
			logger.debug("Adding Policy {} to Entire Network", policy.toString());
			addPolicyToNetwork(policy);
		}
		else if (policy.sw.equals("none")){
			logger.debug("Adding Policy {} to Controller", policy.toString());
		}
		//add to a specified switch b/c "all" not specified
		else if(policy.sw.matches(dpidPattern)){
			logger.debug("Adding policy {} to Switch {}", policy.toString(), policy.sw);
			// add appropriate hex string converted to a long type
			addPolicy(policy, policy.sw);
			}	
		else{
			logger.error("***Policy {} error at switch input {} ***", policy.toString(), policy.sw);
		}
	}
	
	/** Deletes a policy
	 *  @author wallnerryan
	 *  @overloaded
	**/
	@Override
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
		
		//from sw get the value "all" or "dpid"
		//either way removes from list b/c sw policy's have a single flow instance
		//deletePolicy(id, swid) or deletePolicyFromNetwork
	}
	
	/**
	 * Returns a flowmod from a policy
	 * @param policy
	 * @return
	 */
	public OFFlowMod policyToFlowMod(QoSPolicy policy){
		//initialize a match structure that matches everything
		OFMatch match = new OFMatch();
		//Based on the policy match appropriately.
		
		//no wildcards
		match.setWildcards(0);
		
		if(policy.ethtype != -1){
			match.setDataLayerType((policy.ethtype));
			//debug
			logger.debug("setting match on eth-type");
		}
		if(policy.protocol != -1){
			match.setNetworkProtocol(policy.protocol);
			//debug
			logger.debug("setting match on protocol ");
		}
		if(policy.ingressport != -1){
			match.setInputPort(policy.ingressport);
			//debug
			logger.debug("setting match on ingress port ");
		}
		if(policy.ipdst != -1){
			match.setNetworkDestination(policy.ipdst);
			//debug
			logger.debug("setting match on network destination");
		}
		if(policy.ipsrc != -1){
			match.setNetworkSource(policy.ipsrc);
			//debug
			logger.debug("setting match on network source");
		}
		if(policy.vlanid != -1){
			match.setDataLayerVirtualLan(policy.vlanid);
			//debug
			logger.debug("setting match on VLAN");
		}
		if(policy.tos != -1){
			match.setNetworkTypeOfService(policy.tos);
			//debug
			logger.debug("setting match on ToS");
		}
		if(policy.ethsrc != null){
			match.setDataLayerSource(policy.ethsrc);
			//debug
			logger.debug("setting match on data layer source");
		}
		if(policy.ethdst != null){
			match.setDataLayerDestination(policy.ethdst);
			//debug
			logger.debug("setting match on data layer destination");
		}
		if(policy.tcpudpsrcport != -1){
			match.setTransportSource(policy.tcpudpsrcport);
			//debug
			logger.debug("setting match on transport source port");
		}
		if(policy.tcpudpdstport != -1){
			match.setTransportDestination(policy.tcpudpdstport);
			//debug
			logger.debug("setting match on transport destination");
		}
		
		//Create a flow mod using the previous match structure
		OFFlowMod fm = new OFFlowMod();
		fm.setType(OFType.FLOW_MOD);
		//depending on the policy nw_tos or queue the flow mod
		// will change the type of service bits or enqueue the packets
		/**TODO**/ 
		if(policy.queue > -1 && policy.service == null){
			logger.info("This policy is a queuing policy");
			List<OFAction> actions = new ArrayList<OFAction>();
			
			//add the queuing action
			OFActionEnqueue enqueue = new OFActionEnqueue();
			enqueue.setLength((short) 0xffff);
			enqueue.setType(OFActionType.OPAQUE_ENQUEUE); // I think this happens anyway in the constructor
			enqueue.setPort(policy.enqueueport);
			enqueue.setQueueId(policy.queue);
			actions.add((OFAction)enqueue);
			
			logger.info("Match is : {}", match.toString());
			//add the matches and actions and return
			fm.setMatch(match)
				.setActions(actions)
				.setIdleTimeout((short) 0)  // infinite
				.setHardTimeout((short) 0)  // infinite
				.setBufferId(OFPacketOut.BUFFER_ID_NONE)
				.setFlags((short) 0)
				.setOutPort(OFPort.OFPP_NONE.getValue())
				.setPriority(policy.priority)
				.setLengthU((short)OFFlowMod.MINIMUM_LENGTH + OFActionEnqueue.MINIMUM_LENGTH);
				
		}
		else if(policy.queue == -1 && policy.service != null){
			logger.info("This policy is a type of service policy");
			List<OFAction> actions = new ArrayList<OFAction>();
			
			//add the queuing action
			OFActionNetworkTypeOfService tosAction = new OFActionNetworkTypeOfService();
			tosAction.setType(OFActionType.SET_NW_TOS);
			tosAction.setLength((short) 0xffff);
			
			//Find the appropriate type of service bits in policy
			Byte pTos = null;
			List<QoSTypeOfService> serviceList = this.getServices();
			for(QoSTypeOfService s : serviceList){
				if(s.name.equals(policy.service)){
					//policy's service ToS bits
					pTos = s.tos;
				}
			}
			tosAction.setNetworkTypeOfService(pTos);
			actions.add((OFAction)tosAction);
			
			logger.info("Match is : {}", match.toString());
			//add the matches and actions and return.class.ge
			fm.setMatch(match)
				.setActions(actions)
				.setIdleTimeout((short) 0)  // infinite
				.setHardTimeout((short) 0)  // infinite
				.setBufferId(OFPacketOut.BUFFER_ID_NONE)
				.setFlags((short) 0)
				.setOutPort(OFPort.OFPP_NONE.getValue())
				.setPriority(Short.MAX_VALUE)
				.setLengthU((short)OFFlowMod.MINIMUM_LENGTH + OFActionNetworkTypeOfService.MINIMUM_LENGTH);
		}
		else{
			logger.error("Policy Misconfiguration");
		}
	return fm;	
	}
}
