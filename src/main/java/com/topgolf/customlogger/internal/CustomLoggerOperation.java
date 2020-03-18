package com.topgolf.customlogger.internal;

import com.topgolf.customlogger.internal.model.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ObjectMessage;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

/**
 * Author: Chakri Bonthala
 */
public class CustomLoggerOperation {

    private Logger logger;

    /**
     * Author: Chakri Bonthala
     *
     * @return
     */
    @MediaType(value = ANY, strict = false)
    public void customLogger(@ParameterGroup(name = "Log") LogProperties logProperties,
                             @ParameterGroup(name = "Extended") ExtendedProperties extendedProperties,
                             @ParameterGroup(name = "Exception") ExceptionProperties exceptionProperties,
                             @ParameterGroup(name = "Options") LogLocationInfoProperty logLocationInfoProperty,
                             ComponentLocation location,
                             @Config CustomLoggerConfiguration customLoggerConfiguration) {

        if (logProperties.getCategory() != null)
            initLogger(logProperties.getCategory());
        else initLogger("com.integration-platform.topgolf.api");

        final Map<LoggerLevelProperty.LogLevel, Level> levelMap = getMappings();

        Map<String, Object> logContent = new HashMap<>();
        logContent.put("app_name", customLoggerConfiguration.getApp_name());
        logContent.put("app_version", customLoggerConfiguration.getApp_version());
        logContent.put("env", customLoggerConfiguration.getEnv());
        logContent.put("timestamp", Instant.now().toString());
        logContent.put("ext", extendedProperties.getProperties());

        //This is because, we need to see what is in the nested object when the Hashmap is logged.
        Map<String, Object> logOnes = new HashMap<>();
        logOnes.put("correlation_id", logProperties.getCorrelation_id());
        logOnes.put("message", logProperties.getMessage());
        logOnes.put("payload", logProperties.getPayload());
        logOnes.put("trace_point", logProperties.getTracePoint());
        logContent.put("log", logOnes);

        if (exceptionProperties != null) {
            Map<String, Object> exceptionOnes = new HashMap<>();
            exceptionOnes.put("statusCode", exceptionProperties.getStatus_code());
            exceptionOnes.put("type", exceptionProperties.getType());
            exceptionOnes.put("detail", exceptionProperties.getDetail());

            logContent.put("exception", exceptionOnes);
        }

        if (logLocationInfoProperty.logLocationInfo) {
            Map<String, String> locationInfo = new HashMap<>();
            locationInfo.put("location", location.getLocation());
            locationInfo.put("root_container", location.getRootContainerName());
            locationInfo.put("component", location.getComponentIdentifier().getIdentifier().toString());
            locationInfo.put("file_name", location.getFileName().orElse(""));
            locationInfo.put("line_in_file", String.valueOf(location.getLineInFile().orElse(null)));

            logContent.put("location", locationInfo);
        }
        ObjectMessage objectMessage = new ObjectMessage(logContent);
        logger.log(levelMap.get(logProperties.getLog_level()), objectMessage);
    }

    private void initLogger(String category) {
        this.logger = LogManager.getLogger(category);
    }

    private static Map<LoggerLevelProperty.LogLevel, Level> getMappings() {
        Map<LoggerLevelProperty.LogLevel, Level> map = new HashMap<>();
        map.put(LoggerLevelProperty.LogLevel.INFO, Level.INFO);
        map.put(LoggerLevelProperty.LogLevel.DEBUG, Level.DEBUG);
        map.put(LoggerLevelProperty.LogLevel.TRACE, Level.TRACE);
        map.put(LoggerLevelProperty.LogLevel.ERROR, Level.ERROR);
        map.put(LoggerLevelProperty.LogLevel.WARN, Level.WARN);
        map.put(LoggerLevelProperty.LogLevel.FATAL, Level.FATAL);
        return map;
    }
}