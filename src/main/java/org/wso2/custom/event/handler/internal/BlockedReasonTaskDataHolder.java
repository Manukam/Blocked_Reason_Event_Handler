package org.wso2.custom.event.handler.internal;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.identity.governance.IdentityGovernanceService;

public class BlockedReasonTaskDataHolder  {

    private static BlockedReasonTaskDataHolder instance = new BlockedReasonTaskDataHolder();
    private IdentityGovernanceService identityGovernanceService;
    private BundleContext bundleContext;

    private BlockedReasonTaskDataHolder() {
    }

    public static BlockedReasonTaskDataHolder getInstance() {

        return instance;
    }

    public IdentityGovernanceService getIdentityGovernanceService() {
        return identityGovernanceService;
    }

    public void setIdentityGovernanceService(IdentityGovernanceService identityGovernanceService) {
        this.identityGovernanceService = identityGovernanceService;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
