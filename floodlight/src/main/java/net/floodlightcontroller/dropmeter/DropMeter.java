package net.floodlightcontroller.dropmeter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import net.floodlightcontroller.routing.Path;
import net.floodlightcontroller.core.FloodlightContext;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFFlowModify;
import org.projectfloodlight.openflow.protocol.OFMeterFlags;
import org.projectfloodlight.openflow.protocol.OFMeterMod;
import org.projectfloodlight.openflow.protocol.OFMeterModCommand;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionMeter;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.meterband.OFMeterBand;
import org.projectfloodlight.openflow.protocol.meterband.OFMeterBandDrop;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.routing.Path;

import org.projectfloodlight.openflow.types.TransportPort;
import net.floodlightcontroller.fdmcalculator.FDMCalculator;
import net.floodlightcontroller.fdmcalculator.IFDMCalculatorService; 



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DropMeter{
	
		protected static int meterid = 1; 
	    //protected IFDMCalculatorService fdmservice;
	    
	    protected static final Logger log = LoggerFactory.getLogger(DropMeter.class);
		public DropMeter(FloodlightModuleContext context){
			//fdmservice = context.getServiceImpl(IFDMCalculatorService.class);
		}

		public DropMeter(){
			
		}
		
	    public Float createMeter(IOFSwitch currentSwitch, OFPort currentPort,IOFSwitch nextSwitch, OFPort nextPort ) {

	            /*Please change the rate here. The switch&port needed are passed as parameter to this function. */
	            //int rate  = 1000; 
	            //Float rate = (fdmservice.getFlowBW(currentSwitch, currentPort, nextSwitch, nextPort)*1000);
	            //rate = (int)rate*1000;
	            /*End of getRate()*/
	            //int rate = 10000;
	    		Float rate = 2.0f;//Float.POSITIVE_INFINITY;
	            createMeter(currentSwitch,currentPort,nextSwitch,nextPort,rate);
	            
	            return  rate;
	     }
	    
	    public int createMeter(IOFSwitch currentSwitch, OFPort currentPort,IOFSwitch nextSwitch, OFPort nextPort ,Float rate) {
	    	OFFactory meterFactory = OFFactories.getFactory(OFVersion.OF_13);
            OFMeterMod.Builder meterModBuilder = meterFactory.buildMeterMod()
                .setMeterId(meterid).setCommand(OFMeterModCommand.ADD);
            
            OFMeterBandDrop.Builder bandBuilder = meterFactory.meterBands().buildDrop()
                .setRate(Math.round(rate*1000));
            OFMeterBand band = bandBuilder.build();
            List<OFMeterBand> bands = new ArrayList<OFMeterBand>();
            bands.add(band);
  
            Set<OFMeterFlags> flags2 = new HashSet<>();
            flags2.add(OFMeterFlags.KBPS);
            meterModBuilder.setMeters(bands)
                .setFlags(flags2)
                .build();
                

            currentSwitch.write(meterModBuilder.build());
            return Math.round(rate);
	    	
	    }
	    
	    public void addpathtoFDMmodule(Path p){
	    	//this.fdmservice.addPath(p);
	    }
	    
	    
	    public void bindMeterWithFlow(OFPort inPort,IPv4Address dstIp, TransportPort dstPort, IPv4Address srcIp, IOFSwitch sw, TransportPort srcPort, Path path) {
	    	Match.Builder mb = sw.getOFFactory().buildMatch();
	    	
	    	log.info("bindMeter[ inport:" + inPort.toString()+
	    			" tcpdstPort:" + dstPort.toString()+
	    			" tcpip:" + srcIp.toString() +
	    			" sw:" + sw.toString() +
	    			" tcpsrcPort:" + srcPort.toString() + ']');
	    	mb.setExact(MatchField.IN_PORT, inPort)
	    	.setExact(MatchField.ETH_TYPE, EthType.IPv4)
	    	.setExact(MatchField.IPV4_SRC, srcIp)
	    	.setExact(MatchField.IPV4_DST, dstIp)
    		.setExact(MatchField.IP_PROTO, IpProtocol.TCP)
            .setExact(MatchField.TCP_SRC, srcPort)
            .setExact(MatchField.TCP_DST, dstPort);



            OFFactory my13Factory = OFFactories.getFactory(OFVersion.OF_13);
            ArrayList<OFInstruction> instructions = new ArrayList<OFInstruction>();
            ArrayList<OFAction> actionList = new ArrayList<OFAction>();
            OFInstructionMeter meter = my13Factory.instructions().buildMeter()
                .setMeterId(meterid)
                .build();
            OFActionOutput output = my13Factory.actions().buildOutput()
                .setPort(path.getPath().get(1).getPortId())
                .build();

            actionList.add(output);
            OFInstructionApplyActions applyActions = my13Factory.instructions().buildApplyActions()
                .setActions(actionList)
                .build();
            instructions.add(applyActions);
            instructions.add(meter);
            meterid++;

            OFFlowModify flowmod = my13Factory.buildFlowModify()
            		.setMatch(mb.build())
            		.setInstructions(instructions)
            		.build();
            sw.write(flowmod);
            		
//            OFFlowAdd flowAdd = my13Factory.buildFlowAdd()
//                    .setMatch(mb.build())
//                    .setInstructions(instructions)
//                    .setPriority(32768)
//                    .build();
//                sw.write(flowAdd);
	    }
	    
}