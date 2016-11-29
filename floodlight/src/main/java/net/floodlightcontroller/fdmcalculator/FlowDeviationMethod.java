package net.floodlightcontroller.fdmcalculator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import net.floodlightcontroller.linkdiscovery.Link;

class FlowDeviationMethod {
	
	public FlowDeviationMethod(float delta, float epsilon) {
		EPSILON = epsilon;
		DELTA = delta;
	}
	
	Float EPSILON;
	Float DELTA;

	Float[] globalFlow;
	Float[] EFlow;
	// Float[] Pflow;

	Float[][] shortestPathDistance;
	Integer[][] shortestPathPredecessor;

	Float[] FDlen;
	Float[] NewCap;
	// Float[] Cost;

	Float Aresult;
	Boolean Aflag;

	Float CurrentDelay = 0.0f;
	Float PreviousDelay = 0.0f;
	
	private Map<Link, Float> getGlobalFlows(FDMTopology network) {
		Map<Link, Float> globalFlows = (Map<Link, Float>) new HashMap<Link, Float>();
		for(int i = 0; i < network.getNoLinks(); i++) {
			globalFlows.put(network.allLinks.get(i), globalFlow[i]);
		}
		return globalFlows;
	}

	Map<Link, Float> runFDM(FDMTopology network) {
		
		//Initialization Code
		
		EFlow = new Float[network.getNoLinks()]; // Current Extremal in Book
		globalFlow = new Float[network.getNoLinks()]; //GFlow in Book

//		PFlow = new Float[network.getNoNodes()];
		
		shortestPathDistance = new Float[network.getNoNodes()][network.getNoNodes()];
		shortestPathPredecessor = new Integer[network.getNoNodes()][network.getNoNodes()];
		
		FDlen = new Float[network.getNoLinks()];
		NewCap = new Float[network.getNoLinks()];
//		Cost = new Float[network.getNoLinks()];
		for(Integer i = 0; i < network.getNoLinks(); i++) {
			FDlen[i] = Float.POSITIVE_INFINITY;
			NewCap[i] = 0.0f;
			globalFlow[i] = 0.0f;
			EFlow[i] = 0.0f;
//			Cost[i] = 0;
		}
		
		Float PreviousDelay = Float.POSITIVE_INFINITY;
//		Integer i, n;
		Boolean prInteger = true;
//		Integer prInteger = 1;

		
		
		SetLinkLens(globalFlow, network.getCapacity(), network.getMsgLen(), FDlen);
		SetSP(network, FDlen, network.getAdj(), shortestPathDistance, shortestPathPredecessor);
		LoadLinks(network.getReq(), shortestPathPredecessor, network, globalFlow);
		Aresult = AdjustCaps(globalFlow, network.getCapacity(), NewCap);
		if (Aresult == 1)
			Aflag = false;
		else
			Aflag = true;
		CurrentDelay = CalcDelay(globalFlow, NewCap, network.getMsgLen(), network.getTotal_requirement());

		Integer count = 0;
		//start to run FDM
		while(Aflag || (CurrentDelay < PreviousDelay*(1-EPSILON))) {
			SetLinkLens(globalFlow, network.getCapacity(), network.getMsgLen(), FDlen);
			SetSP(network, FDlen, network.getAdj(), shortestPathDistance, shortestPathPredecessor);
			LoadLinks(network.getReq(), shortestPathPredecessor, network, EFlow);
			//previous delay based on current NewCap
			PreviousDelay = CalcDelay(globalFlow, NewCap, network.getMsgLen(), network.getTotal_requirement());
			Superpose(EFlow, globalFlow, NewCap, network.getTotal_requirement(), network.getMsgLen());
			//current delay after superposition
			CurrentDelay = CalcDelay(globalFlow, NewCap, network.getMsgLen(), network.getTotal_requirement());
			
			if(Aflag) {
				Aresult = AdjustCaps(globalFlow, network.getCapacity(), NewCap);
				if (Aresult == 1)
					Aflag = false;
				else
					Aflag = true;
			}
//			PreviousDelay = network.getCurrentDelay();
//			network.setCurrentDelay(CalcDelay(network.getGflow(), NewCap, network.getMsgLen(), network.getTotal_requirement()));
			//judge whether the problem is feasible 
			Float max_FD_len = 0f, min_FD_len = Float.POSITIVE_INFINITY;
			for (Integer i = 0; i < network.getNoLinks(); i++) {
				if (FDlen[i] > 0) {
					max_FD_len = Math.max(max_FD_len, FDlen[i]);
					min_FD_len = Math.min(min_FD_len, FDlen[i]);
				}
			}
			if(Aflag == true && CurrentDelay >= PreviousDelay*(1-EPSILON)) {
			//if ((Aflag == true && (max_FD_len - min_FD_len)<EPSILON)||count==100) {
				System.out.print("The problem is infeasible. Now reduce the request.\n");
				prInteger = false;
				break;
			}
			
			for(int i = 0; i < network.getNoLinks(); i ++) {
				System.out.print("Gflow[" + i + "] in iteration is " + globalFlow[i] + "\n");
			}
			
		 	System.out.print("current delay in iteration is " + CurrentDelay + "\n");
			count++;
		}
		if(prInteger) {
			System.out.print("\n");
		 	for(int i = 0; i < network.getNoLinks(); i ++) {
				System.out.print("Gflow[" + i + "] is " + globalFlow[i] + "\n");
				System.out.print("fd_length[" + i + "] is " + FDlen[i] + "\n");
			}

		 	System.out.println("current delay is " + CurrentDelay);
			System.out.println("current count is " + count);
		}
//		else {
//
//			//initialize request for infeasible problem
//			for(i = 0; i < NN; i++) {
//				for(n = 0; n < NN; n++) {
//					if (Req[i][n] != 0) {
//						MM_Req[i][n] = STEP;
//					}
//				}
//			}
//			prInteger = true;
//			while(prInteger) {
//				TotReq = 0;
//				PreviousDelay = INFINITY;
//
//				for(i = 0; i < NN; i++) {
//					for(n = 0; n < NN; n ++) {
//						TotReq += MM_Req[i][n];
//					}
//				}
//				for(i = 0; i < network.getNoLinks(); i ++) {
//					Gflow[i] = 0;
//				}
//				SetLinkLens(NL, Gflow, Cap, MsgLen, FDlen, Cost);
//				SetSP(NN, End2, FDlen, Adj, shortestPathDistance, SPpred);
//				LoadLinks(NN, NL, MM_Req, SPpred, End1, Gflow);
//				Aresult = AdjustCaps(NL, Gflow, Cap, NewCap);
//				if (Aresult == 1)
//					Aflag = 0;
//				else
//					Aflag = 1;
//				CurrentDelay = CalcDelay(NL, Gflow, NewCap, MsgLen, TotReq, Cost);
//				count = 0;
//				while(Aflag || (CurrentDelay < PreviousDelay*(1-EPSILON))) {
//					SetLinkLens(NL, Gflow, NewCap, MsgLen, FDlen, Cost);
//					SetSP(NN, End2, FDlen, Adj, SPdist, SPpred);
//					LoadLinks(NN, NL, MM_Req, SPpred, End1, Eflow);
//					Superpose(NL, Eflow, Gflow, NewCap, TotReq, MsgLen, Cost);
//
//					if (Aflag) {
//						Aresult = AdjustCaps(NL, Gflow, Cap, NewCap);
//						if (Aresult == 1)
//							Aflag = 0;
//						else
//							Aflag = 1;
//					}
//					PreviousDelay = CurrentDelay;
//					CurrentDelay = CalcDelay(NL, Gflow, NewCap, MsgLen, TotReq, Cost);
//					//judge whether the problem is feasible 
//					Float max_FD_len = 0, min_FD_len = INFINITY;
//					for (Integer i = 0; i < network.getNoLinks(); i++) {
//						if (FDlen[i] > 0) {
//							max_FD_len = max(max_FD_len, FDlen[i]);
//							min_FD_len = min(min_FD_len, FDlen[i]);
//						}
//					}
//					//if(Aflag == 1 && (CurrentDelay >= PreviousDelay*(1-EPSILON))) {
//					if ((Aflag == 1 && (max_FD_len - min_FD_len)<EPSILON) || count == 100) {
//						System.out.print("The problem becomes infeasible.\n");
//						prInteger = 0;
//						break;
//					}
//					count++;
//				}
//
//				//increase the MM_Req
//				for(i = 0; i < NN; i++) {
//					for(n = 0; n < NN; n++) {
//						MM_Req[i][n] = min(Req[i][n], MM_Req[i][n] + STEP);
//						
//					}
//				}
//				if(prInteger == 0) {
//					for(i = 0; i < network.getNoLinks(); i++) {
//						System.out.print("When the problem is feasible Gflow[%d] = %f\n", i, Pflow[i]);
//					}
//				}	
//				for(i = 0; i < network.getNoLinks(); i++) {
//					Pflow[i] = Gflow[i];
//				}		
//			}
//		}
		return getGlobalFlows(network);
	}

