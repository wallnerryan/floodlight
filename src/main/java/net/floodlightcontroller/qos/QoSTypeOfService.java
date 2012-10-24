package net.floodlightcontroller.qos;

/**
 * 
 * @author ryan wallner 
 * 
 */

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
