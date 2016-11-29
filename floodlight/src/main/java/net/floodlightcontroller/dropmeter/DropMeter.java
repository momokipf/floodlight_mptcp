package net.floodlightcontroller.dropmeter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import net.floodlightcontroller.routing.Path;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
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

import org.projectfloodlight.openflow.types.TransportPort;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DropMeter{

	    public void createMeter(IOFSwitch currentSwitch, OFPort currentPort,IOFSwitch nextSwitch, OFPort nexttPort ) {
	    	 OFFactory meterFactory = OFFactories.getFactory(OFVersion.OF_13);
	            OFMeterMod.Builder meterModBuilder = meterFactory.buildMeterMod()
	                .setMeterId(meterid).setCommand(OFMeterModCommand.ADD);


	            /*Please change the rate here. The switch&port needed are passed as parameter to this function. */
	            int rate  = 1000; 
	            /*End of getRate()*/
	            
	            OFMeterBandDrop.Builder bandBuilder = meterFactory.meterBands().buildDrop()
	                .setRate(rate);
	            OFMeterBand band = bandBuilder.build();
	            List<OFMeterBand> bands = new ArrayList<OFMeterBand>();
	            bands.add(band);
	  
	            Set<OFMeterFlags> flags2 = new HashSet<>();
	            flags2.add(OFMeterFlags.KBPS);
	            meterModBuilder.setMeters(bands)
	                .setFlags(flags2).build();

	            currentSwitch.write(meterModBuilder.build());
	     }
	    
	    public void bindMeterWithFlow(OFPort inPort, TransportPort dstPort, IPv4Address srcIp, IOFSwitch sw, TransportPort srcPort, Path path) {
	    	Match.Builder mb = sw.getOFFactory().buildMatch();
	    	mb.setExact(MatchField.IN_PORT, inPort)
	    	.setExact(MatchField.ETH_TYPE, EthType.IPv4)
	    	.setExact(MatchField.IPV4_SRC, srcIp)
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

            OFFlowAdd flowAdd = my13Factory.buildFlowAdd()
                    .setMatch(mb.build())
                    .setInstructions(instructions)
                    .setPriority(32768)
                    .build();
                sw.write(flowAdd);
	    }
	    
	    protected static int meterid = 1; 
	  
}