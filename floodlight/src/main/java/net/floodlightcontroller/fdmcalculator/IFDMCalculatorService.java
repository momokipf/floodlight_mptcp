
/**
 * Interface for FDMCaculator
 */
package net.floodlightcontroller.fdmcalculator;


import java.util.Map;
import java.util.List;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.routing.Path;
/**
 * @author Waleed Ishrat Rahman and Krit Sae Fang
 * For UCLA CS218 Project - Fall 2016
 * Not all functions/methods are implemented. Stubs left for future work
 */
public interface IFDMCalculatorService extends IFloodlightService {
//	/**
//	 * Add a flow to the FDM for calculation
//	 * @param srcSwitchID - ID for the source node
//	 * @param desSwitchID - ID for the destination node
//	 * @param requestBW - Requested bandwidth for this flow
//	 */
//	public void addFlow(DatapathId srcNodeID, DatapathId desNodeID, double requestBW);
//	
//	/**
//	 * Remove a flow from FDM calculation
//	 * THIS IS OUTSIDE FALL 2016 SCOPE - ADDED AS TEMPLATE FOR FUTURE WORK
//	 * @param srcSwitchID - ID for the source node
//	 * @param desSwitchID - ID for the destination node
//	 */
//	public void removeFlow(DatapathId srcNodeID, DatapathId desNodeID);
//	
//	/**
//	 * Return the bandwidth for the link between two nodes
//	 * @param srcSwitchID - ID for the source node
//	 * @param desSwitchID - ID for the destination node
//	 * @return bandwidth
//	 */
//	public double getFlowBW(DatapathId srcNodeID, DatapathId desNodeID);

	/**
	 * Return the bandwidth for the link between two nodes
	 * @param srcSwitchID - ID for the source node
	 * @param desSwitchID - ID for the destination node
	 * @return bandwidth
	 */
	public Float getFlowBW(Link link);

	/**
	 * Return the bandwidth for the link between two nodes
	 * @param srcSwitchID - ID for the source node
	 * @param desSwitchID - ID for the destination node
	 * @return bandwidth
	 */
	public Float getFlowBW(IOFSwitch currentSwitch, OFPort currentPort,IOFSwitch nextSwitch, OFPort nextPort);
	
	/*
	 * Describe: the path need to add into FDM module
	 * @param  the path need to add into FDM module
	 * No return
	 */
	public void addPath(Path p);
	
	/*
	 * Return all rules set by user
	 */
	public Map<String,List<Float>> getRules();
	
	public void addRule(String nodetuple,Float req,Float cap);
	
	public void clearRule();
}