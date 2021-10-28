package com.avio.customlogger;

import com.avio.customlogger.model.*;
import com.avio.customlogger.utils.CustomLoggerUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.ObjectMessage;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.avio.customlogger.utils.CustomLoggerUtils.getLocationInformation;
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

        this.logger = CustomLoggerUtils.initLogger(customLoggerConfiguration.getCategory_prefix(), logProperties.getCategory());

        final Map<LoggerLevelProperty.LogLevel, Level> levelMap = getMappings();

        Map<String, Object> logContext = new HashMap<>();
        logContext.put("app_name", customLoggerConfiguration.getApp_name());
        logContext.put("app_version", customLoggerConfiguration.getApp_version());
        logContext.put("env", customLoggerConfiguration.getEnv());
        logContext.put("timestamp", Instant.now().toString());
        logContext.put("ext", extendedProperties.getProperties());

        //This is because, we need to see what is in the nested object when the Hashmap is logged.
        Map<String, Object> logInner = new HashMap<>();
        logInner.put("correlation_id", logProperties.getCorrelation_id());
        logInner.put("message", logProperties.getMessage());
        logInner.put("trace_point", logProperties.getTracePoint());
        logContext.put("log", logInner);

        Level level = levelMap.get(logProperties.getLog_level());
        //evaluate payload only when we actually need it
        ParameterResolver<String> payload = logProperties.getPayload();
        if (logger.isEnabled(level) && payload != null) {
            logInner.put("payload", payload.resolve());

        }

        if (exceptionProperties != null) {
            Map<String, Object> exceptionOnes = new HashMap<>();
            exceptionOnes.put("statusCode", exceptionProperties.getStatus_code());
            exceptionOnes.put("type", exceptionProperties.getType());
            exceptionOnes.put("detail", exceptionProperties.getDetail());

            logContext.put("exception", exceptionOnes);
        }
        if (logLocationInfoProperty.logLocationInfo) {
            logContext.put("location", getLocationInformation(location));
        }
        /* 1.2.1 - Changed to MapMessage instead of ObjectMessage */
        MapMessage mapMessage = new MapMessage(logContext);
        logger.log(level, mapMessage);
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