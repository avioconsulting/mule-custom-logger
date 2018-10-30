package com.lamar.customlogger.internal.metadata;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;



public class GenericDataPoints {
	
	@Parameter
	@DisplayName("Application Name")
	@Summary("Name of the Mule Application")
	@Example("#[app.name]")
	String applicationName;

	
	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	
	@Parameter
	@DisplayName("Application Version")
	@Summary("Version of the Mule Application")
	@Example("${pomVersion}")
	String applicationVersion;

	
	public String getApplicationVersion() {
		return applicationVersion;
	}

	public void setApplicationVersion(String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}
	
	@Parameter
	@DisplayName("Environment")
	@Summary("Mule Application Environment")
	@Example("${env}")
	String environment;

	
	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	
	@Parameter
	@DisplayName("Time Stamp")
	@Summary("Time Stamp")
	@Example("#[now()]")
	String  timestamp;

	
	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	@Parameter
	@DisplayName("Transaction ID")
	@Summary("Transaction UUID")
	@Example("#[vars.transactionId]")
	String  transactionId;

	
	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	
	@Parameter
	@Optional
	@DisplayName("Message")
	@Summary("Message to be logged")
	String  message;

	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Parameter
	@Optional
	@DisplayName("Payload")
	@Summary("Payload to be logged")
	@Example("#[payload]")
	String thePayload;

	
	public String getThePayload() {
		return thePayload;
	}

	public void setThePayload(String thePayload) {
		this.thePayload = thePayload;
	}
	
	@Parameter
	@DisplayName("Level")
	@Optional(defaultValue="INFO")
	private LoggerLevel.LogLevel logLevel;
	
	
	public LoggerLevel.LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LoggerLevel.LogLevel logLevel) {
		this.logLevel = logLevel;
	}
	
	@Parameter
	@Optional(defaultValue = "#['com.lamar.' ++ p('app.name')]")
	@DisplayName("Category")
	String logCategory;
	
	public String getLogCategory() {
		return logCategory;
	}

	public void setLogCategory(String logCategory) {
		this.logCategory = logCategory;
	}
}
