package com.avioconsulting.mule.logger.internal;

import com.avioconsulting.mule.logger.api.processor.*;
import com.avioconsulting.mule.logger.internal.utils.CustomLoggerUtils;
import org.apache.logging.log4j.Logger;
import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.runtime.parameter.CorrelationInfo;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.route.Chain;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

public class CustomLoggerTimerScopeOperations {
    public static final String DEFAULT_CATEGORY_SUFFIX = "timer";
    private final org.slf4j.Logger classLogger = LoggerFactory.getLogger(CustomLoggerTimerScopeOperations.class);

    @Inject
    Registry registry;

    @Inject
    ExpressionManager expressionManager;

    private Logger logger;
    private CustomLoggerUtils customLoggerUtils;

    @MediaType(value = ANY, strict = false)
    @DisplayName(value = "Logging Timer Scope")
    public void timerScope(@DisplayName(value = "Timer Name") String timerName,
                           @ParameterGroup(name = "Log") LogProperties logProperties,
                           @ParameterGroup(name = "Options") AdditionalProperties additionalProperties,
                           ComponentLocation location,
                           CorrelationInfo correlationInfo,
                           Chain operations,
                           CompletionCallback<Object, Object> callback) {

        Map<String, String> loggerConfigAttributes = CustomLoggerUtils.getGlobalConfigAttributes(registry, "avio-logger", "config");
        classLogger.info("Global Config Attributes: " + loggerConfigAttributes.toString());


        CustomLogger logger = new CustomLogger();
        ExceptionProperties exceptionProperties = new ExceptionProperties();
        String correlationId = correlationInfo.getCorrelationId();
        String applicationName = CustomLoggerUtils.retrieveValueFromGlobalConfig(expressionManager, loggerConfigAttributes, "applicationName");
        String applicationVersion = CustomLoggerUtils.retrieveValueFromGlobalConfig(expressionManager, loggerConfigAttributes, "applicationVersion");
        String environment = CustomLoggerUtils.retrieveValueFromGlobalConfig(expressionManager, loggerConfigAttributes, "environment");
        String defaultCategory = CustomLoggerUtils.retrieveValueFromGlobalConfig(expressionManager, loggerConfigAttributes, "defaultCategory");
        String logLevel = CustomLoggerUtils.retrieveValueFromGlobalConfig(expressionManager, loggerConfigAttributes, "level");
        classLogger.debug("Log Level: " + logLevel);
        Boolean enableV1Compatibility = Boolean.valueOf(CustomLoggerUtils.retrieveValueFromGlobalConfig(expressionManager, loggerConfigAttributes, "enableV1Compatibility"));
        MessageAttributes messageAttributes = new MessageAttributes();

        long startTime = System.currentTimeMillis();
        MessageAttribute timerNameAtt = new MessageAttribute("timerName", timerName);
        messageAttributes.getAttributeList().add(timerNameAtt);
        operations.process(
                result -> {
                    long elapsedMilliseconds = System.currentTimeMillis() - startTime;
                    MessageAttribute elapsed = new MessageAttribute("elapsedTimeMs", String.valueOf(elapsedMilliseconds));
                    messageAttributes.getAttributeList().add(elapsed);
                    logProperties.setMessage("Timer scope [" + timerName + "] completed in " + elapsedMilliseconds + "ms");
                    logger.log(logProperties, messageAttributes, exceptionProperties, additionalProperties, location, correlationId, applicationName, applicationVersion, environment, defaultCategory, enableV1Compatibility);
                    callback.success(result);
                },
                (error, previous) -> {
                    long elapsedMilliseconds = System.currentTimeMillis() - startTime;
                    MessageAttribute elapsed = new MessageAttribute("elapsedTimeMs", String.valueOf(elapsedMilliseconds));
                    messageAttributes.getAttributeList().add(elapsed);
                    logProperties.setMessage("Timer scope [" + timerName + "] completed with errors in " + elapsedMilliseconds + "ms");
                    logProperties.setLevel(LogProperties.LogLevel.ERROR);
                    logger.log(logProperties, messageAttributes, exceptionProperties, additionalProperties, location, correlationId, applicationName, applicationVersion, environment, defaultCategory, enableV1Compatibility);
                    callback.error(error);
                });
    }

}
