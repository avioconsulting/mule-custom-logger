package com.lamar.customlogger.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.lamar.customlogger.internal.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;

import java.util.HashMap;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

/**
 * This class is a container for operations, every public method in this class
 * will be taken as an extension operation. Author: Chakri Bonthala
 */
public class CustomLoggerOperation {

    protected Log logger;

    /**
     * Author: Chakri Bonthala Example of an operation that uses the configuration
     * and a connection instance to perform some action.
     *
     * @return
     */
    @MediaType(value = ANY, strict = false)
    public void customLogger(@ParameterGroup(name = "Standard") StandardProperties standardProperties,
                             @ParameterGroup(name = "Log") LogProperties logProperties,
                             @ParameterGroup(name = "Extended") ExtendedProperties extendedProperties,
                             @ParameterGroup(name = "Exception") ExceptionProperties exceptionProperties) {

        initLogger(logProperties.getCategory());

        try {
            Gson gson = new GsonBuilder().create();
            String extJsonString = extendedProperties.getProperties() == null ? gson.toJson(extendedProperties) : gson.toJson(extendedProperties.getProperties());
            String standardJsonString = gson.toJson(standardProperties);
            String exceptionJsonString = gson.toJson(exceptionProperties);
            String logJsonString = gson.toJson(logProperties);

            HashMap<String, Object> logContent = new Gson().fromJson(standardJsonString, HashMap.class);

            logContent.put("log", new JsonParser().parse(logJsonString).getAsJsonObject());
            if (!extJsonString.equals("{}")) {
//                JsonObject extJson = new JsonParser().parse(extJsonString).getAsJsonObject();
//                for (Object jsonKey : extJson.keySet()) {
//                    logContent.put((String) jsonKey, extJson.get((String) jsonKey));
//                }
                logContent.put("ext", new JsonParser().parse(extJsonString).getAsJsonObject());
            }

            if (!exceptionJsonString.equals("{}")) {
//			JsonObject exceptionJson = new JsonParser().parse(exceptionJsonString).getAsJsonObject();
//			for (Object jsonKey : exceptionJson.keySet()) {
//		        logContent.put((String)jsonKey,exceptionJson.get((String)jsonKey));
//		    }
                logContent.put("exception", new JsonParser().parse(exceptionJsonString).getAsJsonObject());
            }

            logWithLevel(gson.toJson(logContent), logProperties.getLog_level().logLevel());
            return;
        } catch (Exception e) {
            logWithLevel("Failed to produce Json" + e.getMessage(), "ERROR");
        }
    }

    protected void initLogger(String category) {
        this.logger = LogFactory.getLog(category);
    }

    protected void logWithLevel(String logMessage, String logLevel) {

        if (LoggerLevelProperty.LogLevel.ERROR.logLevel().equals(logLevel)) {
            logger.error(logMessage);
        } else if (LoggerLevelProperty.LogLevel.WARN.logLevel().equals(logLevel)) {
            logger.warn(logMessage);
        } else if (LoggerLevelProperty.LogLevel.DEBUG.logLevel().equals(logLevel)) {
            logger.debug(logMessage);
        } else if (LoggerLevelProperty.LogLevel.TRACE.logLevel().equals(logLevel)) {
            logger.trace(logMessage);
        } else {
            logger.info(logMessage);
        }

    }
}
