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
	
	private float capacity = Float.POSITIVE_INFINITY;
	private Link linkattribute = null;
	
	private float requirement;// = 2.0f;
	//public float globalflow;
	public float currentlinklength;
	public float currentextremalflow;
	
	public CustomizedLink(Link link,Float cap,Float req){
		//super();
		this.linkattribute = link;
		this.requirement = req;
		this.capacity = cap;
		this.currentlinklength=0.0f;
	}
	
	public CustomizedLink(Link link){
		this.linkattribute = link;
		this.requirement = 0.0f;
		this.currentlinklength=0.0f;
	}


	
	public void setCapacity(float cap){
		this.capacity = cap;
	}
	
	public float getCapacity(){
		return this.capacity;
	}
	
	
	
	
	
	public void setrequirement(float req){
		this.requirement = req;
	}
	
	public float getrequirement(){
		return this.requirement;
	}
	
	
	@Override
    public String toString() {
		return "Customized"+linkattribute.toString()+' '+
				"Customized Field [ requirement="+ this.requirement+
				" capacity=" + this.capacity+
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
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (linkattribute.getDst().getLong() ^ (linkattribute.getDst().getLong() >>> 32));
        result = prime * result + linkattribute.getDstPort().getPortNumber();
        result = prime * result + (int) (linkattribute.getSrc().getLong() ^ (linkattribute.getSrc().getLong() >>> 32));
        result = prime * result + linkattribute.getSrcPort().getPortNumber();
        return result; /* do not include latency */
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