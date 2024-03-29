package com.wso2.validator.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(name = "com.wso2.validator.LocalEntryExecutor", immediate = true)
public class APIPublishExecutorComponent {
    private static Log log = LogFactory.getLog(APIPublishExecutorComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("OB Publisher Executor component is activated ");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("OB Publisher Executor is deactivated");
        }
    }
}
