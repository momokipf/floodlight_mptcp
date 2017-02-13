/**
 *    Copyright 2013, Big Switch Networks, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

package net.floodlightcontroller.linkdiscovery.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.floodlightcontroller.linkdiscovery.ILinkDiscovery.LinkDirection;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery.LinkType;
import net.floodlightcontroller.linkdiscovery.internal.LinkInfo;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.linkdiscovery.Link;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;

public class LinksResource extends ServerResource {

	private static final String STR_SRCSW = "src-switch";
	private static final String STR_SRCPOR ="src-port"; 
	private static final String STR_DSTSW = "dst-switch";
	private static final String STR_DSTPOR = "dst-port";
	private static final String STR_LAT = "latency";
	private static final String STR_CAP = "capacity";
	
	
    @Get("json")
    public Set<LinkWithType> retrieve() {
        ILinkDiscoveryService ld = (ILinkDiscoveryService)getContext().getAttributes().
                get(ILinkDiscoveryService.class.getCanonicalName());
        Map<Link, LinkInfo> links = new HashMap<Link, LinkInfo>();
        Set<LinkWithType> returnLinkSet = new HashSet<LinkWithType>();

        if (ld != null) {
            links.putAll(ld.getLinks());
            for (Link link: links.keySet()) {
                LinkInfo info = links.get(link);
                LinkType type = ld.getLinkType(link, info);
                if (type == LinkType.DIRECT_LINK || type == LinkType.TUNNEL) {
                    LinkWithType lwt;

                    DatapathId src = link.getSrc();
                    DatapathId dst = link.getDst();
                    OFPort srcPort = link.getSrcPort();
                    OFPort dstPort = link.getDstPort();
                    Link otherLink = new Link(dst, dstPort, src, srcPort, U64.ZERO /* not important in lookup */);
                    LinkInfo otherInfo = links.get(otherLink);
                    LinkType otherType = null;
                    if (otherInfo != null)
                        otherType = ld.getLinkType(otherLink, otherInfo);
                    if (otherType == LinkType.DIRECT_LINK ||
                            otherType == LinkType.TUNNEL) {
                        // This is a bi-direcitonal link.
                        // It is sufficient to add only one side of it.
                        if ((src.getLong() < dst.getLong()) || (src.getLong() == dst.getLong()
                        		&& srcPort.getPortNumber() < dstPort.getPortNumber())) {
                            lwt = new LinkWithType(link,
                                    type,
                                    LinkDirection.BIDIRECTIONAL);
                            returnLinkSet.add(lwt);
                        }
                    } else {
                        // This is a unidirectional link.
                        lwt = new LinkWithType(link,
                                type,
                                LinkDirection.UNIDIRECTIONAL);
                        returnLinkSet.add(lwt);

                    }
                }
            }
        }
        return returnLinkSet;
    }
    
    
    @Post("json")
    public Set<LinkWithType> storeLink(String json){
    	ILinkDiscoveryService ld = (ILinkDiscoveryService)getContext().getAttributes().
                get(ILinkDiscoveryService.class.getCanonicalName());
    	Map<Link, LinkInfo> links = new HashMap<Link, LinkInfo>();
        Set<LinkWithType> returnLinkSet = new HashSet<LinkWithType>();
        
        MappingJsonFactory f = new MappingJsonFactory();
		JsonParser jp = null;
		String src_sw = null;
		String src_port = null;
		String dst_sw = null;
		String dst_port = null;
		Float cap = Float.MAX_VALUE;
		
		try{
			try{
				jp = f.createParser(json);
			}
			catch(IOException e){
				e.printStackTrace();
			}
			jp.nextToken();
			if(jp.getCurrentToken() != JsonToken.START_OBJECT){
				throw new IOException("Expected START_OBJECT");
			}
			
			while(jp.nextToken()!=JsonToken.END_OBJECT){ 
				if(jp.getCurrentToken()!=JsonToken.FIELD_NAME){
					throw new IOException("Expected FIELD_NAME");
				}
			}
			
			String n = jp.getCurrentName().toLowerCase();
			jp.nextToken();
			switch(n){
			case STR_SRCSW: src_sw = jp.getText();
							break;
			case STR_SRCPOR:src_port = jp.getText();
							break;
			case STR_DSTSW: dst_sw = jp.getText();
							break;
			case STR_DSTPOR:dst_port = jp.getText();
							break;
			case STR_CAP: cap = Float.parseFloat( jp.getText());
							break;
			default : break;
			}
			
		}
    	catch(IOException e){
    		e.printStackTrace();
    		
    	}
		
		
		if(ld!=null){
			links.putAll(ld.getLinks());
			for(Link link:links.keySet()){
				LinkInfo info = links.get(link);
                LinkType type = ld.getLinkType(link, info);
                if (type == LinkType.DIRECT_LINK || type == LinkType.TUNNEL) {
                	LinkWithType lwt;

                    DatapathId src = link.getSrc();
                    DatapathId dst = link.getDst();
                    OFPort srcPort = link.getSrcPort();
                    OFPort dstPort = link.getDstPort();
                    if(src.toString().equals(src_sw)&&dst.toString().equals(dst_sw)
                    		&&srcPort.toString().equals(src_port)&&dstPort.toString().equals(dst_port)){
                    	System.out.println("here is a link match");
                    	link.setCapacity(cap);
                    }
                    Link otherLink = new Link(dst, dstPort, src, srcPort, U64.ZERO /* not important in lookup */);
                    LinkInfo otherInfo = links.get(otherLink);
                    LinkType otherType = null;
                    if (otherInfo != null)
                        otherType = ld.getLinkType(otherLink, otherInfo);
                    if (otherType == LinkType.DIRECT_LINK ||
                            otherType == LinkType.TUNNEL) {
                        // This is a bi-direcitonal link.
                        // It is sufficient to add only one side of it.
                        if ((src.getLong() < dst.getLong()) || (src.getLong() == dst.getLong()
                        		&& srcPort.getPortNumber() < dstPort.getPortNumber())) {
                            lwt = new LinkWithType(link,
                                    type,
                                    LinkDirection.BIDIRECTIONAL);
                            returnLinkSet.add(lwt);
                        }
                    } else {
                        // This is a unidirectional link.
                        lwt = new LinkWithType(link,
                                type,
                                LinkDirection.UNIDIRECTIONAL);
                        returnLinkSet.add(lwt);

                    }
                    
                }
                
			}	
		}
		return returnLinkSet;
    }
    
}
