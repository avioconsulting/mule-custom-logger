package com.avio.customlogger.internal.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ObjectMessage;
import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.notification.PipelineMessageNotification;
import org.mule.runtime.api.notification.PipelineMessageNotificationListener;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.core.api.expression.ExpressionRuntimeException;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.xml.namespace.QName;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
 * Listener for Mule notifications on flow start, end and completion.
 */
public class LoggingPipelineNotificationListener
        implements PipelineMessageNotificationListener<PipelineMessageNotification> {

    private final org.slf4j.Logger classLogger = LoggerFactory.getLogger(LoggingPipelineNotificationListener.class);
    public static final String CATEGORY_DEFAULT = "com.avioconsulting.default";
    public static final String CATEGORY_SUFFIX = ".flow";
    public static final String APP_NAME_DEFAULT = "UNKNOWN_APP_NAME";
    public static final String APP_VERSION_DEFAULT = "UNKNOWN_APP_VERSION";
    public static final String ENV_DEFAULT = "UNKNOWN_ENV";
    private Logger logger = null;
    private Map<String, Object> logContext = new HashMap<>();
    private Map<String, Object> logInner = new HashMap<>();
    @Parameter
    private String baseCategory = CATEGORY_DEFAULT;
    @Parameter
    private String appName = APP_NAME_DEFAULT;
    @Parameter
    private String appVersion = APP_VERSION_DEFAULT;
    @Parameter
    private String env = ENV_DEFAULT;

    @Inject
    ConfigurationComponentLocator configurationComponentLocator;

    @Inject
    Registry registry;

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
        logContext.put("app_name", appName);
        logContext.put("app_version", appVersion);
        logContext.put("env", env);
        classLogger.debug("Set defaults, locating avio-core:config global element");
        List<Component> config = configurationComponentLocator.find(ComponentIdentifier.builder().namespace("avio-core").name("config").build());
        Optional<ExpressionManager> expressionManager = registry.lookupByType(ExpressionManager.class);
        Map<String, String> componentParameters = null;
        if (config.size() == 1 && expressionManager.isPresent()) {
            try {
                classLogger.debug("Located avio-core:config global element, attempting to retrieve properties");
                componentParameters = (Map<String, String>) config.get(0).getAnnotations().get(QName.valueOf("{config}componentParameters"));
            } catch (Exception e) {
                classLogger.error("Could not retrieve properties from avio-core:config global element");
            }
        }
        if (componentParameters != null) {
            classLogger.debug("Retrieved properties from avio-core:config global element, attempting to parse and store in logContext");
            if (APP_NAME_DEFAULT.equals(appName))
                logContext.put("app_name", safeEvaluate(expressionManager.get(), componentParameters.get("app_name")));
            if (APP_VERSION_DEFAULT.equals(appVersion))
                logContext.put("app_version", safeEvaluate(expressionManager.get(), componentParameters.get("app_version")));
            if (ENV_DEFAULT.equals(env))
                logContext.put("env", safeEvaluate(expressionManager.get(), componentParameters.get("env")));
            if (CATEGORY_DEFAULT.equals(baseCategory))
                this.logger = LogManager.getLogger(safeEvaluate(expressionManager.get(), componentParameters.get("baseCategory")) + CATEGORY_SUFFIX);
        }
        if (this.logger == null)
            this.logger = LogManager.getLogger(baseCategory + CATEGORY_SUFFIX);
    }

    private Object safeEvaluate(ExpressionManager expressionManager, String expression) {
        if (expression.charAt(0) == '#' && expression.charAt(1) == '[' && expression.charAt(expression.length() - 1) == ']') {
            try {
                return expressionManager.evaluate(expression).getValue().toString();
            } catch (ExpressionRuntimeException error) {
                classLogger.error("There was an error evaluating the following expression: " + expression);
            }
        }
        return expression;
    }

    public void setBaseCategory(String baseCategory) {
        this.baseCategory = baseCategory;
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
