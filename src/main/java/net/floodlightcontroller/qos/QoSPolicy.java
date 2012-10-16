package net.floodlightcontroller.qos;

import net.floodlightcontroller.qos.QoSPolicy;

public class QoSPolicy implements Comparable<QoSPolicy>{
	
	//TODO create params
	
	public int priority;

	
	public QoSPolicy(){
		//TODO create this.param = value
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
