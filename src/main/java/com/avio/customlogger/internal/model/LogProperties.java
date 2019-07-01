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
    private String payload;

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

    public LoggerLevelProperty.LogLevel getLog_level() {
        return level;
    }

    public String getCategory() {
        return category;
    }

    public String getCorrelation_id() {
        return correlation_id;
    }

    public String getPayload() {
        return payload;
    }

    public tracePointProperty.tracePoint getTracePoint() {
        return tracePoint;
    }

}