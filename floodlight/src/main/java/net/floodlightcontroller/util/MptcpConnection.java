package net.floodlightcontroller.util;

import java.util.ArrayList;

import net.floodlightcontroller.routing.Path;

import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.TransportPort;

public class MptcpConnection {
	private IPv4Address primarySourceIP;
	private IPv4Address primaryDestinationIP;
	private int primarySourcePort;
	private int primaryDestinationPort;
	private byte[] token;
	private byte[] senderKey;
	private byte[] receiverKey;
	ArrayList<IPPath> iproutes;
	
	public MptcpConnection(IPv4Address sourceIP,IPv4Address destinationIP,int sourcePort,
			int destinationPort, byte [] senderKey){
		this.primarySourceIP = sourceIP;
		this.primaryDestinationIP = destinationIP;
		this.primarySourcePort = sourcePort;
		this.senderKey = senderKey;
		iproutes = new ArrayList<IPPath>();
	}
	public void addRoutes(IPv4Address source, IPv4Address destination,ArrayList<Path> v,int subNum){
		IPPath n = new IPPath(source,destination,v,subNum);
		this.iproutes.add(n);
	}
	public ArrayList<Path> getAllRoutes(IPv4Address source,IPv4Address destination){
		
		ArrayList<Path> n = new ArrayList<Path>();
		for(int i=0;i<iproutes.size();i++){
			if(source.equals(iproutes.get(i).getSourceIp()) && destination.equals(iproutes.get(i).getDestinationIp())){
				return iproutes.get(i).getAvailableRoutes();
			}
		}
			return n;
	}
	
	public void setRoutes(IPv4Address source,IPv4Address destination, ArrayList<Path> v){
		for(int i=0;i<iproutes.size();i++){
			if(source.equals(iproutes.get(i).getSourceIp()) && destination.equals(iproutes.get(i).getDestinationIp())){
				 iproutes.get(i).setAvailableRoutes(v);
			}
		}
	}
	public boolean ipsAlreadySeen(IPv4Address source,IPv4Address destination){
		for(int i=0;i<iproutes.size();i++){
			if(source.equals(iproutes.get(i).getSourceIp()) && destination.equals(iproutes.get(i).getDestinationIp())){
				return true;
			}
		}
			return false;
	}
	public Path getNextRoute(IPv4Address source,IPv4Address destination){
		Path newRoute=null;
		for(int i=0;i<iproutes.size();i++){
			if(source.equals(iproutes.get(i).getSourceIp()) && destination.equals(iproutes.get(i).getDestinationIp())){
				IPPath ir = iproutes.get(i);
				newRoute=ir.getNextPath();
				ir.increaseCurrentSubflowNumber();
			}
		}
		return newRoute;
	}
	

	
	public IPv4Address getPrimarySourceIP() {
		return primarySourceIP;
	}

	public void setPrimarySourceIP(IPv4Address primarySourceIP) {
		this.primarySourceIP = primarySourceIP;
	}

	public IPv4Address getPrimaryDestinationIP() {
		return primaryDestinationIP;
	}

	public void setPrimaryDestinationIP(IPv4Address primaryDestinationIP) {
		this.primaryDestinationIP = primaryDestinationIP;
	}

	public int getPrimarySourcePort() {
		return primarySourcePort;
	}

	public void setPrimarySourcePort(int primarySourcePort) {
		this.primarySourcePort = primarySourcePort;
	}

	public int getPrimaryDestinationPort() {
		return primaryDestinationPort;
	}

	public void setPrimaryDestinationPort(int primaryDestinationPort) {
		this.primaryDestinationPort = primaryDestinationPort;
	}

	public byte[] getToken() {
		return token;
	}

	public void setToken(byte[] token) {
		this.token = token;
	}

	public byte[] getSenderKey() {
		return senderKey;
	}

	public void setSenderKey(byte[] senderKey) {
		this.senderKey = senderKey;
	}

	public byte[] getReceiverKey() {
		return receiverKey;
	}

	public void setReceiverKey(byte[] receiverKey) {
		this.receiverKey = receiverKey;
	}
}
