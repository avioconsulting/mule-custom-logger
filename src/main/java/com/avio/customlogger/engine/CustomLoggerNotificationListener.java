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
    private String appName = CustomLoggerConstants.DEFAULT_APP_NAME;
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
            classLogger.info("Set customLoggerUtils");
        }
        try {
            classLogger.debug("Locating avio-core:config global element");
            List<Component> avioConfig = configurationComponentLocator.find(ComponentIdentifier.builder().namespace("avio-core").name("config").build());
            Map<String, String> componentParameters = null;
            if (avioConfig.size() == 1) {
                try {
                    classLogger.debug("Located avio-core:config global element, attempting to retrieve properties");
                    componentParameters = (Map<String, String>) avioConfig.get(0).getAnnotations().get(QName.valueOf("{config}componentParameters"));
                } catch (Exception e) {
                    classLogger.error("Could not retrieve properties from avio-core:config global element");
                }
            }
            classLogger.debug("Retrieved properties from avio-core:config global element, attempting to parse and store in logContext");
            if (!CustomLoggerConstants.DEFAULT_APP_NAME.equals(appName)) {
                classLogger.debug("Used parameter for app_name: " + appName);
                logContext.put("app_name", customLoggerUtils.safeEvaluate(appName));
            } else if (componentParameters != null) {
                classLogger.debug("Used global-config for app_name: " + componentParameters.get("app_name"));
                logContext.put("app_name", customLoggerUtils.safeEvaluate(componentParameters.get("app_name")));
            } else {
                classLogger.debug("Used default for app_name: " + appName);
                logContext.put("app_name", customLoggerUtils.safeEvaluate(appName));
            }

            if (!CustomLoggerConstants.DEFAULT_APP_VERSION.equals(appVersion)) {
                classLogger.debug("Used parameter for app_version: " + appVersion);
                logContext.put("app_version", customLoggerUtils.safeEvaluate(appVersion));
            } else if (componentParameters != null) {
                classLogger.debug("Used global-config for app_version: " + componentParameters.get("app_version"));
                logContext.put("app_version", customLoggerUtils.safeEvaluate(componentParameters.get("app_version")));
            } else {
                classLogger.debug("Used default for app_version: " + appVersion);
                logContext.put("app_version", customLoggerUtils.safeEvaluate(appVersion));
            }

            if (!CustomLoggerConstants.DEFAULT_ENV.equals(env)) {
                classLogger.debug("Used parameter for env: " + env);
                logContext.put("env", customLoggerUtils.safeEvaluate(env));
            } else if (componentParameters != null) {
                classLogger.debug("Used global-config for env: " + componentParameters.get("env"));
                logContext.put("env", customLoggerUtils.safeEvaluate(componentParameters.get("env")));
            } else {
                classLogger.debug("Used default for env: " + env);
                logContext.put("env", customLoggerUtils.safeEvaluate(env));
            }

            if (!CustomLoggerConstants.DEFAULT_CATEGORY_PREFIX.equals(categoryPrefix)) {
                classLogger.debug("Used parameter for category prefix: " + categoryPrefix);
                this.logger = LogManager.getLogger(customLoggerUtils.safeEvaluate(categoryPrefix + CATEGORY_SUFFIX));
            } else if (componentParameters != null) {
                classLogger.debug("Used global-config for category prefix: " + componentParameters.get("category_prefix"));
                this.logger = LogManager.getLogger(customLoggerUtils.safeEvaluate(componentParameters.get("category_prefix")) + CATEGORY_SUFFIX);
            } else {
                classLogger.debug("Used default for category prefix: " + categoryPrefix);
                this.logger = LogManager.getLogger(customLoggerUtils.safeEvaluate(categoryPrefix + CATEGORY_SUFFIX));
            }
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
