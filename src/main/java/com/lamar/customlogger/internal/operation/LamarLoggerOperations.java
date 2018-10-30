package com.lamar.customlogger.internal.operation;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lamar.customlogger.internal.metadata.ExceptionDataPoints;
import com.lamar.customlogger.internal.metadata.ExtDataPoints;
import com.lamar.customlogger.internal.metadata.GenericDataPoints;
import com.lamar.customlogger.internal.metadata.LoggerLevel;

/**
 * This class is a container for operations, every public method in this class
 * will be taken as an extension operation. Author: Chakri Bonthala
 */
public class LamarLoggerOperations {

	protected Log logger;

	/**
	 * Author: Chakri Bonthala Example of an operation that uses the configuration
	 * and a connection instance to perform some action.
	 * 
	 * @return
	 */
	@MediaType(value = ANY, strict = false)
	public void customLogger(@ParameterGroup(name = "Standard Properties") GenericDataPoints genericDataPoints,
			@ParameterGroup(name = "Additional Properties") ExtDataPoints extDataPoints,
			@ParameterGroup(name = "Exception Properties") ExceptionDataPoints exceptionDataPoints) {

		initLogger(genericDataPoints.getLogCategory());

		try {
		Gson gson = new GsonBuilder().create();	
		String extJsonString = extDataPoints.getExt() == null ? gson.toJson(extDataPoints) : gson.toJson(extDataPoints.getExt());	
		String genericJsonString = gson.toJson(genericDataPoints);
		String exceptionJsonString = gson.toJson(exceptionDataPoints);

		HashMap<String, Object> logContent = new Gson().fromJson(genericJsonString, HashMap.class);
		
		
		if (!extJsonString.equals("{}")) {
			JsonObject extJson = new JsonParser().parse(extJsonString).getAsJsonObject();
			for (Object jsonKey : extJson.keySet()) {
		        logContent.put((String)jsonKey,extJson.get((String)jsonKey));
		    }
		}
		
		if (!exceptionJsonString.equals("{}")) {
			JsonObject exceptionJson = new JsonParser().parse(exceptionJsonString).getAsJsonObject();
			for (Object jsonKey : exceptionJson.keySet()) {
		        logContent.put((String)jsonKey,exceptionJson.get((String)jsonKey));
		    }
		}

		logWithLevel(gson.toJson(logContent), genericDataPoints.getLogLevel().logLevel());
		return;
		} catch (Exception e) {
			logWithLevel("Failed to produce Json"+e.getMessage(),"ERROR");
		}
	}

	protected void initLogger(String category) {
		this.logger = LogFactory.getLog(category);
	}

	protected void logWithLevel(String logMessage, String logLevel) {

		if (LoggerLevel.LogLevel.ERROR.logLevel().equals(logLevel)) {
			logger.error(logMessage);
		} else if (LoggerLevel.LogLevel.WARN.logLevel().equals(logLevel)) {
			logger.warn(logMessage);
		} else if (LoggerLevel.LogLevel.DEBUG.logLevel().equals(logLevel)) {
			logger.debug(logMessage);
		} else if (LoggerLevel.LogLevel.TRACE.logLevel().equals(logLevel)) {
			logger.trace(logMessage);
		} else {
			logger.info(logMessage);
		}

	}
}
