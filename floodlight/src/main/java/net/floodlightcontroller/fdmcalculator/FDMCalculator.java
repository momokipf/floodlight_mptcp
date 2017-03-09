package net.floodlightcontroller.fdmcalculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.core.util.SingletonTask;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery.LDUpdate;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.routing.Path;
import net.floodlightcontroller.dropmeter.DropMeter;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import net.floodlightcontroller.topology.ITopologyListener;
import net.floodlightcontroller.topology.ITopologyService;
import net.floodlightcontroller.fdmcalculator.Web.FdmWebRoutable;
//import net.floodlightcontroller.topology.TopologyInstance;

public class FDMCalculator implements IFDMCalculatorService, ITopologyListener, IFloodlightModule {

	protected int FDMCALCULATE_INTERVAL = 1000;
	
	protected static final Logger log = LoggerFactory.getLogger(FDMCalculator.class);

	// Protected variables we'll be using
	protected ITopologyService topologyService;		// Topology service that we'll be calling

	protected IRestApiService restApiService;
	
	protected IOFSwitchService switchService;
	protected static IThreadPoolService threadPoolService;
	
	
	//private Map<PathId,Set<Path>> currentuser = new HashMap<PathId,Set<Path>>();
	private Map<String,Set<Flowinfo>> activeuser;
	//private Map<Path, Flowinfo> pathinfo;
	
	protected FDMTopology currentInstance;
	
	private Map<Link, Float> globalLinkFlows;// need to change
	
	protected Map<String,List<Float>> rule;
	
	protected SingletonTask newInstanceTask;
	
	//Modules that get the path updates
	protected BlockingQueue<PathUpdate> pathUpdates;
	
	
	
	
	/*
	 * Thread dedicated to handle path update. Calculate FDM parameter only when there
	 *  are Path Update
	 * 
	 */
	protected class UpdateFDMTopologyWorker implements Runnable{
		@Override
		public void run(){
			try{
				/*
				 * test version 1.0 calculate result every 0.5s;
				 */
				if(pathUpdates.peek()!=null){
					calculateFDM();
					populatemeter();
					//pathUpdates.clear();
				}
				
			}
			catch(Exception e){
				e.printStackTrace();
				log.error(e.getMessage());
				
			}finally{
				pathUpdates.clear();
				newInstanceTask.reschedule(FDMCALCULATE_INTERVAL, TimeUnit.MILLISECONDS);
			}
		}
	}
	
	/*
	 * Class PathUpdate: specification of the path update.
	 * Operation: ADD & DELETE
	 */
	
	private class PathUpdate{
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
	
	private class Flowinfo{
		private IPv4Address src;
		private IPv4Address dst;
		private TransportPort srcport;
		private TransportPort dstport;
		private Path path;
		
		public Flowinfo(IPv4Address src, IPv4Address dst,TransportPort srcport, TransportPort dstport,Path path){
			this.src = src;
			this.dst = dst;
			this.srcport = srcport;
			this.dstport = dstport;
			this.path = path;
		}
		
		public IPv4Address getsrc(){
			return this.src;
		}
		
		public IPv4Address getdst(){
			return this.dst;
		}
		public Path getpath(){
			return this.path;
		}
		
		public TransportPort gettcpsrcport(){
			return this.srcport;
		}
		public TransportPort gettcpdstport(){
			return dstport;
		}
		public String toString(){
			return "Flowinfo[ src:" + src +
					" dst: " + dst +
					" srcport" + srcport.toString() +
					" dstport" + dstport.toString() +
					" ]";
		}
		@Override
	    public boolean equals(Object obj) {
	        if (this == obj)
	            return true;
	        if (obj == null)
	            return false;
	        if (getClass() != obj.getClass())
	            return false;
	        Flowinfo other = (Flowinfo) obj;
	        if (!this.src.equals(other.src))
	            return false;
	        if (!this.dst.equals(other.dst))
	            return false;
	        if (!this.srcport.equals(other.srcport))
	            return false;
	        if (!this.dstport.equals(other.dstport))
	            return false;
	        return true; /* do not include latency and other field */
	    }
	}
	
	
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
		switchService = context.getServiceImpl(IOFSwitchService.class);
		//tm = (ITopologyManagerBackend)context.getServiceImpl(ITopologyService.class);
		//buildTopology();
		this.restApiService = context.getServiceImpl(IRestApiService.class);
		log.debug("FDM module init");
		
