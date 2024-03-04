package com.avioconsulting.mule.logger.internal;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import com.avioconsulting.mule.logger.api.processor.*;
import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import javax.inject.Inject;
import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.runtime.parameter.CorrelationInfo;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.route.Chain;
import org.slf4j.LoggerFactory;

public class CustomLoggerTimerScopeOperations {
  public static final String DEFAULT_CATEGORY_SUFFIX = "timer";
  private final org.slf4j.Logger classLogger = LoggerFactory.getLogger(CustomLoggerTimerScopeOperations.class);

  @Inject
  Registry registry;

  @Inject
  ExpressionManager expressionManager;

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
      @DisplayName("OpenTelemetry Context") @Optional(defaultValue = "#[vars.OTEL_TRACE_CONTEXT]") @Placement(tab = "Message Attributes") ParameterResolver<TypedValue<Object>> oTelContext,

      ComponentLocation location,
      CorrelationInfo correlationInfo,
      Chain operations,
      CompletionCallback<Object, Object> callback) {
    CustomLoggerConfiguration config = customLoggerRegistrationService.getConfig();
    if (config == null) {
      classLogger
          .error("CustomerLoggerConfiguration is null, this should have been injected during config start.");
      operations.process(
          result -> {
            callback.success(result);
          },
          (error, previous) -> {
            callback.error(error);
          });
    } else {
      CustomLogger logger = config.getLogger();
      ExceptionProperties exceptionProperties = new ExceptionProperties();
      MessageAttributes messageAttributes = new MessageAttributes();
      messageAttributes.setOTelContext(oTelContext);

      String correlationId = correlationInfo.getCorrelationId();
      long startTime = System.currentTimeMillis();
      MessageAttribute timerNameAtt = new MessageAttribute("timerName", timerName);
      messageAttributes.getAttributeList().add(timerNameAtt);
      if (logProperties.getCategorySuffix() == null || logProperties.getCategorySuffix().equals("")) {
        logProperties.setCategorySuffix(DEFAULT_CATEGORY_SUFFIX);
      }
      operations.process(
          result -> {
            long elapsedMilliseconds = System.currentTimeMillis() - startTime;
            MessageAttribute elapsed = new MessageAttribute("elapsedTimeMs",
                String.valueOf(elapsedMilliseconds));
            messageAttributes.getAttributeList().add(elapsed);
            if (logProperties.getMessage() == null || logProperties.getMessage().equals("")) {
              logProperties.setMessage(
                  "Timer scope [" + timerName + "] completed in " + elapsedMilliseconds + "ms");
            }
            logger.log(logProperties, messageAttributes, exceptionProperties, additionalProperties, config,
                location, correlationId);
            callback.success(result);
          },
          (error, previous) -> {
            long elapsedMilliseconds = System.currentTimeMillis() - startTime;
            MessageAttribute elapsed = new MessageAttribute("elapsedTimeMs",
                String.valueOf(elapsedMilliseconds));
            messageAttributes.getAttributeList().add(elapsed);
            if (logProperties.getMessage() == null || logProperties.getMessage().equals("")) {
              logProperties.setMessage("Timer scope [" + timerName + "] completed with errors in "
                  + elapsedMilliseconds + "ms");
            }
            logProperties.setLevel(LogProperties.LogLevel.ERROR);
            logger.log(logProperties, messageAttributes, exceptionProperties, additionalProperties, config,
                location, correlationId);
            callback.error(error);
          });
    }
  }

}
