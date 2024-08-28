package com.avioconsulting.mule.logger.internal;

import com.avioconsulting.mule.logger.api.processor.AdditionalProperties;
import com.avioconsulting.mule.logger.api.processor.ExceptionProperties;
import com.avioconsulting.mule.logger.api.processor.LogProperties;
import com.avioconsulting.mule.logger.api.processor.MessageAttribute;
import com.avioconsulting.mule.logger.api.processor.MessageAttributes;
import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import com.avioconsulting.mule.logger.internal.utils.CustomLoggerUtils;
import com.avioconsulting.mule.logger.internal.utils.PayloadTransformer;
import com.google.gson.Gson;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.slf4j.LoggerFactory;

public class CustomLogger {

  private static final org.slf4j.Logger classLogger = LoggerFactory.getLogger(CustomLogger.class);
  // TODO: Create Logger Cache

  private static final Map<LogProperties.LogLevel, Level> levelMap = new HashMap<LogProperties.LogLevel, Level>();

  static {
    levelMap.put(LogProperties.LogLevel.FATAL, Level.FATAL);
    levelMap.put(LogProperties.LogLevel.WARN, Level.WARN);
    levelMap.put(LogProperties.LogLevel.ERROR, Level.ERROR);
    levelMap.put(LogProperties.LogLevel.TRACE, Level.TRACE);
    levelMap.put(LogProperties.LogLevel.DEBUG, Level.DEBUG);
    levelMap.put(LogProperties.LogLevel.INFO, Level.INFO);
  }

  // Tests can mock this with this visibility
  public PayloadTransformer payloadTransformer = new PayloadTransformer();

  /**
   * Formats dates in ISO 8601 format with milliseconds and UTC timezone.
   */
  private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"));

  public void log(LogProperties logProperties,
      MessageAttributes messageAttributes,
      ExceptionProperties exceptionProperties,
      AdditionalProperties additionalProperties,
      CustomLoggerConfiguration loggerConfig,
      ComponentLocation location,
      String correlationId) {
    log(logProperties, messageAttributes, exceptionProperties, additionalProperties, loggerConfig, location,
        correlationId, null);
  }

  public void log(LogProperties logProperties,
      MessageAttributes messageAttributes,
      ExceptionProperties exceptionProperties,
      AdditionalProperties additionalProperties,
      CustomLoggerConfiguration loggerConfig,
      ComponentLocation location,
      String correlationId, StreamingHelper streamingHelper) {
    log(logProperties, messageAttributes,
        exceptionProperties, additionalProperties,
        location, correlationId,
        loggerConfig,
        streamingHelper);
  }

  public void log(LogProperties logProperties,
      MessageAttributes messageAttributes,
      ExceptionProperties exceptionProperties,
      AdditionalProperties additionalProperties,
      ComponentLocation location,
      String correlationId,
      CustomLoggerConfiguration loggerConfig,
      StreamingHelper streamingHelper) {

    String applicationName = loggerConfig.getApplicationName();
    String applicationVersion = loggerConfig.getApplicationVersion();
    String environment = loggerConfig.getEnvironment();
    String defaultCategory = loggerConfig.getDefaultCategory();
    boolean enableV1Compatibility = loggerConfig.isEnableV1Compatibility();
    boolean formatAsJson = loggerConfig.isFormatAsJson();

    if (enableV1Compatibility) {
      logV1(logProperties, messageAttributes,
          exceptionProperties, additionalProperties,
          location, correlationId,
          applicationName, applicationVersion,
          environment, defaultCategory, formatAsJson);
    } else {
      Logger logger = CustomLoggerUtils.initLogger(defaultCategory, logProperties.getCategory(),
          logProperties.getCategorySuffix());
      Level level = levelMap.get(logProperties.getLevel());
      String correlation = logProperties.getCorrelationId() != null ? logProperties.getCorrelationId()
          : correlationId;
      Object oTelContext = messageAttributes.getOTelContextObject();

      Map<String, Object> logContext = new LinkedHashMap<>();
      logContext.put("timestamp", dateTimeFormatter.format(Instant.now()));
      logContext.put("appName", applicationName);
      logContext.put("appVersion", applicationVersion);
      logContext.put("env", environment);
      logContext.put("correlationId", correlation);
      logContext.put("message", logProperties.getMessage());

      /*
       * If oTelContext is not null, then add message attributes for traceId and
       * spanId from the context
       */
      if (oTelContext != null) {
        Map<String, String> oTelContextMap = (Map<String, String>) oTelContext;
        MessageAttribute traceId = new MessageAttribute("traceId",
            oTelContextMap.get("traceId"));
        messageAttributes.getAttributeList().add(traceId);
        MessageAttribute traceIdLongLowPart = new MessageAttribute("traceIdLongLowPart",
            oTelContextMap.get("traceIdLongLowPart"));
        messageAttributes.getAttributeList().add(traceIdLongLowPart);
        MessageAttribute spanId = new MessageAttribute("spanId",
            oTelContextMap.get("spanId"));
        messageAttributes.getAttributeList().add(spanId);
        MessageAttribute spanIdLong = new MessageAttribute("spanIdLong",
            oTelContextMap.get("spanIdLong"));
        messageAttributes.getAttributeList().add(spanIdLong);
      }
      logContext.put("messageAttributes", messageAttributes.getAttributes());

      /*
       * Evaluate payload only when we actually need it
       * Conditionally set payload to log, if encrypted or compressed,
       * base64 encode string being logged
       */
      ParameterResolver<String> payload = logProperties.getPayload();
      if (logger.isEnabled(level) && payload != null) {
        String payloadString = payload.resolve();
        if (streamingHelper != null) {
          payloadString = payloadTransformer.transformPayload(loggerConfig, streamingHelper, payloadString);
        }
        logContext.put("payload", payloadString);
      }

      if (exceptionProperties != null) {
        Map<String, Object> exceptionMap = new LinkedHashMap<>();
        exceptionMap.put("statusCode", exceptionProperties.getStatusCode());
        exceptionMap.put("detail", exceptionProperties.getDetail());
        logContext.put("exception", exceptionMap);
      }

      if (additionalProperties.isIncludeLocationInfo()) {
        logContext.put("location", getLocationInformation(location));
      }

      writeLog(logContext, logger, level, formatAsJson);
    }
  }

