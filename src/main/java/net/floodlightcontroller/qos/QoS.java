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
	protected List<QoSTypeOfService> services;
	protected IRestApiService restApi;
	protected static Logger logger;
	protected byte diffServValue;
	protected Integer queueId;
	protected IPacket payLoad;
	public boolean enabled;
	protected IStorageSourceService storageSource;
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
	public static final String COLUMN_IS_QUE = "is_queuing";
	public static final String COLUMN_QUEUEID = "queueid";
	public static String ColumnNames[] = { COLUMN_NAME, 
										   COLUMN_NW_TOS, 
										   COLUMN_MATCH_PROTOCOL,
										   COLUMN_MATCH_INGRESSPRT,
										   COLUMN_MATCH_IPDST,
										   COLUMN_MATCH_IPSRC,
										   COLUMN_MATCH_VLANID,
										   COLUMN_MATCH_ETHSRC,
										   COLUMN_MATCH_ETHDST,
										   COLUMN_MATCH_TCPUDP_SRCPRT,
										   COLUMN_MATCH_TCPUDP_DSTPRT,
										   COLUMN_NW_TOS};
	
	public static final String TOS_TABLE_NAME = "controller_qos_tos";
	public static final String COLUMN_TOSID = "tosid";
	public static final String COLUMN_TOSNAME = "tosname";
	public static final String COLUMN_TOSBITS = "tosbits";
	public static String TOSColumnNames[] = {COLUMN_TOSNAME,
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
        
		return l;
    }
    
    /**
     * Reads the types of services from the storage and creates a sorted arraylist of QoSTypeOfService
     * from them.
     * @return the sorted arraylist of Type of Service instances (rules from storage)
     */
    protected ArrayList<QoSTypeOfService> readServicesFromStorage() {
        ArrayList<QoSTypeOfService> l = new ArrayList<QoSTypeOfService>();
        
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
        // storage, create table and read rules
        storageSource.createTable(TABLE_NAME, null);
        storageSource.setTablePrimaryKeyName(TABLE_NAME, COLUMN_POLID);
        //avoid thread issues for concurrency
        synchronized (policies) {
            this.policies = readPoliciesFromStorage(); 
            }
        storageSource.createTable(TOS_TABLE_NAME, null);
        storageSource.setTablePrimaryKeyName(TOS_TABLE_NAME, COLUMN_TOSID);
        synchronized (services) {
            this.services = readServicesFromStorage(); 
            }
		
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
		
		payLoad = eth.getPayload();
		if ( payLoad instanceof IPv4) {
				IPv4 ip = (IPv4) eth.getPayload();
				diffServValue = ip.getDiffServ();
		}
		else{
				payLoad = eth.getPayload();
				payloadStr = payLoad.toString();
				System.out.println("Not a Layer 3 Packet, Packet Payload is: "+payloadStr);
		}
		
		//************************************
		//************************************
		// Perform matching on policies, output "getting QoS, rulname, ToS or Queue
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

	@Override
	public void enableQoS(boolean enable) {
		logger.info("Setting QoS to {}", enabled);
        this.enabled = enable;	
	}
	
	@Override
	public boolean isEnabled(){
	    	return this.enabled;
	}

	@Override
	public List<QoSPolicy> getRules() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addTypeOfService(QoSTypeOfService tos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteTypeOfService(int tosid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addPolicy(QoSPolicy policy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deletePolicy(int policyid) {
		// TODO Auto-generated method stub
		
	}
	
	//*********************************************************************************
	//*********************************************************************************
	
}
