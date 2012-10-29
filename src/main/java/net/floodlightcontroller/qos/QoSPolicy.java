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
*  as in the firewall flowspace. This QoS modules acts in a pro-active manner having to abide
*  by existing "Policies" within a network.
*  
* code adopted from Firewall
* @author Amer Tahir
* @edited KC Wang
**/

import net.floodlightcontroller.qos.QoSPolicy;

public class QoSPolicy implements Comparable<QoSPolicy>{
	
	//create params
	public long policyid;
	public String name;
	public byte protocol;
	public short ingressport;
	public long ipdst;
	public long ipsrc;
	public short vlanid;
	public long ethsrc;
	public long ethdst;
	public short tcpudpsrcport;
	public short tcpudpdstport;
	public boolean e2e;
	
	//Can be "all" or a "dpid"
	public String sw;
	
	//If it is queuing, must ignore ToS bits. and set "enqueue".
	public short queue;
	
	//default type of service to best effort
	public String nw_tos;
	
	//Defaulted Priority
	public int priority = 0;
	

	public QoSPolicy(){
		this.policyid = 0;
		this.name = null;
		this.protocol = 0x00;
		this.ingressport = 0;
		this.ipdst = 0;
		this.ipsrc = 0;
		this.vlanid = -1;
		this.ethsrc = 0;
		this.ethdst = 0;
		this.tcpudpdstport = 0;
		this.tcpudpsrcport = 0;
		this.e2e = false;
		this.sw = "all";
		this.queue = -1;
		this.nw_tos = null;
		this.priority = 0;
		
	}
	
	/**
     * Generates a unique ID for the instance
     * 
     * @return int representing the unique id
     */
    public int genID() {
        int uid = this.hashCode();
        if (uid < 0) {
            uid = Math.abs(uid);
            uid = uid * 15551;
        }
        return uid;
    }
	
	/**
     * Comparison method for Collections.sort method
     * @param rule the rule to compare with
     * @return number representing the result of comparison
     * 0 if equal
     * negative if less than 'rule'
     * greater than zero if greater priority rule than 'rule'
     */
	public int compareTo(QoSPolicy policy) {
		return this.priority - ((QoSPolicy)policy).priority;
    }
	
	/**
	 * 
	 * @param policy
	 * @return
	 */
	public boolean isSameAs(QoSPolicy policy){
		
		//TODO add more to this check
		
		if (this.equals(policy)){
			return true;
		}
		else{
			return false;
		}
	}
	
}
