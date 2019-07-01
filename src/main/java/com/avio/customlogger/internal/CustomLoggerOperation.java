package com.avio.customlogger.internal;

import com.avio.customlogger.internal.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private Log logger;

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
        else initLogger("com.avioconsulting.api");


        HashMap<String, Object> logContent = new HashMap<String, Object>();
        logContent.put("app_name", customLoggerConfiguration.getApp_name());
        logContent.put("app_version", customLoggerConfiguration.getApp_version());
        logContent.put("env", customLoggerConfiguration.getEnv());
        logContent.put("timestamp", Instant.now().toString());
        logContent.put("log", logProperties);
        logContent.put("ext", extendedProperties.getProperties());

        //This is because, we need to see what is in the 'exceptionProperties' when the Hashmap is logged.
        if (exceptionProperties != null) {
            Map<String, Object> exceptionOnes = new HashMap<String, Object>();
            exceptionOnes.put("statusCode", exceptionProperties.getStatus_code());
            exceptionOnes.put("type", exceptionProperties.getType());
            exceptionOnes.put("detail", exceptionProperties.getDetail());
            logContent.put("exception", exceptionOnes);
        }

        if (logLocationInfoProperty.logLocationInfo == true) {
            Map<String, String> locationInfo = new HashMap<String, String>();
            locationInfo.put("location", location.getLocation());
            locationInfo.put("root_container", location.getRootContainerName());
            locationInfo.put("component", location.getComponentIdentifier().getIdentifier().toString());
            locationInfo.put("file_name", location.getFileName().orElse(""));
            locationInfo.put("line_in_file", String.valueOf(location.getLineInFile().orElse(null)));

            logContent.put("location", locationInfo);
        }
        ObjectMessage objectMessage = new ObjectMessage(logContent);
        logWithLevel(objectMessage, logProperties.getLog_level().logLevel());
    }

    private void initLogger(String category) {
        this.logger = LogFactory.getLog(category);
    }

    protected void logWithLevel(ObjectMessage logMessage, String logLevel) {

        if (LoggerLevelProperty.LogLevel.ERROR.logLevel().equals(logLevel)) {
            logger.error(logMessage);
        } else if (LoggerLevelProperty.LogLevel.WARN.logLevel().equals(logLevel)) {
            logger.warn(logMessage);
        } else if (LoggerLevelProperty.LogLevel.DEBUG.logLevel().equals(logLevel)) {
            logger.debug(logMessage);
        } else if (LoggerLevelProperty.LogLevel.TRACE.logLevel().equals(logLevel)) {
            logger.trace(logMessage);
        } else if (LoggerLevelProperty.LogLevel.FATAL.logLevel().equals(logLevel)) {
            logger.fatal(logMessage);
        } else {
            logger.info(logMessage);
        }

    }
}