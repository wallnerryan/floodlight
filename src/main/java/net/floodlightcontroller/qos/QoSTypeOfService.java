package net.floodlightcontroller.qos;

public class QoSTypeOfService implements Comparable<QoSTypeOfService>{
	
	//TODO create params
	
	public int priority;

	
	public QoSTypeOfService(){
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
	public int compareTo(QoSTypeOfService policy) {
		return this.priority - ((QoSTypeOfService)policy).priority;
    }

}
