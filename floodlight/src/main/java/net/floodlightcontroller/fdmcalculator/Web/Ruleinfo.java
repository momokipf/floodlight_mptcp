package net.floodlightcontroller.fdmcalculator.Web;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.web.LinkWithType;

//import net.floodlightcontroller.linkdiscovery.web.LinkWithType;



@JsonSerialize(using=Ruleinfo.class)
public class Ruleinfo extends JsonSerializer<Ruleinfo> {
	public String srcSwDpid;
	public String srcPort;
	public String dstSwDpid;
	public String dstPort;
	
	public Float requirement = 0.0f;
	public Float capacity = Float.MAX_VALUE;
	
	public Ruleinfo(){}
	
	public Ruleinfo(String nodetuple,List<Float> value){
		String[] parts = nodetuple.split("-");
		this.srcSwDpid = parts[0];
		this.srcPort = parts[1];
		this.dstSwDpid = parts[2];
		this.dstPort = parts[3];

		this.requirement = value.get(0);
		this.capacity = value.get(1);
		
	}
	
	
	
	
	
	@Override
    public void serialize(Ruleinfo rf, JsonGenerator jgen, SerializerProvider arg2)
		throws IOException, JsonProcessingException {
		
		jgen.writeStartObject();
		
		jgen.writeStringField("src-switch", rf.srcSwDpid);
        jgen.writeStringField("src-port", rf.srcPort);
        jgen.writeStringField("dst-switch", rf.dstSwDpid);
        jgen.writeStringField("dst-port", rf.dstPort);
        
        jgen.writeNumberField("requirement", rf.requirement);
        jgen.writeNumberField("capacity",rf.capacity);
        jgen.writeEndObject();
		
	}
	@Override
    public Class<Ruleinfo> handledType() {
        return Ruleinfo.class;
    }
}