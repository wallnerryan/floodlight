package net.floodlightcontroller.qos;

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
	
	//If it is queuing, must ignore ToS bits. and set "enqueue".
	public boolean is_queuing;
	public short queueid;
	
	//default type of service to best effort
	public byte nw_tos = 0x00;
	
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
		this.is_queuing = false;
		this.queueid = -1;
		this.nw_tos = 0x00;
		this.priority = 0;
		
	}
	
	//***********************************
	//***********************************
	//TODO Getters and Setters for params
	//***********************************
	//***********************************
	
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
	
}
