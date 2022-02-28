package com.avioconsulting.mule.logger.internal;

import com.avioconsulting.mule.logger.api.processor.AdditionalProperties;
import com.avioconsulting.mule.logger.api.processor.ExceptionProperties;
import com.avioconsulting.mule.logger.api.processor.LogProperties;
import com.avioconsulting.mule.logger.api.processor.MessageAttributes;
import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import com.avioconsulting.mule.logger.internal.utils.CustomLoggerUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.runtime.parameter.CorrelationInfo;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

/**
 * Author: Chakri Bonthala
 */
public class CustomLoggerOperation {

    @MediaType(value = ANY, strict = false)
    public void log(@ParameterGroup(name = "Log") LogProperties logProperties,
                    @ParameterGroup(name = "Message Attributes") MessageAttributes messageAttributes,
                    @ParameterGroup(name = "Exception Details") ExceptionProperties exceptionProperties,
                    @ParameterGroup(name = "Additional Options") AdditionalProperties additionalProperties,
                    @Config CustomLoggerConfiguration loggerConfig,
                    ComponentLocation location,
                    CorrelationInfo correlationInfo) {
        loggerConfig.getLogger().log(logProperties, messageAttributes, exceptionProperties, additionalProperties, loggerConfig, location, correlationInfo.getCorrelationId());
    }
}