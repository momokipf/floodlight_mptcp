package net.floodlightcontroller.fdmcalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.routing.Path;
import net.floodlightcontroller.routing.PathId;


class FlowDeviationMethod {
	protected static final Logger log = LoggerFactory.getLogger(FlowDeviationMethod.class);
	/*
	 * this.EPSILON = 0.00001f;
	 * this.DELTA = 0.002f;
	 */
	
	private static Float EPSILON =  0.00001f;
	private static Float DELTA = 0.002f;
	
//	private List<CustomizedLink> allFlows;
//	List<LinkedList<CustomizedLink>> pathset;
	
	private FDMTopology FDMtopoinstance;
	
	private Map<PathId,LinkedList<Integer>> shortestPath;
	
	private Float[] NewCap;
	private Float[] globalFlow;  //included in customizedlink 
	private Float[] EFlow; 		 //included in customizedlink 
	private Float[] FDlen;
	//private Float[] FDlen;
	
	private Float total_req ;
	
	public FlowDeviationMethod(FDMTopology FDMtopoinstance) {
		log.info("/*********************Start fdm algorithm*****************************/");
		this.FDMtopoinstance = FDMtopoinstance;
		NewCap = new Float[FDMtopoinstance.getNoLinks()];
		globalFlow = new Float[FDMtopoinstance.getNoLinks()];
		EFlow = new Float[FDMtopoinstance.getNoLinks()];
		shortestPath = new HashMap<PathId,LinkedList<Integer>>();
		total_req = FDMtopoinstance.getTotal_requirement();
		FDlen = new Float[FDMtopoinstance.getNoLinks()];
		for(int i= 0; i <FDMtopoinstance.getallLinks().size();++i ){
			log.info('('+Integer.toString(i)+')' + ' '+FDMtopoinstance.getallLinks().get(i).toString());
		}
	}


//	Float[][] shortestPathDistance;
//	Integer[][] shortestPathPredecessor;

//	Float[] FDlen;
	
	// Float[] Cost;

	Float Aresult;
	Boolean Aflag;

	Float CurrentDelay = 0.0f;
	Float PreviousDelay = 0.0f;
	
	
	
	
	
//	private Map<Link, Float> getGlobalFlows(FDMTopology network) {
//		Map<Link, Float> globalFlows = (Map<Link, Float>) new HashMap<Link, Float>();
//		for(int i = 0; i < network.getNoLinks(); i++) {
//			globalFlows.put(network.allLinks.get(i), globalFlow[i]);
//		}
//		return globalFlows;
//	}