  private void writeLog(Map<String, Object> logContext, Logger logger, Level level, boolean formatAsJson) {
    /*
     * Check system property avio.logger.useMapMessage to turn on MapMessage usage
     * in studio.
     * This allows pattern layout to display specific attributes of the MapMessage
     */
    Message message;
    if ("true".equalsIgnoreCase(System.getProperty("avio.logger.useMapMessage"))) {
      message = new MapMessage<>(logContext);
    } else if (formatAsJson) {
      Gson gson = new Gson();
      String json = gson.toJson(logContext);
      message = new SimpleMessage(json);
    } else {
      message = new ObjectMessage(logContext);
    }
    logger.log(level, message);
  }

  public static Map<String, String> getLocationInformation(ComponentLocation location) {
    Map<String, String> locationInfo = new LinkedHashMap<>();
    locationInfo.put("component", location.getComponentIdentifier().getIdentifier().toString());
    locationInfo.put("rootContainer", location.getRootContainerName());
    locationInfo.put("location", location.getLocation());
    locationInfo.put("fileName", location.getFileName().orElse(""));
    locationInfo.put("lineInFile", String.valueOf(location.getLineInFile().orElse(null)));
    return locationInfo;
  }

  public void logV1(LogProperties logProperties,
      MessageAttributes messageAttributes,
      ExceptionProperties exceptionProperties,
      AdditionalProperties additionalProperties,
      ComponentLocation location,
      String correlationId,
      String applicationName,
      String applicationVersion,
      String environment,
      String defaultCategory,
      boolean formatAsJson) {

    Logger logger = CustomLoggerUtils.initLogger(defaultCategory, logProperties.getCategory(),
        logProperties.getCategorySuffix());

    Map<String, Object> logContext = new HashMap<>();
    logContext.put("app_name", applicationName);
    logContext.put("app_version", applicationVersion);
    logContext.put("env", environment);
    logContext.put("timestamp", Instant.now().toString());
    logContext.put("ext", messageAttributes.getAttributes());

    Map<String, Object> logInner = new HashMap<>();
    String cId = logProperties.getCorrelationId() != null ? logProperties.getCorrelationId() : correlationId;
    logInner.put("correlation_id", cId);
    logInner.put("message", logProperties.getMessage());
    logContext.put("log", logInner);

    Level level = levelMap.get(logProperties.getLevel());

    ParameterResolver<String> payload = logProperties.getPayload();
    if (logger.isEnabled(level) && payload != null) {
      logInner.put("payload", payload.resolve());
    }

    if (exceptionProperties != null) {
      Map<String, Object> exceptionOnes = new HashMap<>();
      exceptionOnes.put("statusCode", exceptionProperties.getStatusCode());
      exceptionOnes.put("type", exceptionProperties.getType());
      exceptionOnes.put("detail", exceptionProperties.getDetail());

      logContext.put("exception", exceptionOnes);
    }

    if (additionalProperties.isIncludeLocationInfo()) {
      logContext.put("location", getLocationInformation(location));
    }

    writeLog(logContext, logger, level, formatAsJson);
  }

}
