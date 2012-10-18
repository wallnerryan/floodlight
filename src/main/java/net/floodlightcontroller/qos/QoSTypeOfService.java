package net.floodlightcontroller.qos;

public class QoSTypeOfService implements Comparable<QoSTypeOfService>{
	
	//TODO create params
	
	public int uid;
	public String name;
	
	//default best effort
	public byte tos = 0x00;

	
	public QoSTypeOfService(){
		//TODO create this.param = value
		this.uid = -1;
		this.name = null;
		this.tos = 0x00;
	}
	
	//*******************************
	//*******************************
	//TODO CREATE GETTERS AND SETTERS
	//*******************************
	//*******************************
	
	/**
     * Comparison method for Collections.sort method
     * @param rule the rule to compare with
     * @return number representing the result of comparison
     * 0 if equal
     * negative if less than 'rule'
     * greater than zero if greater priority rule than 'rule'
     */
	public int compareTo(QoSTypeOfService policy) {
		return this.priority - ((QoSTypeOfService)policy).priority;
    }

}
