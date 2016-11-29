/**
 * Interface for FDMCaculator
 */
package net.floodlightcontroller.fdmcalculator;

import org.projectfloodlight.openflow.types.DatapathId;

import net.floodlightcontroller.linkdiscovery.Link;

/**
 * @author Waleed Ishrat Rahman and Krit Sae Fang
 * For UCLA CS218 Project - Fall 2016
 * Not all functions/methods are implemented. Stubs left for future work
 */
public interface IFDMCalculator {
	/**
	 * Add a flow to the FDM for calculation
	 * @param srcSwitchID - ID for the source node
	 * @param desSwitchID - ID for the destination node
	 * @param requestBW - Requested bandwidth for this flow
	 */
	public void addFlow(DatapathId srcNodeID, DatapathId desNodeID, double requestBW);
	
	/**
	 * Remove a flow from FDM calculation
	 * THIS IS OUTSIDE FALL 2016 SCOPE - ADDED AS TEMPLATE FOR FUTURE WORK
	 * @param srcSwitchID - ID for the source node
	 * @param desSwitchID - ID for the destination node
	 */
	public void removeFlow(DatapathId srcNodeID, DatapathId desNodeID);
	
	/**
	 * Return the bandwidth for the link between two nodes
	 * @param srcSwitchID - ID for the source node
	 * @param desSwitchID - ID for the destination node
	 * @return bandwidth
	 */
	public double getFlowBW(DatapathId srcNodeID, DatapathId desNodeID);

	/**
	 * Return the bandwidth for the link between two nodes
	 * @param srcSwitchID - ID for the source node
	 * @param desSwitchID - ID for the destination node
	 * @return bandwidth
	 */
	public float getFlowBW(Link link);

}
