package com.avio.customlogger.internal.model;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class LogProperties {


    @Parameter
    @Optional
    @DisplayName("Correlation ID")
    @Summary("Correlation UUID")
    @Example("#[vars.correlationId]")
    private String correlation_id;

    @Parameter
    @Optional
    @DisplayName("Message")
    @Summary("Message to be logged")
    private String message;

    @Parameter
    @Optional
    @DisplayName("Payload")
    @Summary("Payload to be logged")
    @Example("#[write(payload, \"application/json\")]")
    String payload;

    @Parameter
    @DisplayName("Level")
    @Optional(defaultValue = "INFO")
    private LoggerLevelProperty.LogLevel level;

    @Parameter
    @DisplayName("Trace Point")
    @Optional(defaultValue = "FLOW")
    private tracePointProperty.tracePoint tracePoint;

    @Parameter
    @Optional
    @DisplayName("Category")
    private String category;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LoggerLevelProperty.LogLevel getLog_level() {
        return level;
    }

    public void setLog_level(LoggerLevelProperty.LogLevel log_level) {
        this.level = level;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCorrelation_id() {
        return correlation_id;
    }

    public void setCorrelation_id(String correlation_id) {
        this.correlation_id = correlation_id;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public tracePointProperty.tracePoint getTracePoint() {
        return tracePoint;
    }

    public void setTracePoint(tracePointProperty.tracePoint tracePoint) {
        this.tracePoint = tracePoint;
    }
}