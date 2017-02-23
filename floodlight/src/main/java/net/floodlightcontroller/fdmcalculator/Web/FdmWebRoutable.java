package net.floodlightcontroller.fdmcalculator.Web;

import org.restlet.Context;
import org.restlet.routing.Router;

import net.floodlightcontroller.linkdiscovery.web.LinksResource;
import net.floodlightcontroller.restserver.RestletRoutable;
import net.floodlightcontroller.fdmcalculator.Web.FdmReq_CapResource;

public class FdmWebRoutable implements RestletRoutable{
	/**
     * Create the Restlet router and bind to the proper resources.
     */
	@Override
	public Router getRestlet(Context context){
		Router router = new Router(context);
		router.attach("/links/json", LinksResource.class);
		router.attach("/config/json",FdmReq_CapResource.class);
		
		
		return router;
	}
	
	
	@Override 
	public String basePath(){
		return "/wm/fdm";
	}
}
