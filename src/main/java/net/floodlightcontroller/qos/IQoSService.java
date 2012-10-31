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
*
**/

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
     * 
     * @param policy
     */
    public void addPolicyToNetwork(QoSPolicy policy);
    
    /**
     * 
     * @param policy
     */
    public void deletePolicyFromNetwork(QoSPolicy policy);
    
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
