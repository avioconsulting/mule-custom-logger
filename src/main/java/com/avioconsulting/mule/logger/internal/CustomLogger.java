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

import com.avioconsulting.mule.logger.internal.utils.LogPropertiesMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.component.location.Location;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;
import org.slf4j.LoggerFactory;

public class CustomLogger {

  public static final String TIMESTAMP = "timestamp";
  public static final String APP_NAME = "appName";
  public static final String APP_NAME_V1 = "app_name";
  public static final String APP_VERSION = "appVersion";
  public static final String APP_VERSION_V1 = "app_version";
  public static final String ENV = "env";
  public static final String CORRELATION_ID = "correlationId";
  public static final String CORRELATION_ID_V1 = "correlation_id";
  public static final String MESSAGE = "message";
  public static final String MESSAGE_ATTRIBUTES = "messageAttributes";
  public static final String PAYLOAD = "payload";
  public static final String LOCATION = "location";
  public static final String STATUS_CODE = "statusCode";
  public static final String DETAIL = "detail";
  public static final String EXCEPTION = "exception";
  public static final String COMPONENT = "component";
  public static final String ROOT_CONTAINER = "rootContainer";
  public static final String TYPE = "type";
  public static final String EXT = "ext";
  public static final String LOG = "log";
  public static final String AVIO_LOGGER_USE_MAP_MESSAGE = "avio.logger.useMapMessage";
  public static final String FILE_NAME = "fileName";
  public static final String LINE_IN_FILE = "lineInFile";
  // TODO: Create Logger Cache

    public void log(LogProperties logProperties, MessageAttributes messageAttributes, ExceptionProperties exceptionProperties,
      AdditionalProperties additionalProperties, CustomLoggerConfiguration loggerConfig, ComponentLocation location,
      String correlationId) {
    writeInLog(logProperties, messageAttributes, exceptionProperties, additionalProperties, location, correlationId,
        loggerConfig.getApplicationName(), loggerConfig.getApplicationVersion(), loggerConfig.getEnvironment(),
        loggerConfig.getDefaultCategory(), loggerConfig.isEnableV1Compatibility());
  }

  public void writeInLog(LogProperties logProperties, MessageAttributes messageAttributes, ExceptionProperties exceptionProperties,
      AdditionalProperties additionalProperties, ComponentLocation location, String correlationId, String applicationName,
      String applicationVersion, String environment, String defaultCategory, boolean isV1compatibilityEnabled) {

    Logger logger = CustomLoggerUtils.initLogger(defaultCategory, logProperties.getCategory(), logProperties.getCategorySuffix());
    Level level = LogPropertiesMap.levelMap.get(logProperties.getLevel());

    if (isV1compatibilityEnabled) {
      buildLogContextV1(logProperties, messageAttributes, exceptionProperties, additionalProperties, location, correlationId,
          applicationName, applicationVersion,
          environment, logger, level);
    } else {
      buildLogContext(applicationName, applicationVersion, environment, correlationId,
             logProperties, messageAttributes, logger, level, exceptionProperties ,additionalProperties, location);
    }
  }

  public void buildLogContextV1(LogProperties logProperties, MessageAttributes messageAttributes, ExceptionProperties exceptionProperties,
          AdditionalProperties additionalProperties, ComponentLocation location, String correlationId, String applicationName,
          String applicationVersion, String environment, Logger logger, Level level) {

    Map<String, Object> logContext = new HashMap<>();
    logContext.put(APP_NAME_V1, applicationName);
    logContext.put(APP_VERSION_V1, applicationVersion);
    logContext.put(ENV, environment);
    logContext.put(TIMESTAMP, Instant.now().toString());
    logContext.put(EXT, messageAttributes.getAttributesAsMapCopy());
    loggerContextSetInnerLog(logContext,logProperties,correlationId);
    loggerContextSetPayload(logContext,logProperties,logger,level);
    loggerContextSetException(logContext,exceptionProperties);
    loggerContextSetLocationInformation(logContext,location,additionalProperties);
    logMessage(logger,logContext,level);
  }

