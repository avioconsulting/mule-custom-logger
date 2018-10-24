package com.lamar.customlogger.internal.operation;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.lamar.customlogger.internal.metadata.ExceptionDataPoints;
import com.lamar.customlogger.internal.metadata.ExtDataPoints;
import com.lamar.customlogger.internal.metadata.GenericDataPoints;
import com.lamar.customlogger.internal.metadata.LoggerLevel;

/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 * Author: Chakri Bonthala
 */
public class LamarLoggerOperations {

	protected Log logger;

	/**
	 * Author: Chakri Bonthala
	 * Example of an operation that uses the configuration and a connection instance
	 * to perform some action.
	 * 
	 * @return
	 */
	@MediaType(value = ANY, strict = false)
	public void customLogger(@ParameterGroup(name = "Generic") GenericDataPoints genericDataPoints,
			@ParameterGroup(name = "Extra Parameters") ExtDataPoints extDataPoints,
			@ParameterGroup(name = "Exception") ExceptionDataPoints exceptionDataPoints) {

		try {
		initLogger(genericDataPoints.getLogCategory());

		Gson gson = new GsonBuilder().create();
		
		HashMap<String, Object> logContent = new Gson().fromJson(gson.toJson(genericDataPoints), HashMap.class);
		
		logContent.put("ext", new JsonParser().parse(gson.toJson(extDataPoints.getExt())).getAsJsonObject());
		logContent.put("exception", new JsonParser().parse(gson.toJson(exceptionDataPoints)).getAsJsonObject());
		
		logWithLevel(gson.toJson(logContent), genericDataPoints.getLogLevel().logLevel());
		return;
		} catch (Exception e) {
			logWithLevel("Failed to create JSON Log message","ERROR");
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
