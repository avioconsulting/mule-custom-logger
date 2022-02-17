package com.avioconsulting.mule.logger.internal.listeners;

import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import com.avioconsulting.mule.logger.internal.utils.CustomLoggerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ObjectMessage;
import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.notification.PipelineMessageNotification;
import org.mule.runtime.api.notification.PipelineMessageNotificationListener;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/*
 * Listener for Mule notifications on flow start, end and completion.
 */
public class CustomLoggerNotificationListener
        implements PipelineMessageNotificationListener<PipelineMessageNotification> {

    private final org.slf4j.Logger classLogger = LoggerFactory.getLogger(CustomLoggerNotificationListener.class);
    public static final String DEFAULT_CATEGORY_SUFFIX = ".flow";
    private Logger logger = null;
    private Map<String, Object> logContext = new HashMap<>();
    private Map<String, Object> logInner = new HashMap<>();
    @Parameter
    private String categoryPrefix = CustomLoggerConfiguration.DEFAULT_CATEGORY_PREFIX;
    @Parameter
    private String categorySuffix = DEFAULT_CATEGORY_SUFFIX;
    @Parameter
    private String appName = CustomLoggerConfiguration.DEFAULT_APP_NAME;
    @Parameter
    private String appVersion = CustomLoggerConfiguration.EXAMPLE_APP_VERSION;
    @Parameter
    private String env = CustomLoggerConfiguration.DEFAULT_ENV;
    @Parameter
    private String moduleConfigurationName;

    private CustomLoggerConfiguration config;

    @Inject
    Registry registry;
    private CustomLoggerUtils customLoggerUtils;

    public CustomLoggerNotificationListener(CustomLoggerConfiguration config) {
        this.config = config;
    }

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
            customLoggerUtils = new CustomLoggerUtils(registry, moduleConfigurationName);
        }
        try {
            logContext.put("app_name", customLoggerUtils.decideOnValue(CustomLoggerConfiguration.DEFAULT_APP_NAME, appName, "app_name"));
            logContext.put("app_version", customLoggerUtils.decideOnValue(CustomLoggerConfiguration.EXAMPLE_APP_VERSION, appVersion, "app_version"));
            logContext.put("env", customLoggerUtils.decideOnValue(CustomLoggerConfiguration.DEFAULT_ENV, env, "env"));
            this.logger = LogManager.getLogger(customLoggerUtils.decideOnValue(CustomLoggerConfiguration.DEFAULT_CATEGORY_PREFIX, categoryPrefix, "category_prefix") + categorySuffix);
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

    public void setModuleConfigurationName(String moduleConfigurationName) {
        this.moduleConfigurationName = moduleConfigurationName;
    }
}
