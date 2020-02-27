package org.wso2.custom.event.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.governance.IdentityGovernanceException;
import org.wso2.carbon.identity.governance.common.IdentityConnectorConfig;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.custom.event.handler.internal.BlockedReasonTaskDataHolder;
import org.wso2.carbon.identity.application.common.model.Property;

import java.sql.Timestamp;
import java.util.*;

public class CustomEventHandler extends AbstractEventHandler implements IdentityConnectorConfig {

    private static final String BLOCKED_REASON = "http://wso2.org/claims/identity/m2mSubStatus";
    private static final String ACCOUNT_DISABLED = "http://wso2.org/claims/identity/accountDisabled";
    private static final String LAST_LOGIN_TIME = "http://wso2.org/claims/lastLoginTime";
    private static final String PASSWORD_EXPIRY = "http://wso2.org/claims/orclActiveEndDate";
    private static final String ACCOUNT_DISABLE_DELAY = "suspension.notification.account.disable.delay";
    private static final long CONFIGURED_IDLE_ACTIVITY_TIME = 192182;
    private static final Log log = LogFactory.getLog(CustomEventHandler.class);

    @Override
    public void handleEvent(Event event) throws IdentityEventException {
        Map<String, Object> eventProperties = event.getEventProperties();
        Map<String, String> claims = (Map<String, String>) eventProperties.get(IdentityEventConstants.EventProperty
                .USER_CLAIMS);
        Property[] identityProperties = null;
        int disableDelay = 0;
        UserStoreManager userStoreManager = (UserStoreManager) eventProperties.get(IdentityEventConstants.EventProperty.USER_STORE_MANAGER);
        String userName = (String) eventProperties.get(IdentityEventConstants.EventProperty.USER_NAME);
        try {
            identityProperties = BlockedReasonTaskDataHolder.getInstance().getIdentityGovernanceService()
                    .getConfiguration(getPropertyNames(), (String)eventProperties.get(IdentityEventConstants.EventProperty.TENANT_DOMAIN));
        } catch (IdentityGovernanceException e) {
           log.error("Error while reading the configurations");
        }

        for (Property identityProperty : identityProperties) {
            if (identityProperty == null) {
                continue;
            }
            if(ACCOUNT_DISABLE_DELAY.equals(identityProperty.getName())){
                disableDelay = Integer.parseInt(identityProperty.getValue());
            }
        }
        boolean isAccountDisabled = true;
        String blockedReason = null;
        try {
            Map<String, String> values = userStoreManager.getUserClaimValues(userName, new String[]{
                    ACCOUNT_DISABLED, BLOCKED_REASON}, UserCoreConstants.DEFAULT_PROFILE);
            isAccountDisabled = Boolean.parseBoolean(values.get(ACCOUNT_DISABLED));
            blockedReason = values.get(BLOCKED_REASON);
        } catch (UserStoreException e) {
            log.error("Error when retrieving the User claims");
        }
        if (isAccountDisabled && blockedReason.isEmpty()) {
            if (isAccountIdle(Integer.parseInt(claims.get(LAST_LOGIN_TIME)), disableDelay)){
                blockedReason = "IDLE";
            } else if (Long.parseLong(claims.get(PASSWORD_EXPIRY)) > CONFIGURED_IDLE_ACTIVITY_TIME) {
                blockedReason = "PASSWORD_EXPIRY";
            } else {
                blockedReason = "ADMINISTRATOR";
            }
            setUserClaim(BLOCKED_REASON, blockedReason, userStoreManager, userName);
        }
    }

    protected void setUserClaim(String claimName, String claimValue, UserStoreManager userStoreManager, String userName) throws IdentityEventException {

        HashMap userClaims = new HashMap();
        userClaims.put(claimName, claimValue);
        try {
            userStoreManager.setUserClaimValues(userName, userClaims, null);
        } catch (UserStoreException e) {
            throw new IdentityEventException("Error while setting user claim value :" + userName, e);
        }

    }

    public String getFriendlyName() {
        return "blockReasonHandler";
    }

    public String getCategory() {
        return null;
    }

    public String getSubCategory() {
        return null;
    }

    public int getOrder() {
        return 50;
    }

    public Map<String, String> getPropertyNameMapping() {
        return null;
    }

    public Map<String, String> getPropertyDescriptionMapping() {
        return null;
    }

    public String[] getPropertyNames() {

        List<String> properties = new ArrayList();
        properties.add(ACCOUNT_DISABLE_DELAY);
//        properties.add(PasswordHistoryConstants.PW_HISTORY_COUNT);
        return properties.toArray(new String[properties.size()]);
    }

    public Properties getDefaultPropertyValues(String s) throws IdentityGovernanceException {
        return null;
    }

    public Map<String, String> getDefaultPropertyValues(String[] strings, String s) throws IdentityGovernanceException {
        return null;
    }

    @Override
    public String getName() {
        return "blockReasonHandler";
    }

    private boolean isAccountIdle(int lastLogin, int delay) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        int diffInDays = (int)(timestamp.getTime() - lastLogin) / (1000 * 60 * 60 * 24);
        return delay > diffInDays;
    }
}
