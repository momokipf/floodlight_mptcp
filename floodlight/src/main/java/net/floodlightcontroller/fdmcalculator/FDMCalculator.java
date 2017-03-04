package net.floodlightcontroller.fdmcalculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
import net.floodlightcontroller.core.util.SingletonTask;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery.LDUpdate;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.routing.Path;
import net.floodlightcontroller.routing.PathId;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import net.floodlightcontroller.topology.ITopologyListener;
import net.floodlightcontroller.topology.ITopologyService;
import net.floodlightcontroller.fdmcalculator.Web.FdmWebRoutable;
//import net.floodlightcontroller.topology.TopologyInstance;

public class FDMCalculator implements IFDMCalculatorService, ITopologyListener, IFloodlightModule {

	protected int FDMCALCULATE_INTERVAL = 500;
	
	protected static final Logger log = LoggerFactory.getLogger(FDMCalculator.class);

	// Protected variables we'll be using
	protected ITopologyService topologyService;		// Topology service that we'll be calling

	protected IRestApiService restApiService;
	
	protected static IThreadPoolService threadPoolService;
	
	
	//private Map<PathId,Set<Path>> currentuser = new HashMap<PathId,Set<Path>>();
	private Map<String,Set<Path>> activeuser;
	
	protected FDMTopology currentInstance;
	
	private Map<Link, Float> globalLinkFlows;// need to change
	
	protected Map<String,List<Float>> rule;
	
	protected SingletonTask newInstanceTask;
	
	//Modules that get the path updates
	protected BlockingQueue<PathUpdate> pathUpdates;
	
	protected class UpdateFDMTopologyWorker implements Runnable{
		@Override
		public void run(){
			try{
				/*
				 * test version 1.0 calculate result every 0.5s;
				 */
				if(pathUpdates.peek()!=null){
					calculateFDM();
					log.info("calculate fdm" + pathUpdates.toString());
					pathUpdates.clear();
				}
				
			}
			catch(Exception e){
				log.error("Error in topology instance task thread");
			}finally{
				newInstanceTask.reschedule(FDMCALCULATE_INTERVAL, TimeUnit.MILLISECONDS);
			}
		}
	}
	
	public class PathUpdate{
		public static final String ADD = "add";
		public static final String DELETE = "delect";
		protected String pathstr;
		protected Path p;
		protected String op;
		
		public PathUpdate(String pathstr,Path p, String op){
			this.pathstr = pathstr;
			this.p = p;
			this.op = op;
		}
	};
	

	private boolean fdmactive = true;
	
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
		threadPoolService = context.getServiceImpl(IThreadPoolService.class);
		//tm = (ITopologyManagerBackend)context.getServiceImpl(ITopologyService.class);
		//buildTopology();
		this.restApiService = context.getServiceImpl(IRestApiService.class);
		log.debug("FDM module init");
		
		activeuser = new HashMap<String,Set<Path>>();
		rule = new HashMap<String,List<Float>>();
		pathUpdates = new LinkedBlockingQueue<PathUpdate>();

	}

	@Override
	public void startUp(FloodlightModuleContext context) 
			throws FloodlightModuleException {
		// TODO Auto-generated method stub
		ScheduledExecutorService ses = threadPoolService.getScheduledExecutor();
		newInstanceTask = new SingletonTask(ses,new UpdateFDMTopologyWorker());
		newInstanceTask.reschedule(FDMCALCULATE_INTERVAL, TimeUnit.MILLISECONDS);
		this.restApiService.addRestletRoutable(new FdmWebRoutable());
		buildTopology();
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
	public void addPath(String pathstr,Path p){
		if(currentInstance==null)
			return;
		if(activeuser.containsKey(pathstr)){
			if(!activeuser.get(pathstr).contains(p)){
				activeuser.get(pathstr).add(p);
			}
			else 
				return;
		}
		else{
			HashSet<Path> newset = new HashSet<Path>();
			newset.add(p);
			activeuser.put(pathstr, newset);
		}
		updatePath(pathstr,p,PathUpdate.ADD);
		currentInstance.addPathtoTopology(p);
	}
	
	@Override 
	public void delectPath(String pathstr,Path p){
		
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
	
	public void updatePath(String pathstr, Path p, String op){
		PathUpdate up = new PathUpdate(pathstr,p,op);
		try {
			this.pathUpdates.put(up);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};

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
	
	private void updateUser(){
		//List<PathUpdate> appliedUpdates = new ArrayList<PathUpdate>();
		PathUpdate update = null;
		while(this.pathUpdates.peek()!=null){
			try{
				update = pathUpdates.take();
			}catch(Exception e){
				log.error("Error reading path update");
			}
			switch(update.op){
				case PathUpdate.ADD:
					if(this.activeuser.containsKey(update.pathstr)){
						this.activeuser.get(update.pathstr).add(update.p);
					}
					else{
						Set<Path> newset = new HashSet<Path>();
						newset.add(update.p);
						this.activeuser.put(update.pathstr, newset);
					}
					log.info("Path added : "+update.pathstr);
					break;
				case PathUpdate.DELETE:
					if(this.activeuser.containsKey(update.pathstr)){
						this.activeuser.get(update.pathstr).remove(update.p);
					}
					log.info("Path delete : "+update.pathstr);
					break;
				default:
						break;
			}
		}
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
			log.info(this.topologyService.getBlockedPorts().toString());
			currentInstance = new FDMTopology(1,this.topologyService.getAllLinks(),rule,topologyService.getAllEdge());
			for(String str:activeuser.keySet()){
				currentInstance.addPathstoTopology(activeuser.get(str));
			}
			
	}

	
	private Map<DatapathId, Set<CustomizedLink>> buildstatictopo() {
		Map<DatapathId,Set<CustomizedLink>> cusdpidLinks = new HashMap<DatapathId, Set<CustomizedLink>>();
		Map<DatapathId,Set<Link>> dpidLinks = this.topologyService.getAllLinks();
		for(DatapathId pathid:dpidLinks.keySet()){
			for(Link l:dpidLinks.get(pathid)){
				if(dpidLinks.containsKey(pathid)) {
                    //dpidLinks.get(pathid).add(new CustomizedLink(l,Float.MAX_VALUE,2.0f));
                }
                else {
                    //dpidLinks.put(pathid,new HashSet<Link>(Arrays.asList(new CustomizedLink(l,Float.MAX_VALUE,2.0f))));
                }
			}
		}
		
		return cusdpidLinks;
	}
	
	
	
	
	
	
	
}