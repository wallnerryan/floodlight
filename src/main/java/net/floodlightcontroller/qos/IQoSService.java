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
    public void addTypeOfService(QoSTypeOfService tos);

    /**
     * Deletes a Type of Service
     */
    public void deleteTypeOfService(int tosid);

    /**
     * Returns all of the QoS rules
     * @return List of all rules
     */
    public List<QoSPolicy> getRules();
    
    /**
     * Adds a QoS Policy
     */
    public void addPolicy(QoSPolicy policy);

    /**
     * Deletes a QoS Policy
     */
    public void deletePolicy(int policyid);
    
}
