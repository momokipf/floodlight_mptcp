package net.floodlightcontroller.fdmcalculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery.LDUpdate;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.topology.ITopologyListener;
import net.floodlightcontroller.topology.ITopologyService;

public class FDMCalculator implements IFDMCalculatorService, ITopologyListener, IFloodlightModule {

	protected static final Logger log = LoggerFactory.getLogger(FDMCalculator.class);

	// Protected variables we'll be using
	protected ITopologyService topologyService;		// Topology service that we'll be calling

	private Map<Link, Float> globalLinkFlows;
	
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
	    Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
	    l.add(IFDMCalculatorService.class);
	    return l;
	}
	 
	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
	    Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
	    m.put(IFDMCalculatorService.class, this);
	    return m;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// Set dependencies
		// We require TopologyService to be up and running first
		Collection<Class<? extends IFloodlightService>> l = 
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(ITopologyService.class);
		log.info("getModuleDependencies");

		return l;
	}

	@Override
	public void init(FloodlightModuleContext context) 
			throws FloodlightModuleException {
		// Initialize our dependencies
		topologyService = context.getServiceImpl(ITopologyService.class);
		topologyService.addListener(FDMCalculator.this);
		log.info("init");

	}

	@Override
	public void startUp(FloodlightModuleContext context) 
			throws FloodlightModuleException {
		// TODO Auto-generated method stub
		log.info("startup");
	}

	@Override
	public void topologyChanged(List<LDUpdate> linkUpdates) {
		// Update our topology
		log.info("topologyChanged");
		buildTopology();
	}

//	@Override
//	public void addFlow(DatapathId srcNodeID, DatapathId desNodeID,
//			double requestBW) {
//		// TODO Auto-generated method stub
//		// req.add
//		calculateFDM();
//	}
//
//	@Override
//	public void removeFlow(DatapathId srcNodeID, DatapathId desNodeID) {
//		// TODO Auto-generated method stub
//		// req.remove
//		calculateFDM();
//	}
//
//	@Override
//	public double getFlowBW(DatapathId srcNodeID, DatapathId desNodeID) {
//		// TODO Auto-generated method stub
//		// Go through End1 and End2, find match, find match in Gflow
//		return 0.0;
//	}
	
	@Override
	public Float getFlowBW(Link link) {
		// TODO Auto-generated method stub
		return globalLinkFlows.get(link);
	}
	
	public Float getFlowBW(IOFSwitch currentSwitch, OFPort currentPort,IOFSwitch nextSwitch, OFPort nextPort) {
		U64 latency = U64.of(0L);
		
		// Build a link to send in
		Link link = new Link(currentSwitch.getId(), currentPort, nextSwitch.getId(), nextPort, latency);
		 log.info("********************This is the rate info:{}***********",String.valueOf(globalLinkFlows.get(link)));
		 //log.info( String.valueOf(globalLinkFlows.get(link)));
		return globalLinkFlows.get(link);
	}

	
	/**
	 * Main function for doing FDM
	 * This only calculates and fill gflow
	 */
	private void calculateFDM() {
		// TODO FDM code here
	}
	
	/**
	 * Build the topology in our implementation
	 * This is needed as TopologyService keeps its topology in a very different way
	 */
	private void buildTopology() {
		// Variables we need
		Map<DatapathId, Set<Link>> linkMap;
		
		linkMap = topologyService.getAllLinks();
		log.info("No. of Nodes: " + linkMap.size());
		log.info("Complete Topology: " + linkMap.toString());

		FDMTopology top = new FDMTopology(1, linkMap);
		log.info("All Links: " + top.allLinks);
		log.info("All Nodes: " + top.nodes);
		log.info("No. of Links: " + top.getNoLinks());
		Float[] a_cap = {
				5.0f, 6.0f, 7.0f, 
				Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, 
				Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, 
				Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, 
				Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, 
				Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, 
				Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE,
				Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
		// if(a_cap.length != top.getNoLinks()) {
		// 	log.info("Capacity Incorrectly Initialized");
		// 	System.exit(0);
		// }
		top.initCapacity(a_cap);
		Float[][] a_req = { 
				{0f, 0f, 0f, 0f, 0f, 0f, 0f },
				{0f, 0f, 0f, 0f, 0f, 0f, 0f },
				{0f, 0f, 0f, 0f, 0f, 0f, 0f },
				{0f, 0f, 0f, 0f, 0f, 0f, 0f },
				{5.0f, 0f, 0f, 0f, 0f, 0f, 0f },
				{6.0f, 0f, 0f, 0f, 0f, 0f, 0f },
				{1.0f, 0f, 0f, 0f, 0f, 0f, 0f } };
		log.info("All Req: " + Arrays.deepToString(a_req));
		top.initRequirements(a_req);
		
		float delta = 0.002f;
		float epsilon = 0.00001f;
		FlowDeviationMethod fdm = new FlowDeviationMethod(delta, epsilon);
		globalLinkFlows = fdm.runFDM(top);
		
		log.info("Global Flows: " + globalLinkFlows);
		
	}

}