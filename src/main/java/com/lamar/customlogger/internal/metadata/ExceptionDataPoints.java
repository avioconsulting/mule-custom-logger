package com.lamar.customlogger.internal.metadata;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;



public class ExceptionDataPoints {
	
	@Parameter
	@DisplayName("Status Code")
	@Summary("Exception Status Code")
	@Optional
	String statusCode;

	
	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	
	@Parameter
	@DisplayName("Type")
	@Summary("Type of Exception")
	@Optional
	String exceptionType;

	
	public String getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(String exceptionType) {
		this.exceptionType = exceptionType;
	}

	@Parameter
	@DisplayName("Description")
	@Summary("Exception Description")
	@Optional
	String exceptionDescription;

	
	public String getExceptionDescription() {
		return exceptionDescription;
	}

	public void setExceptionDescription(String exceptionDescription) {
		this.exceptionDescription = exceptionDescription;
	}
}
