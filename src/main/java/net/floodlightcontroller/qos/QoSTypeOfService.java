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
* code adopted from Firewall
* @author Amer Tahir
* @edited KC Wang
**/

public class QoSTypeOfService implements Comparable<QoSTypeOfService>{
	
	//TODO create params
	
	public int sid;
	public String name;
	
	//default best effort
	public byte tos = 0x00;
	
	public QoSTypeOfService(){
		//TODO create this.param = value
		this.sid = -1;
		this.name = null;
		this.tos = 0x00;
	}
	
	//*******************************
	//*******************************
	//TODO CREATE GETTERS AND SETTERS?
	//*******************************
	//*******************************
	
	/**
     * Generates a unique ID for the instance
     * 
     * @return int representing the unique id
     */
    public int genID() {
        int uid = this.hashCode(); //need to create??
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
	public int compareTo(QoSTypeOfService policy) {
		return this.tos - ((QoSTypeOfService)policy).tos;
    }
	
	/**
	 * Check whether a service is the same
	 * @param service
	 * @return
	 */
	public boolean isSameAs(QoSTypeOfService service){
		//check if either the name of the service of the ToS bits match
		if((this.name == service.name)
				|| (this.tos == service.tos)){
		return true;
		}
		else{
			//if not, return false
			return false;
		}
	}

}
