package com.avioconsulting.mule.logger.internal;

import com.avioconsulting.mule.logger.api.processor.*;
import com.avioconsulting.mule.logger.api.processor.LogProperties.*;
import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import com.avioconsulting.mule.logger.internal.utils.CustomLoggerUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.runtime.parameter.CorrelationInfo;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

/**
 * Author: Chakri Bonthala
 */
public class CustomLoggerOperation {


    private final Map<String, Logger> loggerMap;

    private static final Map<LogLevel, Level> levelMap = new HashMap<LogLevel, Level>() {{
        put(LogLevel.INFO, Level.INFO);
        put(LogLevel.DEBUG, Level.DEBUG);
        put(LogLevel.TRACE, Level.TRACE);
        put(LogLevel.ERROR, Level.ERROR);
        put(LogLevel.WARN, Level.WARN);
        put(LogLevel.FATAL, Level.FATAL);
    }};


    public CustomLoggerOperation() {
        this.loggerMap = new HashMap<>();
    }

    /**
     * Author: Chakri Bonthala
     *
     */
    @MediaType(value = ANY, strict = false)
    public void log(@ParameterGroup(name = "Log") LogProperties logProperties,
                             @ParameterGroup(name = "Exception Details") ExceptionProperties exceptionProperties,
                             @ParameterGroup(name = "Additional Options") AdditionalProperties additionalProperties,
                             ComponentLocation location,
                             @Config CustomLoggerConfiguration loggerConfig,
                             CorrelationInfo correlationInfo) {


        Logger logger = CustomLoggerUtils.initLogger(loggerConfig.getDefaultCategory(), logProperties.getCategory());
        Level level = levelMap.get(logProperties.getLevel());

        Map<String, Object> logContext = new HashMap<>();
        logContext.put("timestamp", Instant.now().toString());
        logContext.put("appName", loggerConfig.getApplicationName());
        logContext.put("appVersion", loggerConfig.getApplicationVersion());
        logContext.put("env", loggerConfig.getEnvironment());
        logContext.put("messageAttributes", logProperties.getMessageAttributes());

        //This is because, we need to see what is in the nested object when the Hashmap is logged.
        Map<String, Object> logMap = new HashMap<>();
        String correlationId = logProperties.getCorrelationId() != null ? logProperties.getCorrelationId() : correlationInfo.getCorrelationId();
        logMap.put("correlationId", correlationId);
        logMap.put("message", logProperties.getMessage());
        logContext.put("log", logMap);

        //evaluate payload only when we actually need it
        ParameterResolver<String> payload = logProperties.getPayload();
        if (logger.isEnabled(level) && payload != null) {
            logMap.put("payload", payload.resolve());
        }

        if (exceptionProperties != null) {
            Map<String, Object> exceptionMap = new HashMap<>();
            exceptionMap.put("statusCode", exceptionProperties.getStatusCode());
            exceptionMap.put("type", exceptionProperties.getType());
            exceptionMap.put("detail", exceptionProperties.getDetail());
            logContext.put("exception", exceptionMap);
        }

        if (additionalProperties.isIncludeLocationInfo()) {
            logContext.put("location", getLocationInformation(location));
        }

        /*
           Check system property avio.logger.useMapMessage to turn on MapMessage usage in studio.
           This allows pattern layout to display specific attributes of the MapMessage
         */
        Message message;
        if("true".equalsIgnoreCase(System.getProperty("avio.logger.useMapMessage"))) {
            message = new MapMessage<>(logContext);
        } else {
            message = new ObjectMessage(logContext);
        }

        logger.log(level, message);
    }

    public static Map<String, String> getLocationInformation(ComponentLocation location) {
        Map<String, String> locationInfo = new TreeMap<>();
        locationInfo.put("location", location.getLocation());
        locationInfo.put("rootContainer", location.getRootContainerName());
        locationInfo.put("component", location.getComponentIdentifier().getIdentifier().toString());
        locationInfo.put("fileName", location.getFileName().orElse(""));
        locationInfo.put("lineInFile", String.valueOf(location.getLineInFile().orElse(null)));
        return locationInfo;
    }

    public void logV1(@ParameterGroup(name = "Log") LogProperties logProperties,
                    @ParameterGroup(name = "Exception Details") ExceptionProperties exceptionProperties,
                    @ParameterGroup(name = "Additional Options") AdditionalProperties additionalProperties,
                    ComponentLocation location,
                    @Config CustomLoggerConfiguration customLoggerConfiguration,
                    CorrelationInfo correlationInfo) {


        Logger logger = CustomLoggerUtils.initLogger(customLoggerConfiguration.getDefaultCategory(), logProperties.getCategory());

        Map<String, Object> logContext = new HashMap<>();
        logContext.put("app_name", customLoggerConfiguration.getApplicationName());
        logContext.put("app_version", customLoggerConfiguration.getApplicationVersion());
        logContext.put("env", customLoggerConfiguration.getEnvironment());
        logContext.put("timestamp", Instant.now().toString());
        logContext.put("ext", logProperties.getMessageAttributes());

        //This is because, we need to see what is in the nested object when the Hashmap is logged.
        Map<String, Object> logInner = new HashMap<>();
        String correlationId = logProperties.getCorrelationId() != null ? logProperties.getCorrelationId() : correlationInfo.getCorrelationId();
        logInner.put("correlation_id", correlationId);
        logInner.put("message", logProperties.getMessage());
        logContext.put("log", logInner);

        Level level = levelMap.get(logProperties.getLevel());
        //evaluate payload only when we actually need it
        ParameterResolver<String> payload = logProperties.getPayload();
        if (logger.isEnabled(level) && payload != null) {
            logInner.put("payload", payload.resolve());

        }

        if (exceptionProperties != null) {
            Map<String, Object> exceptionOnes = new HashMap<>();
            exceptionOnes.put("statusCode", exceptionProperties.getStatusCode());
            exceptionOnes.put("type", exceptionProperties.getType());
            exceptionOnes.put("detail", exceptionProperties.getDetail());

            logContext.put("exception", exceptionOnes);
        }
        if (additionalProperties.isIncludeLocationInfo()) {
            logContext.put("location", getLocationInformation(location));
        }

        /* 1.2.1 - Changed to MapMessage instead of ObjectMessage */
        /* Check system property - avio.custom.logger.env  */
        /* this lets us differentiate to use simplified logging locally */
        Message message;
        if("true".equalsIgnoreCase(System.getProperty("avio.logger.mapmessage"))) {
            message = new MapMessage<>(logContext);
        } else {
            message = new ObjectMessage(logContext);
        }
        logger.log(level, message);
    }
}