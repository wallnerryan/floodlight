package net.floodlightcontroller.qos;

import java.util.List;

import net.floodlightcontroller.qos.QoSPolicy;
import net.floodlightcontroller.core.module.IFloodlightService;


public interface IQoSService extends IFloodlightService {
	
	/**
     * Enables/disables the Quality of Service Tool.
     * @param enable Whether to enable or disable the Tool.
     */
    public void enableQoS(boolean enable);
    
    /**
     * Checked the enabledness
     * @param boolean.
     */
    public boolean isEnabled();
    
    /**
     * Adds a Type of Service
     */
    public void addService(QoSTypeOfService service);
    
    /**
     * Adds a Type of Service
     */
    public List<QoSTypeOfService> getServices();

    /**
     * Deletes a Type of Service
     */
    public void deleteService(int sid);

    /**
     * Returns all of the QoS rules
     * @return List of all rules
     */
    public List<QoSPolicy> getPolicies();
    
    /**
     * Returns all of the QoS rules
     * @return List of all rules
     */
    public List<QoSPolicy> getE2EPolicies();
    
    /**
     * Adds a QoS Policy
     */
    public void addPolicy(QoSPolicy policy, long swid);

    /**
     * Deletes a QoS Policy
     */
    public void deletePolicy(int policyid, long swid);
    
    /**
     * adds policy to switch
     */
    public void addPolicy(QoSPolicy policy);
    
    /**
     * deletes policy ffrom switch
     */
    public void deletePolicy(long dpid);
}
