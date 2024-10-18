package com.avioconsulting.mule.logger.internal.listeners;

import com.avioconsulting.mule.logger.api.processor.AdditionalProperties;
import com.avioconsulting.mule.logger.api.processor.ExceptionProperties;
import com.avioconsulting.mule.logger.api.processor.LogProperties;
import com.avioconsulting.mule.logger.api.processor.MessageAttributes;
import com.avioconsulting.mule.logger.internal.CustomLogger;
import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.event.Event;

public abstract class CustomLoggerAbstractNotificationListener {
  protected final CustomLoggerConfiguration config;

  public CustomLoggerAbstractNotificationListener(CustomLoggerConfiguration config) {
    this.config = config;
  }

  protected abstract org.slf4j.Logger getClassLogger();

  protected void logMessage(ComponentLocation location, Event event, String logMessage, String categoryPrefix,
      LogProperties.LogLevel level) {
    CustomLogger logger = config.getLogger();
    LogProperties logProperties = new LogProperties();
    MessageAttributes messageAttributes = new MessageAttributes();
    if (event.getVariables().get("OTEL_TRACE_CONTEXT") != null) {
      Object oTelContextObject = event.getVariables().get("OTEL_TRACE_CONTEXT")
          .getValue();
      messageAttributes.setOTelContextObject(oTelContextObject);
    }
    ExceptionProperties exceptionProperties = new ExceptionProperties();
    AdditionalProperties additionalProperties = new AdditionalProperties();
    additionalProperties.setIncludeLocationInfo(true);
    String correlationId = event.getCorrelationId();
    if (categoryPrefix != null &&
        !categoryPrefix.isEmpty()) {
      logProperties.setCategorySuffix(categoryPrefix);
    }
    logProperties.setLevel(level);
    logProperties.setMessage(logMessage);
    logger.log(logProperties, messageAttributes, exceptionProperties, additionalProperties, config,
        location, correlationId);
  }
}