	public Float[] runFDM() {
		
//		//Initialization Code
//		
//		EFlow = new Float[network.getNoLinks()]; // Current Extremal in Book
//		globalFlow = new Float[network.getNoLinks()]; //GFlow in Book
//
// //		PFlow = new Float[network.getNoNodes()];
//		
//		FDlen = new Float[network.getNoLinks()];
//		NewCap = new Float[network.getNoLinks()];
//  //		Cost = new Float[network.getNoLinks()];
		for(Integer i = 0; i < FDMtopoinstance.getNoLinks(); i++) {
			NewCap[i] = 0.0f;
			globalFlow[i] = 0.0f;
			EFlow[i] = 0.0f;
			FDlen[i] = 0.0f;
		}

		//log.info("/*********************Start fdm algorithm*****************************/");
		
		Float PreviousDelay = Float.POSITIVE_INFINITY;
//		Integer i, n;
		boolean prInteger = true;
//		Integer prInteger = 1;
		boolean feasible = true;
		
		SetLinkLens(globalFlow,FDMtopoinstance.getMsgLen(),FDlen);
		SetSP(FDlen);
		LoadLinks(globalFlow);
		Aresult = AdjustCaps(globalFlow,NewCap);
		if (Aresult == 1)
			Aflag = false;
		else
			Aflag = true;
		CurrentDelay = CalcDelay(globalFlow, NewCap,FDMtopoinstance.getMsgLen(), total_req);

		Integer count = 0;
		//start to run FDM
		while(Aflag || (CurrentDelay < PreviousDelay*(1-EPSILON))) {
			SetLinkLens(globalFlow,FDMtopoinstance.getMsgLen(),FDlen);
			SetSP(FDlen);
			LoadLinks(EFlow);
			//previous delay based on current NewCap
			PreviousDelay = CalcDelay(globalFlow, NewCap, FDMtopoinstance.getMsgLen(), total_req);
			Superpose(EFlow, globalFlow, NewCap, total_req, FDMtopoinstance.getMsgLen());
			//current delay after superposition
			CurrentDelay = CalcDelay(globalFlow, NewCap, FDMtopoinstance.getMsgLen(), total_req);
			StringBuffer ss = new StringBuffer();
			for(Float i:globalFlow){
				ss.append(Float.toString(i)+' ');
			}
			log.info("Intermediat result: "+ ss.toString());
			if(Aflag) {
				Aresult = AdjustCaps(globalFlow, NewCap);
				Aflag = (Aresult==1)?false:true;
			}
			
			if(Aflag && (CurrentDelay>=PreviousDelay*(1-EPSILON))){
				feasible = false;
				break;
			}
		}
		
		if(feasible){
			
			StringBuffer ss = new StringBuffer();
			
			for(int i = 0 ; i < this.FDMtopoinstance.getNoLinks();++i){
				this.FDMtopoinstance.getCustomizedLink(i).currentlinklength = globalFlow[i];
				ss.append(Float.toString(globalFlow[i]));
			}
			log.info("FDM calcuate done" + ss.toString());
			return globalFlow;
		}
		else{
//			PreviousDelay = network.getCurrentDelay();
//			network.setCurrentDelay(CalcDelay(network.getGflow(), NewCap, network.getMsgLen(), network.getTotal_requirement()));
			//judge whether the problem is feasible 
//			Float max_FD_len = 0f, min_FD_len = Float.POSITIVE_INFINITY;
//			for (Integer i = 0; i < network.getNoLinks(); i++) {
//				if (FDlen[i] > 0) {
//					max_FD_len = Math.max(max_FD_len, FDlen[i]);
//					min_FD_len = Math.min(min_FD_len, FDlen[i]);
//				}
//			}
//			if(Aflag == true && CurrentDelay >= PreviousDelay*(1-EPSILON)) {
//			//if ((Aflag == true && (max_FD_len - min_FD_len)<EPSILON)||count==100) {
//				System.out.print("The problem is infeasible. Now reduce the request.\n");
//				prInteger = false;
//				break;
//			}
//			
//			for(int i = 0; i < network.getNoLinks(); i ++) {
//				System.out.print("Gflow[" + i + "] in iteration is " + globalFlow[i] + "\n");
//			}
//			
//		 	System.out.print("current delay in iteration is " + CurrentDelay + "\n");
//			count++;
			log.info("infeasible");
		}
		return globalFlow;
	}

	
	private void SetLinkLens(Float[] Flow,Integer MsgLen, Float[] Len) {
		log.info("/*********************Start fdm SetLinkLens*****************************/");
		int i = 0;
		for(CustomizedLink clink:this.FDMtopoinstance.getallLinks()){
			//clink.currentlinklength = DerivDelay(Flow[i++],clink.getCapacity(), MsgLen);
			Len[i] = DerivDelay(Flow[i++],clink.getCapacity(), MsgLen);
		}
	}

	
	private void SetSP(Float[] len) {
		log.info("/*********************Start fdm SetSP*****************************/");
		for(PathId pid: this.FDMtopoinstance.getadj().keySet()){
			Float dis = Float.MAX_VALUE;
			for(LinkedList<Integer> l:this.FDMtopoinstance.getadj().get(pid)){
				//log.info(l.toString());
				Float tmp_dis = calculatAlllatency(l,len);
				//log.info("the path of "+ l.toString()+" latency : "+Float.toString(tmp_dis));
				if(tmp_dis<dis){
					dis = tmp_dis;
					shortestPath.put(pid,l);
				}
			}
		}
	}
	
	private Float calculatAlllatency(LinkedList<Integer> path,Float[] len){
		Float latency = 0.0f;
		log.info("/*********************Start fdm calculatAlllatency*****************************/");
		int index = 0 ;
		for(Integer p:path){
			if(index==0||index==path.size()-1)
			{
				index++;
				continue;
			}
			index++;
			CustomizedLink curlink = FDMtopoinstance.getCustomizedLink(p);
			//log.info('('+Integer.toString(p)+')' + ' '+curlink.toString());
			latency += /*curlink.currentlinklength*/len[p];  // + curlink.getLatency().getValue()/100; 
		}
		log.info(path.toString() + "latency" + Float.toString(latency));
		return latency;
	}
	
	/*
	 * Because flowDeviation should choose the shortest path from the given path. 
	 * This function is not valid any more;
	 * 
	void Bellman(Integer root, FDMTopology network, Float LinkLength[], LinkedList<Integer>[] Adj, Integer Pred[], Float Dist[]) {
		Integer[] Hop = new Integer[Adj.length];
		for (Integer i = 0; i < Adj.length; i++) {
			Dist[i] = Float.POSITIVE_INFINITY;
			Hop[i] = 0;
		}
		Dist[root] = 0.0f;
		Pred[root] = root;
		
		Stack<Integer> scanqueue = new Stack<Integer>();
		scanqueue.push(root);
		while (!scanqueue.empty()) {
			Integer node = scanqueue.peek();
			scanqueue.pop();
			for (Integer i = 0; i < Adj[node].size(); i++) {
				Integer link = Adj[node].get(i);
				if (link == -1)
					break;
				Integer node2 = network.getEnd2(link);
				Float d = Dist[node] + LinkLength[link];
				if (Dist[node2] > d) {
					Dist[node2] = d;
					//if (node2 < 0 || node2 >= NN)
					//	cout << "dd\n";
					Pred[node2] = link;
					Hop[node2] = Hop[node] + 1;
					if(Hop[node2]<3)
						scanqueue.push(node2);
				}
			}

		}

	}
	
	*/
	private void LoadLinks(Float Flow[]) {
		log.info("/*********************Start fdm LoadLinks*****************************/");
		for(Integer i = 0; i < Flow.length; i ++) {
			Flow[i] = 0.0f;
		}
		for(PathId pid: this.shortestPath.keySet()){
			float req = FDMtopoinstance.getCustomizedLink(shortestPath.get(pid).get(0)).getrequirement();
			log.info("retrieve req from the head: "+Float.toString(req));
			for(Integer index:shortestPath.get(pid)){
				Flow[index]+=req;
			}
		}	
	}

	
	private Float AdjustCaps(Float Flow[],Float NewCap[]) {
		Float factor = 1.0f;
		for( Integer i = 0; i < Flow.length; i++) {
			Float cap = this.FDMtopoinstance.getCustomizedLink(i).getCapacity();
			factor = Math.max(factor, (1+DELTA)*Flow[i]/cap);
		}
		for(Integer i = 0; i < Flow.length; i++) {
			NewCap[i] = factor*FDMtopoinstance.getCustomizedLink(i).getCapacity();
		}
		//log.info("AdjustCaps: result : ");
		log.info(" factor" + Float.toString(factor));
		return factor;
	}

