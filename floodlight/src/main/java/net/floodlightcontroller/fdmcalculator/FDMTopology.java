package net.floodlightcontroller.fdmcalculator;


import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.U64;

import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.routing.Path;
import net.floodlightcontroller.routing.PathId;


class FDMTopology {

	
	//LinkedList<Link> allLinks = new LinkedList<Link>();
	private ArrayList<CustomizedLink> allLinks;// = new ArrayList<CustomizedLink>();
	private int switchesnum;
	//private Set<CustomizedLink> linkset;// = new HashSet<CustomizedLink>();
	private Map<CustomizedLink,Integer> invertlinkmap;
	private Map<PathId,List<LinkedList<Integer>>> adjlinkfromswitch;  // Assume one allocation per node
	
	private Float[] req;
	
	Float total_requirement = 0.0f;
	
	
	Integer msgLen = 1;
		
	public FDMTopology(Integer msgLen, Map<DatapathId, Set<Link>> topLinks) {
		
		ArrayList<CustomizedLink> cusLinks = new ArrayList<CustomizedLink>();
		
		for(DatapathId s:topLinks.keySet()){
			for(Link link:topLinks.get(s)){
				int currentIndex = cusLinks.size();
				CustomizedLink cuslink = new CustomizedLink(link,Float.MAX_VALUE,0.0f);
				cusLinks.add(cuslink);
				this.invertlinkmap.put(cuslink, currentIndex);
			}
		}
		switchesnum = topLinks.keySet().size();
		adjlinkfromswitch = new HashMap<PathId,List<LinkedList<Integer>>>();
		
		initRequirements();
	}
	
	
	
	public void addPathtoTopology(List<Path> paths){
		
		for(Path path:paths){
			List<NodePortTuple> nstlist = path.getPath();
			ArrayList<LinkedList<Integer>> ll = null;
			if(this.adjlinkfromswitch.containsKey(path.getId())){
				ll = (ArrayList<LinkedList<Integer>>)adjlinkfromswitch.get(path.getId());
			}
			else{
				ll = new ArrayList<LinkedList<Integer>>();
			}
			LinkedList<Integer> l = new LinkedList<Integer>();
			for(NodePortTuple n: nstlist){
				int index = nstlist.indexOf(n);	
				if(index!=nstlist.size()-1){
					Link link = new Link(n.getNodeId(),n.getPortId(),nstlist.get(index+1).getNodeId(),nstlist.get(index+1).getPortId(),U64.of(0L));
					int lindex = -1;
					if(invertlinkmap.containsKey((CustomizedLink)link)){
						lindex = invertlinkmap.get((CustomizedLink)link);
						// for test, assume req = 2.0f;
						if(index==0){
							allLinks.get(lindex).setrequirement(2.0f);
							this.total_requirement += 2.0f;
						}
					}
					else{
						System.out.println("Can find "+link.toKeyString());
						break;
					}
					l.addLast(lindex);
				}
			}
			ll.add(l);
		}
		
	}
	
	private void initRequirements(){
		
	}
	
	
	public List<CustomizedLink> getallLinks(){
		return this.allLinks;
	}
	
	public Map<PathId,List<LinkedList<Integer>>> getadj(){
		return this.adjlinkfromswitch;
	}
	
	public CustomizedLink getCustomizedLink(int index){
		return allLinks.get(index);
	}
	
	public Integer getCustomizedLinkindex(CustomizedLink cl){
		return this.invertlinkmap.get(cl);
	}
	
	public Integer getNoLinks() {
		return allLinks.size();
	}

	public Integer getNoNodes() {
		return switchesnum;
	}
	
//	public void initCapacity(Float[] linkCapacities) {
////		capacity = new Float[getNoLinks()];
//		capacity = linkCapacities;
//	}
//	
//	public void initRequirements(Float [][] a_req) {
//		for(Integer i = 0; i < getNoNodes(); i++) {
//			for(Integer j = 0; j < getNoNodes(); j++) {
//				if (a_req[i][j] > 0) {
//					req[i][j] = a_req[i][j];
//					total_requirement += a_req[i][j];
//				}
//				else {
//					req[i][j] = 0.0f;
//				}
//			}
//		}
//	}
	
//	public void initCapacity(Float[] a_cap) {
//		//System.arraycopy(a_cap, 0, capacity, 0, capacity.length);
//	}

//	public Integer getEnd1(Integer index) {
//		Link currentLink = allLinks.get(index);
//		int node = nodes.indexOf(currentLink.getSrc());
////		System.out.println("End 1 Value: " + node);
//		return node;
//	}

//	public Integer getEnd2(Integer index) {
//		Link currentLink = allLinks.get(index);
//		int node = nodes.indexOf(currentLink.getDst());
////		System.out.println("End 1 Value: " + node);
//		return node;
//	}

	public Float getTotal_requirement() {
		return total_requirement;
	}

//	public LinkedList<Integer>[] getAdj() {
//		return nodeAdjLinks;
//	}
//
//	public Float[] getCapacity() {
//		return capacity;
//	}

	public Integer getMsgLen() {
		return msgLen;
	}
	
	
	
	
}