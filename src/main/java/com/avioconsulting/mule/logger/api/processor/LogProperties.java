package com.avioconsulting.mule.logger.api.processor;

import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LogProperties {


    @Parameter
    @Optional
    @DisplayName("Correlation ID")
    @Summary("Correlation UUID. Defaults to current event's correlation id.")
    @Example("#[correlationId]")
    private String correlationId;

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
    private ParameterResolver<String> payload;

    @Parameter
    @DisplayName("Level")
    @Optional(defaultValue = "INFO")
    private LogLevel level;

    @Parameter
    @Optional
    @DisplayName("Category")
    private String category;

    @Parameter
    @DisplayName("Message Attributes")
    @Optional
    @NullSafe
    @Summary("Discrete data elements you want to log as key value pairs.  Useful for adding data fields for reporting in log aggregation tools")
    private List<MessageAttribute> messageAttributes;

    public enum LogLevel {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL
    }

    public String getMessage() {
        return message;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getCategory() {
        return category;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public ParameterResolver<String> getPayload() {
        return payload;
    }

    public Map<String,String> getMessageAttributes() {
        Map<String,String> attributes = new LinkedHashMap<>();
        for(MessageAttribute a : messageAttributes) {
            attributes.put(a.getKey(), a.getValue());
        }
        return attributes;
    }
}