	void SetLinkLens(Float[] Flow, Float[] Cap, Integer MsgLen, Float[] Len) {
		for(Integer l = 0; l < Flow.length; l++) {
			Len[l] = DerivDelay(Flow[l], Cap[l], MsgLen);
		}
	}

	void SetSP(FDMTopology network, Float Len[], LinkedList<Integer>[] Adj, Float SPdist[][], Integer SPpred[][]) {

		for(Integer node = 0; node < Adj.length; node++) {
			Bellman(node, network, Len, Adj, SPpred[node], SPdist[node]);
		}
	}

	void Bellman(Integer root, FDMTopology network, Float LinkLength[], LinkedList<Integer>[] Adj, Integer Pred[], Float Dist[]) {
		Integer[] Hop = new Integer[Adj.length];
		for (Integer i = 0; i < Adj.length; i++) {
			Dist[i] = Float.POSITIVE_INFINITY;
			Hop[i] = 0;
		}
		Dist[root] = 0.0f;
		Pred[root] = root;
		
		Stack<Integer> scanqueue = new Stack<Integer>();
//		stack<Integer> scanqueue;
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

	void LoadLinks(Float Req[][], Integer SPpred[][], FDMTopology network, Float Flow[]) {
		Integer m;
		Integer p;
		Integer link;
		for(Integer i = 0; i < network.getNoLinks(); i ++) {
			Flow[i] = 0.0f;
		}
		for(Integer s = 0; s < Req.length; s++) {
			for( Integer d = 0; d < Req.length; d++) {
				if(Req[s][d] > 0) {
					m = d;
					while(m != s) {
						link = SPpred[s][m];
						p = network.getEnd1(link);
						Flow[link] += Req[s][d];
						m = p;
					}	
				}
			}
		}
	}

Float AdjustCaps(Float Flow[], Float Cap[], Float NewCap[]) {
	Float factor = 1.0f;
	for( Integer i = 0; i < Flow.length; i++) {
		factor = Math.max(factor, (1+DELTA)*Flow[i]/Cap[i]);
	}
	for(Integer q = 0; q < Flow.length; q++) {
		NewCap[q] = factor*Cap[q];
	}
	return factor;
}

Float CalcDelay(Float Flow[], Float Cap[], Integer MsgLen, Float TotReq) {
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
