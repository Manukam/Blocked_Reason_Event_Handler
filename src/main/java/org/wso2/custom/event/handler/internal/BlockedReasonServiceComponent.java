package org.wso2.custom.event.handler.internal;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.governance.IdentityGovernanceService;
import org.wso2.custom.event.handler.CustomEventHandler;


@Component(
        name = "BlockedReasonServiceComponent",
        immediate = true)
public class BlockedReasonServiceComponent {

    private static final Logger log = Logger.getLogger(BlockedReasonServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Identity Management Listener is enabled");
            }
            BundleContext bundleContext = context.getBundleContext();
            BlockedReasonTaskDataHolder.getInstance().setBundleContext(bundleContext);
            CustomEventHandler handler = new CustomEventHandler();
            context.getBundleContext().registerService(AbstractEventHandler.class.getName(), handler, null);
        } catch (Exception e) {
            log.error("Error while activating identity governance component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("Identity Management bundle is de-activated");
        }
    }

    protected void unsetIdentityGovernanceService(IdentityGovernanceService idpManager) {

        BlockedReasonTaskDataHolder.getInstance().setIdentityGovernanceService(null);
    }

    @Reference(
            name = "IdentityGovernanceService",
            service = org.wso2.carbon.identity.governance.IdentityGovernanceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityGovernanceService")
    protected void setIdentityGovernanceService(IdentityGovernanceService idpManager) {

        BlockedReasonTaskDataHolder.getInstance().setIdentityGovernanceService(idpManager);
    }
}