	private Float CalcDelay(Float Flow[], Float Cap[], Integer MsgLen, Float TotReq) {
		Float sum = 0.0f;
		for (Integer u = 0; u < Flow.length; u++) {
			sum = sum + Flow[u]*LinkDelay(Flow[u],Cap[u],MsgLen);
		}
		return sum/TotReq;
	}

void Superpose(Float Eflow[], Float Gflow[], Float Cap[], Float TotReq, Integer MsgLen) {
	Float x = FindX(Gflow, Eflow, Cap, TotReq, MsgLen);
	for(Integer l = 0; l < Gflow.length; l++) {
//		Pflow[l] = Gflow[l];
		Gflow[l] = x*Eflow[l] + (1-x)*Gflow[l];
	
	}
	
}

Float FindX(Float Gflow[], Float Eflow[], Float Cap[], Float TotReq, Integer MsgLen) {

	Float xLimit = 0.0f, st = 0.0f, end = 1.0f;
	Float[] Flow = new Float[Gflow.length];
	for (; (end-st)>0.0001;) {
		Boolean exc = false;
		xLimit = st + (end - st) / 2;
		for (int i = 0; i < Flow.length; i++) {
			Flow[i] = xLimit*Eflow[i] + (1 - xLimit)*Gflow[i];
			if (Flow[i] > Cap[i]) {
				exc = true;
				break;
			}
		}
		if (exc) {
			end = xLimit;
		}
		else
			st = xLimit;
	}
	xLimit = st;	
	
	Float x0 = 0.0f; Float f0 = DelayF(x0, Eflow, Gflow, Cap, MsgLen, TotReq);
	//Float x4 = 1.0; Float f4 = DelayF(x4, nl, Eflow, Gflow, Cap, MsgLen, TotReq, Cost);
	Float x4 = xLimit; Float f4 = DelayF(x4, Eflow, Gflow, Cap, MsgLen, TotReq);
	//Float x2 = 0.5; Float f2 = DelayF(x2, nl, Eflow, Gflow, Cap, MsgLen, TotReq, Cost);
	Float x2 = (x0+x4)/2; Float f2 = DelayF(x2, Eflow, Gflow, Cap, MsgLen, TotReq);

	while(x4-x0 > EPSILON) {
		Float x1 = (x0 + x2)/2; Float f1 = DelayF(x1, Eflow, Gflow, Cap, MsgLen, TotReq);
		Float x3 = (x2 + x4)/2; Float f3 = DelayF(x3, Eflow, Gflow, Cap, MsgLen, TotReq);	
		if( (f0 <= f1) || (f1 <= f2) ) {
			x4 = x2; x2 = x1;
			f4 = f2; f2 = f1;
		}
		else if (f2 <= f3) {
			x0 = x1; x4 = x3;
			f0 = f1; f4 = f3;
		}
		else {
			x0 = x2; x2 = x3;
			f0 = f2; f2 = f3;
		}
	}
	if ((f0 <= f2) && (f0 <= f4)) {
		return(x0);
	}
	else if (f2 <= f4) {
		return(x2);
	}
	else {
		return (x4);
	}
}

Float DelayF(Float x, Float Eflow[], Float Gflow[], Float Cap[], Integer MsgLen, Float TotReq) {
	Float[] Flow = new Float[Gflow.length];
		for(Integer l = 0; l < Gflow.length; l++) {
			Flow[l] = x*Eflow[l] + (1-x)*Gflow[l];
		}
		return( CalcDelay(Flow, Cap, MsgLen, TotReq));
}

	Float LinkDelay(Float Flow, Float Cap, Integer MsgLen) {
		return ((MsgLen / Cap) / (1 - Flow / Cap));
	}

	Float DerivDelay(Float Flow, Float Cap, Integer MsgLen) {
		Float f = 1 - Flow / Cap;
		return ((MsgLen / Cap) / (f * f));
	}

	Float Deriv2Delay(Float Flow, Float Cap, Integer MsgLen) {
		Float f = 1 - Flow / Cap;
		return (2 * (MsgLen / Cap) / (Cap * f * f * f));
	}

}