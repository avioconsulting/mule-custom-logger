package com.avio.customlogger.engine;

import com.avio.customlogger.utils.CustomLoggerUtils;
import com.avio.customlogger.utils.CustomLoggerConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ObjectMessage;
import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.notification.PipelineMessageNotification;
import org.mule.runtime.api.notification.PipelineMessageNotificationListener;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.xml.namespace.QName;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.avio.customlogger.utils.CustomLoggerConstants.*;

/*
 * Listener for Mule notifications on flow start, end and completion.
 */
public class CustomLoggerNotificationListener
        implements PipelineMessageNotificationListener<PipelineMessageNotification> {

    private final org.slf4j.Logger classLogger = LoggerFactory.getLogger(CustomLoggerNotificationListener.class);
    public static final String CATEGORY_SUFFIX = ".flow";
    private Logger logger = null;
    private Map<String, Object> logContext = new HashMap<>();
    private Map<String, Object> logInner = new HashMap<>();
    @Parameter
    private String categoryPrefix = CustomLoggerConstants.DEFAULT_CATEGORY_PREFIX;
    @Parameter
    private String appName = DEFAULT_APP_NAME;
    @Parameter
    private String appVersion = CustomLoggerConstants.DEFAULT_APP_VERSION;
    @Parameter
    private String env = CustomLoggerConstants.DEFAULT_ENV;

    @Inject
    ConfigurationComponentLocator configurationComponentLocator;

    @Inject
    Registry registry;
    private CustomLoggerUtils customLoggerUtils;

    @Override
    public void onNotification(PipelineMessageNotification notification) {
        if (logger == null) {
            configureLogger();
        }
        switch (notification.getAction().getActionId()) {
            case PipelineMessageNotification.PROCESS_START:
                logContext.put("timestamp", Instant.now().toString());
                logContext.put("flow_name", notification.getResourceIdentifier());
                logInner.put("message", notification.getResourceIdentifier() + " starting");
                logInner.put("trace_point", "FLOW_START");
                logContext.put("log", logInner);
                logger.info(new ObjectMessage(logContext));
                break;

            case PipelineMessageNotification.PROCESS_COMPLETE:
                logContext.put("timestamp", Instant.now().toString());
                logContext.put("flow_name", notification.getResourceIdentifier());
                logInner.put("message", notification.getResourceIdentifier() + " ending");
                logInner.put("trace_point", "FLOW_END");
                logContext.put("log", logInner);
                logger.info(new ObjectMessage(logContext));
                break;
        }
    }

    private void configureLogger() {
        if (customLoggerUtils == null) {
            customLoggerUtils = new CustomLoggerUtils(registry);
        }
        try {
            logContext.put("app_name", customLoggerUtils.decideOnValue(DEFAULT_APP_NAME, appName, "app_name"));
            logContext.put("app_version", customLoggerUtils.decideOnValue(DEFAULT_APP_VERSION, appVersion, "app_version"));
            logContext.put("env", customLoggerUtils.decideOnValue(DEFAULT_ENV, env, "env"));
            this.logger = LogManager.getLogger(customLoggerUtils.decideOnValue(DEFAULT_CATEGORY_PREFIX, categoryPrefix, "category_prefix") + CATEGORY_SUFFIX);
        } catch (NullPointerException e) {
            classLogger.error("ERROR: ", e);
        }
    }

    public void setCategoryPrefix(String categoryPrefix) {
        this.categoryPrefix = categoryPrefix;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public void setEnv(String env) {
        this.env = env;
    }
}
