package com.avioconsulting.mule.logger.internal;

import com.avioconsulting.mule.logger.api.processor.AdditionalProperties;
import com.avioconsulting.mule.logger.api.processor.ExceptionProperties;
import com.avioconsulting.mule.logger.api.processor.LogProperties;
import com.avioconsulting.mule.logger.api.processor.MessageAttributes;
import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import com.avioconsulting.mule.logger.internal.utils.CustomLoggerUtils;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;
import org.slf4j.LoggerFactory;

public class CustomLogger {

  private static final org.slf4j.Logger classLogger = LoggerFactory.getLogger(CustomLogger.class);
  // TODO: Create Logger Cache

  private static final Map<LogProperties.LogLevel, Level> levelMap = new HashMap<LogProperties.LogLevel, Level>() {
    {
      put(LogProperties.LogLevel.INFO, Level.INFO);
      put(LogProperties.LogLevel.DEBUG, Level.DEBUG);
      put(LogProperties.LogLevel.TRACE, Level.TRACE);
      put(LogProperties.LogLevel.ERROR, Level.ERROR);
      put(LogProperties.LogLevel.WARN, Level.WARN);
      put(LogProperties.LogLevel.FATAL, Level.FATAL);
    }
  };

  public void log(LogProperties logProperties,
      MessageAttributes messageAttributes,
      ExceptionProperties exceptionProperties,
      AdditionalProperties additionalProperties,
      CustomLoggerConfiguration loggerConfig,
      ComponentLocation location,
      String correlationId) {
    log(logProperties, messageAttributes,
        exceptionProperties, additionalProperties,
        location, correlationId,
        loggerConfig.getApplicationName(),
        loggerConfig.getApplicationVersion(),
        loggerConfig.getEnvironment(),
        loggerConfig.getDefaultCategory(),
        loggerConfig.isEnableV1Compatibility());
  }

  public void log(LogProperties logProperties,
      MessageAttributes messageAttributes,
      ExceptionProperties exceptionProperties,
      AdditionalProperties additionalProperties,
      ComponentLocation location,
      String correlationId,
      String applicationName,
      String applicationVersion,
      String environment,
      String defaultCategory,
      boolean enableV1Compatibility) {
    if (enableV1Compatibility) {
      logV1(logProperties, messageAttributes,
          exceptionProperties, additionalProperties,
          location, correlationId,
          applicationName, applicationVersion,
          environment, defaultCategory);
    } else {
      Logger logger = CustomLoggerUtils.initLogger(defaultCategory, logProperties.getCategory(),
          logProperties.getCategorySuffix());
      Level level = levelMap.get(logProperties.getLevel());
      String correlation = logProperties.getCorrelationId() != null ? logProperties.getCorrelationId()
          : correlationId;

      Map<String, Object> logContext = new LinkedHashMap<>();
      logContext.put("timestamp", Instant.now().toString());
      logContext.put("appName", applicationName);
      logContext.put("appVersion", applicationVersion);
      logContext.put("env", environment);
      logContext.put("correlationId", correlation);
      logContext.put("message", logProperties.getMessage());
      logContext.put("messageAttributes", messageAttributes.getAttributes());

      /*
       * Evaluate payload only when we actually need it
       * Conditionally set payload to log, if encrypted or compressed,
       * base64 encode string being logged
       */
      ParameterResolver<String> payload = logProperties.getPayload();
      if (logger.isEnabled(level) && payload != null) {
        String payloadToLog;
        String encryptedString = logProperties.getEncryptedPayload();
        String compressedString = logProperties.getCompressedPayload();
        String payloadString = payload.resolve();
        if (encryptedString != null) {
          payloadToLog = encryptedString;
        } else if (compressedString != null) {
          payloadToLog = compressedString;
        } else {
          payloadToLog = payloadString;
        }
        logContext.put("payload", payloadToLog);
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

      /*
       * Check system property avio.logger.useMapMessage to turn on MapMessage usage
       * in studio.
       * This allows pattern layout to display specific attributes of the MapMessage
       */
      Message message;
      if ("true".equalsIgnoreCase(System.getProperty("avio.logger.useMapMessage"))) {
        message = new MapMessage<>(logContext);
      } else {
        message = new ObjectMessage(logContext);
      }

      logger.log(level, message);
    }
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
      String defaultCategory) {

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

    /* 1.2.1 - Changed to MapMessage instead of ObjectMessage */
    /* Check system property - avio.custom.logger.env */
    /* this lets us differentiate to use simplified logging locally */
    Message message;
    if ("true".equalsIgnoreCase(System.getProperty("avio.logger.useMapMessage"))) {
      message = new MapMessage<>(logContext);
    } else {
      message = new ObjectMessage(logContext);
    }
    logger.log(level, message);
  }

}
