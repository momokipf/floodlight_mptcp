package net.floodlightcontroller.fdmcalculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.floodlightcontroller.linkdiscovery.Link;


public class CustomizedLink extends Link{
	
	/*
	 Now, the attribute of capacity is implemented in the class <@Links>
	 */
	
	//private float capacity = Float.POSITIVE_INFINITY;
	private Link linkattribute = null;
	
	private float requirement;// = 2.0f;
	//public float globalflow;
	public float currentlinklength;
	public float currentextremalflow;
	
	public CustomizedLink(Link link,Float cap,Float req){
		//super();
		this.linkattribute = link;
		this.requirement = req;
		this.currentlinklength=Float.POSITIVE_INFINITY;
	}
	
	
	/*
	public void setcapaity(float f){
		
	}
	
	public float getcpacity(){
		return this.getCapacity();
	}
	*/
	
	
	
	
	public void setrequirement(float req){
		this.requirement = req;
	}
	
	public float getrequirement(){
		return this.requirement;
	}
	
	
	@Override
    public String toString() {
		return "Customized"+this.linkattribute.toString()+'\n'+
				"Customized Field [ requirement="+ this.requirement+
				"]";
    }
	
	public String toKeyString() {
    	return (this.linkattribute.getSrc().toString() + "|" +
    			this.linkattribute.getSrcPort().toString() + "|" +
    			this.linkattribute.getDst().toString() + "|" +
    		    this.linkattribute.getDstPort().toString());
    }
	
	@Override
    public int hashCode() {
        return super.hashCode();
    }
	 
	public int compareTo(CustomizedLink a) {
		return super.compareTo(a.linkattribute);
	}
	
	 @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CustomizedLink other = (CustomizedLink) obj;
        if (!this.linkattribute.getDst().equals(other.linkattribute.getDst()))
            return false;
        if (!this.linkattribute.getDstPort().equals(other.linkattribute.getDstPort()))
            return false;
        if (!this.linkattribute.getSrc().equals(other.linkattribute.getSrc()))
            return false;
        if (!this.linkattribute.getSrcPort().equals(other.linkattribute.getSrcPort()))
            return false;
        return true; /* do not include latency and other field */
    }
	
}