  private void buildLogContext(String applicationName, String applicationVersion,
          String environment, String correlationId, LogProperties logProperties, MessageAttributes messageAttributes,
          Logger logger, Level level, ExceptionProperties exceptionProperties, AdditionalProperties additionalProperties,
          ComponentLocation location){

    Map<String, Object> logContext = new LinkedHashMap<>();
    logContext.put(TIMESTAMP, Instant.now().toString());
    logContext.put(APP_NAME, applicationName);
    logContext.put(APP_VERSION, applicationVersion);
    logContext.put(ENV, environment);
    logContext.put(CORRELATION_ID, resolveCorrelationId(logProperties,correlationId));
    logContext.put(MESSAGE, logProperties.getMessage());
    logContext.put(MESSAGE_ATTRIBUTES, messageAttributes.getAttributesAsMapCopy());
    loggerContextSetPayload(logContext,logProperties,logger,level);
    loggerContextSetException(logContext,exceptionProperties);
    loggerContextSetLocationInformation(logContext,location,additionalProperties);
    logMessage(logger,logContext,level);
  }


  private String resolveCorrelationId(LogProperties logProperties, String correlationId) {
    return StringUtils.isNotBlank(logProperties.getCorrelationId()) ? logProperties.getCorrelationId()
            : correlationId;
  }

  private void loggerContextSetPayload(Map<String, Object> logContext, LogProperties logProperties,
    Logger logger, Level level) {

    ParameterResolver<String> payload = logProperties.getPayload();
    if (logger.isEnabled(level) && payload != null) {
      logContext.put(PAYLOAD, payload.resolve());
    }
  }

  private void loggerContextSetException(Map<String, Object> logContext,
          ExceptionProperties exceptionProperties) {
    if (exceptionProperties != null) {
      Map<String, Object> exceptionOnes = new HashMap<>();
      exceptionOnes.put(STATUS_CODE, exceptionProperties.getStatusCode());
      if(StringUtils.isNotBlank(exceptionProperties.getType())) {
        exceptionOnes.put(TYPE, exceptionProperties.getType());
      }
      exceptionOnes.put(DETAIL, exceptionProperties.getDetail());
      logContext.put(EXCEPTION, exceptionOnes);
    }
  }

  private void loggerContextSetLocationInformation(Map<String, Object> logContext,
          ComponentLocation location, AdditionalProperties additionalProperties) {
    if (additionalProperties.isIncludeLocationInfo()) {
      logContext.put(LOCATION, getLocationInformation(location));
    }
  }

  public static Map<String, String> getLocationInformation(ComponentLocation location) {
    Map<String, String> locationInfo = new LinkedHashMap<>();
    locationInfo.put(COMPONENT, location.getComponentIdentifier().getIdentifier().toString());
    locationInfo.put(ROOT_CONTAINER, location.getRootContainerName());
    locationInfo.put(LOCATION, location.getLocation());
    locationInfo.put(FILE_NAME, location.getFileName().orElse(StringUtils.EMPTY));
    locationInfo.put(LINE_IN_FILE, String.valueOf(location.getLineInFile().orElse(null)));
    return locationInfo;
  }

  private void loggerContextSetInnerLog(Map<String, Object> logContext, LogProperties logProperties, String correlationId){
    Map<String, Object> logInner = new HashMap<>();
    logInner.put(CORRELATION_ID_V1, resolveCorrelationId(logProperties,correlationId));
    logInner.put(MESSAGE, logProperties.getMessage());
    logContext.put(LOG, logInner);
  }

  private void logMessage(Logger logger, Map<String, Object> logContext, Level level) {
    /* 1.2.1 - Changed to MapMessage instead of ObjectMessage */
    /* Check system property - avio.custom.logger.env */
    /* this lets us differentiate to use simplified logging locally */
    /*
     * Check system property avio.logger.useMapMessage to turn on MapMessage usage
     * in studio.
     * This allows pattern layout to display specific attributes of the MapMessage
     */
    Message message;
    if ("true".equalsIgnoreCase(System.getProperty(AVIO_LOGGER_USE_MAP_MESSAGE))) {
      message = new MapMessage<>(logContext);
    } else {
      message = new ObjectMessage(logContext);
    }
    logger.log(level, message);
  }

}
