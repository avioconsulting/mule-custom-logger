package com.avioconsulting.mule.logger.internal;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import com.avioconsulting.mule.logger.api.processor.*;
import com.avioconsulting.mule.logger.api.processor.facade.PropertiesFactory;
import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.runtime.parameter.CorrelationInfo;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.route.Chain;
import org.slf4j.LoggerFactory;

public class CustomLoggerTimerScopeOperations {
  public static final String DEFAULT_CATEGORY_SUFFIX = "timer";
    public static final String ELAPSED_TIME_MS = "elapsedTimeMs";
    public static final String TIMER_NAME = "timerName";
    private final org.slf4j.Logger classLogger = LoggerFactory.getLogger(CustomLoggerTimerScopeOperations.class);

  @Inject
  CustomLoggerRegistrationService customLoggerRegistrationService;

  public CustomLoggerTimerScopeOperations() {
      classLogger.debug("Initializing CustomLoggerTimerScopeOperations...");
  }

  @MediaType(value = ANY, strict = false)
  @DisplayName(value = "Logging Timer Scope")
  public void timerScope(@DisplayName(value = "Timer Name") String timerName,
      @ParameterGroup(name = "Log") LogProperties logProperties,
      @ParameterGroup(name = "Options") AdditionalProperties additionalProperties,
      ComponentLocation location,
      CorrelationInfo correlationInfo,
      Chain operations,
      CompletionCallback<Object, Object> callback) {
    CustomLoggerConfiguration config = customLoggerRegistrationService.getConfig();

    if(config != null){
        CustomLogger logger = config.getCustomLogger();
        String correlationId = correlationInfo.getCorrelationId();
        long startTime = System.currentTimeMillis();
        if (StringUtils.isBlank(logProperties.getCategorySuffix())) {
            logProperties.setCategorySuffix(DEFAULT_CATEGORY_SUFFIX);
        }
        operations.process(
                result -> {
                    logger.log(setUpSuccessLogMessage(System.currentTimeMillis() - startTime,logProperties,timerName),
                            setUpMessageAttributes(startTime,timerName),
                            PropertiesFactory.exceptionPropertiesSupplier().get(),
                            additionalProperties, config, location, correlationId);
                    callback.success(result);
                },
                (error, previous) -> {
                    logger.log(setUpErrorLogMessage(System.currentTimeMillis() - startTime,logProperties,timerName),
                            setUpMessageAttributes(startTime,timerName),
                            PropertiesFactory.exceptionPropertiesSupplier().get()
                            , additionalProperties, config, location, correlationId);
                    callback.error(error);
                });
    }else {
        classLogger.error("CustomerLoggerConfiguration is null, this should have been injected during config start.");
        operations.process(
                callback::success,
                (error, previous) -> callback.error(error));
    }

  }

  private LogProperties setUpSuccessLogMessage(Long elapsedMilliseconds, LogProperties logProperties, String timerName ) {
      if (StringUtils.isBlank(logProperties.getMessage())) {
          logProperties.setMessage("Timer scope [" + timerName + "] completed in "
                  + elapsedMilliseconds + "ms");
      }
      return logProperties;
  }

    private LogProperties setUpErrorLogMessage(Long elapsedMilliseconds, LogProperties logProperties, String timerName ) {
        if (StringUtils.isBlank(logProperties.getMessage())) {
            logProperties.setMessage("Timer scope [" + timerName + "] completed with errors in "
                    + elapsedMilliseconds + "ms");
        }
        logProperties.setLevel(LogProperties.LogLevel.ERROR);
        return logProperties;
    }

  private MessageAttributes setUpMessageAttributes(Long elapsedMilliseconds, String timerName) {
      MessageAttributes messageAttributes = PropertiesFactory.messageAttributesSupplier().get();
      MessageAttribute timerNameAtt = new MessageAttribute(TIMER_NAME, timerName);
      MessageAttribute elapsed = new MessageAttribute(ELAPSED_TIME_MS, String.valueOf(elapsedMilliseconds));
      messageAttributes.getMessageAttributeList().add(timerNameAtt);
      messageAttributes.getMessageAttributeList().add(elapsed);
      return messageAttributes;
  }

}
