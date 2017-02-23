package net.floodlightcontroller.fdmcalculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.routing.Path;
import net.floodlightcontroller.topology.ITopologyManagerBackend;
import net.floodlightcontroller.topology.ITopologyListener;
import net.floodlightcontroller.topology.ITopologyService;
import net.floodlightcontroller.fdmcalculator.Web.FdmWebRoutable;
//import net.floodlightcontroller.topology.TopologyInstance;

public class FDMCalculator implements IFDMCalculatorService, ITopologyListener, IFloodlightModule {

	protected static final Logger log = LoggerFactory.getLogger(FDMCalculator.class);

	// Protected variables we'll be using
	protected ITopologyService topologyService;		// Topology service that we'll be calling

	protected IRestApiService restApiService;
	
	protected static ITopologyManagerBackend tm;
	
	
	private Map<String,Set<Path>> currentuser;
	
	protected FDMTopology currentInstance;
	
	private Map<Link, Float> globalLinkFlows;
	
	protected Map<String,List<Float>> rule = new HashMap<String,List<Float>>();
	
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
		l.add(IRestApiService.class);
		//log.info("getModuleDependencies");
		
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context) 
			throws FloodlightModuleException {
		// Initialize our dependencies
		topologyService = context.getServiceImpl(ITopologyService.class);
		topologyService.addListener(FDMCalculator.this);
		//tm = (ITopologyManagerBackend)context.getServiceImpl(ITopologyService.class);
		//buildTopology();
		this.restApiService = context.getServiceImpl(IRestApiService.class);
		log.info("init");

	}

	@Override
	public void startUp(FloodlightModuleContext context) 
			throws FloodlightModuleException {
		// TODO Auto-generated method stub
		this.restApiService.addRestletRoutable(new FdmWebRoutable());
		log.info("rebuild topology");
	}

	@Override
	public void topologyChanged(List<LDUpdate> linkUpdates) {
		// Update our topology
		buildTopology();
		log.info("topologyChanged");
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
	
	@Override
	public void addPath(Path p){
		
		ArrayList<Path> tmp = new ArrayList<Path>();
		tmp.add(p);
		if(currentInstance!=null)
			this.currentInstance.addPathtoTopology(tmp);
	}
	
	@Override
	public Map<String,List<Float>> getRules(){
		return rule;
	}
	
	@Override
	public void addRule(String nodetuple,Float req,Float cap){
		if(this.rule!=null){
			List<Float> list = new ArrayList<Float>();
			list.add(req);list.add(cap);
			rule.put(nodetuple,list);
			if(currentInstance!=null)
				currentInstance.updateCusLink(nodetuple,req,cap);
		}
	}
	
	@Override
	public void clearRule(){
		if(rule!=null){
			rule.clear();
		}
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
		FlowDeviationMethod fdm = new FlowDeviationMethod(this.currentInstance);
		fdm.runFDM();
	}
	
	/**
	 * Build the topology in our implementation
	 * This is needed as TopologyService keeps its topology in a very different way
	 * (1) static way
	 * (2) dynamic way
	 * 
	 */
	private void buildTopology() {
		// Variables we need
		//Map<DatapathId, Set<Link>> linkMap = tm.getCurrentTopologyInstance(
		//Set<DatapathId> switches = tm.getCurrentTopologyInstance().getSwitches();
			currentInstance = new FDMTopology(1,this.topologyService.getAllLinks(),rule);
	}

	
	private Map<DatapathId, Set<CustomizedLink>> buildstatictopo() {
		Map<DatapathId,Set<CustomizedLink>> cusdpidLinks = new HashMap<DatapathId, Set<CustomizedLink>>();
		Map<DatapathId,Set<Link>> dpidLinks = this.topologyService.getAllLinks();
		for(DatapathId pathid:dpidLinks.keySet()){
			for(Link l:dpidLinks.get(pathid)){
				if(dpidLinks.containsKey(pathid)) {
                    dpidLinks.get(pathid).add(new CustomizedLink(l,Float.MAX_VALUE,2.0f));
                }
                else {
                    dpidLinks.put(pathid,new HashSet<Link>(Arrays.asList(new CustomizedLink(l,Float.MAX_VALUE,2.0f))));
                }
			}
		}
		
		return cusdpidLinks;
	}
	
	
	
	
	
	
	
}