		activeuser = new HashMap<String,Set<Flowinfo>>();
		//pathinfo = new HashMap<Path,Flowinfo>();
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
	public void addPath(IPv4Address src, IPv4Address dst,TransportPort srcport, TransportPort dstport,  Path p){
		if(currentInstance==null)
			return;
		String pathstr = src+"-"+dst;
		Flowinfo fi = new Flowinfo(src,dst,srcport,dstport,p);
		if(activeuser.containsKey(pathstr)){
			if(!activeuser.get(pathstr).contains(fi)){
				activeuser.get(pathstr).add(fi);
			}
			else 
				return;
		}
		else{
			HashSet<Flowinfo> newset = new HashSet<Flowinfo>();
			newset.add(fi);
			activeuser.put(pathstr, newset);
		}
		log.info("addPath we need to check it out: " + p.toString());
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
	
	@Override
	public void deleterule_path(String rule_Key){
		if(rule!=null){
			List<Float> list = rule.remove(rule_Key);
			/*
			 * Because tcp is best effort protocol and switch would feed back then end of one flow,
			 * we had to figure out a solution that let controller knows when it cancel the allocation 
			 * of one flow. Here I just assume when virtual link's req set back to zero, the flow stop.
			 * 
			 * My idea: periodically drop flow on the first switch, then switch would query routing information 
			 * again,then update the allocation.
			 */
			
			for(Map.Entry<String,Set<Flowinfo>> entry:activeuser.entrySet()){
				Iterator<Flowinfo> it = entry.getValue().iterator();
				while(it.hasNext()){
					Flowinfo fi = it.next();
					List<NodePortTuple> nslist = fi.path.getPath();
					int index = nslist.size();
					String porttuple = nslist.get(index-1).getNodeId().toString()+'-'+nslist.get(index-1).getPortId().toString()+
							'-'+nslist.get(index-1).getNodeId().toString()+'-'+nslist.get(index-1).getPortId().toString();
					log.info("check tuple: " + porttuple + "with "+ rule_Key);
					if(rule_Key.equals(porttuple)){
						it.remove();
						log.info("delete one active user");
					}
				}
			}
			buildTopology();
		}
	}
	
	public Float getFlowBW(DatapathId currentSwitch, OFPort currentPort,DatapathId nextSwitch, OFPort nextPort) {
		String linkstr = currentSwitch.toString()+'-'+currentPort.toString()+'-'+nextSwitch.toString()+'-'+nextPort.toString();
		CustomizedLink cuslink = currentInstance.getCustomizedLink(linkstr);
		if(cuslink==null)
		{
			log.error("no cuslink mateched " + linkstr);
			return Float.POSITIVE_INFINITY;
		}
		else {
			log.info("cuslink mateched" + linkstr);
			return cuslink.currentlinklength;
		}
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
			Set<Path> add_path = new HashSet<Path>();
			for(Map.Entry<String, Set<Flowinfo>> entry : activeuser.entrySet()){
				for(Flowinfo fi:entry.getValue()){
					add_path.add(fi.path);
				}
			}
			currentInstance.addPathstoTopology(add_path);
	}

	private void populatemeter(){
		
		DropMeter dm = new DropMeter();
		for(Map.Entry<String, Set<Flowinfo>> entry:activeuser.entrySet()){
			for(Flowinfo fi:entry.getValue()){
				Path p = fi.path;
				List<NodePortTuple> nslist = new ArrayList<NodePortTuple>(p.getPath());
				Collections.reverse(nslist);
				log.info("in populatemeter : "+ fi.toString());
				IOFSwitch  currentSwitch = switchService.getSwitch(nslist.get(1).getNodeId());
	            OFPort currentPort = nslist.get(1).getPortId();         
	            IOFSwitch  nextSwitch = switchService.getSwitch((nslist.get(2).getNodeId()));
	            OFPort nextPort = nslist.get(2).getPortId();
	            Float rate = getFlowBW(nslist.get(1).getNodeId(),currentPort,nslist.get(2).getNodeId(),nextPort);
				dm.createMeter(currentSwitch, currentPort,nextSwitch,nextPort,rate);
				log.info("bind mater "+ Float.toString(rate) + "on " + fi.toString());
				dm.bindMeterWithFlow(nslist.get(0).getPortId(),fi.getdst(),fi.gettcpdstport(), fi.getsrc(), currentSwitch, fi.gettcpsrcport(), new Path(p.getId(),nslist));
			}
		}
	}

	
	
	
	
	
	
	
}