package net.floodlightcontroller.util;

import java.util.ArrayList;

import net.floodlightcontroller.routing.Path;

import org.projectfloodlight.openflow.types.IPv4Address;


public class IPPath {
	IPv4Address sourceIP;
	IPv4Address destinationIP;
	ArrayList<Path> availablePath;
	int currentSubflowNumber;
	
	public IPPath(IPv4Address source, IPv4Address destination, ArrayList<Path> v, int sub_num){
		this.sourceIP = source;
		this.destinationIP = destination;
		this.availablePath = v;
		this.currentSubflowNumber = sub_num;
	}
	
	public Path getNextPath(){
		return availablePath.get(currentSubflowNumber);
	}
	
	public void increaseCurrentSubflowNumber(){
		if(++(this.currentSubflowNumber)>=availablePath.size()){
			this.currentSubflowNumber = 0 ; 
		}
	}
	
	public int getSubflowNumber(){
		return currentSubflowNumber;
	}
	
	public IPv4Address getSourceIp(){
		return sourceIP;
	}
	
	
	public void setSourceIp(IPv4Address sourceIp) {
		this.sourceIP = sourceIp;
	}

	public IPv4Address getDestinationIp() {
		return destinationIP;
	}

	public void setDestinationIp(IPv4Address destinationIp) {
		this.destinationIP = destinationIp;
	}

	public ArrayList<Path> getAvailableRoutes() {
		return availablePath;
	}

	public void setAvailableRoutes(ArrayList<Path> availableRoutes) {
		this.availablePath = availableRoutes;
	}
}
