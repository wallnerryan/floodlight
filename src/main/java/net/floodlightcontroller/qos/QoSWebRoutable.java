package net.floodlightcontroller.qos;

import org.restlet.Context;
import org.restlet.routing.Router;

import net.floodlightcontroller.qos.QoSResource;
import net.floodlightcontroller.qos.QoSPoliciesResource;
import net.floodlightcontroller.restserver.RestletRoutable;

public class QoSWebRoutable implements RestletRoutable{
	
	/**
     * Create the Restlet router and bind to the proper resources.
     */
    @Override
    public Router getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/{op}/json", QoSResource.class);
        router.attach("/{add_or_del}/policy/{sw_or_all}/json", QoSPoliciesResource.class);
        router.attach("/e2e/policy/{h-src}/{h-dst}/json", QoSE2EPoliciesResource.class);
        
        //*****************
        //*****************
        // TODO Add support for add -> all, add -> sw, add -> sw1/to/sw2 & deletes
        // DELETE POLICY will take care of deleting for end to end policies
        //*****************
        //*****************
        
        return router;
    }

    /**
     * Set the base path for the Quality of Service
     */
    @Override
    public String basePath() {
        return "/wm/qos";
    }

}
