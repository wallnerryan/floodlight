package net.floodlightcontroller.qos;

import java.util.Collection;
import java.util.Map;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
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
import net.floodlightcontroller.storage.IStorageSourceListener;
//import net.floodlightcontroller.storage.StorageException;
//import org.codehaus.jackson.JsonParseException;
//import org.codehaus.jackson.JsonParser;
//import org.codehaus.jackson.JsonToken;
//import org.codehaus.jackson.map.MappingJsonFactory;

import net.floodlightcontroller.core.IFloodlightProviderService;
import java.util.ArrayList;
import java.util.Set;
//import org.openflow.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QoSTool implements IOFSwitchListener, IStaticFlowEntryPusherService,
		IOFMessageListener, IFloodlightModule, IStorageSourceListener {
	
	protected IFloodlightProviderService floodlightProvider;
	protected IStorageSourceService storageSource; //stores QoS delegations
	protected Map<String, Byte> standardPolicies;
	protected Map<String, Byte> programmablePolicies;
	protected IRestApiService restApi;
	protected static Logger logger;
	protected byte diffServValue;
	protected IPacket payLoad;
	
	public static final String TABLE_NAME = "controller_staticflowtableentry";
	public static final String COLUMN_NAME = "name";
	//add values to match on, list/array values final array<List> COLUMN_VALUES
	public static final String COLUMN_NW_TOS = "nw_tos";
	public static String ColumnNames[] = { COLUMN_NAME, COLUMN_NW_TOS };
	
	@Override
	public String getName() {
		return "QoSPolicyTool";
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// Needed for ordering of packet_in processing chain
		// UNUSED
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// should modify packet before going to forwarding. 
		// We need to go before forwarding
        return (type.equals(OFType.PACKET_IN) && name.equals("forwarding"));
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		//This module should depend on FloodlightProviderService
		Collection<Class<? extends IFloodlightService>> l =
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IStorageSourceService.class);
	    //l.add(IRestApiService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		logger = LoggerFactory.getLogger(QoSTool.class);

	}
	
	public void setStorageSource(IStorageSourceService storageSource) {
        this.storageSource = storageSource;
    }
	
	@Override
	public void rowsModified(String tableName, Set<Object> rowKeys) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rowsDeleted(String tableName, Set<Object> rowKeys) {
		// TODO Auto-generated method stub
		
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
	public void addedSwitch(IOFSwitch sw) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removedSwitch(IOFSwitch sw) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void startUp(FloodlightModuleContext context) {
		//May not need to listen to PACKET_IN's
		//Could be used to retreive ToS bits,
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		
		
		// TODO add a listener or method to get flows/flow counts etc.
	}
	
	public Byte getProgramableRuleBits(String ruleName){
		//TODO get the QoS bits
		return null;
	}
	
	public void setProgramableRule(String ruleName, Short qosBits){
		//TODO set new rule
	}
	
	public void loadStandardPolicies(){
		//TODO load qos.conf and import standard policies 
	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
                IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		
		payLoad = eth.getPayload();
		if ( payLoad instanceof IPv4) {
				System.out.println("IPv4 Packet");
				IPv4 ip = (IPv4) eth.getPayload();
				diffServValue = ip.getDiffServ();
				
		}
		else{
				payLoad = eth.toString();
				System.out.println("Packet Payload is: "+payLoad);
		}
		
		return Command.CONTINUE;
	}
}
