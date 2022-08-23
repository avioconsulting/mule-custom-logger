package com.avioconsulting.mule.logger.internal.listeners;

import com.avioconsulting.mule.logger.api.processor.AdditionalProperties;
import com.avioconsulting.mule.logger.api.processor.LogProperties;
import com.avioconsulting.mule.logger.api.processor.facade.PropertiesFactory;
import com.avioconsulting.mule.logger.internal.CustomLogger;
import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.notification.PipelineMessageNotification;
import org.mule.runtime.api.notification.PipelineMessageNotificationListener;
import org.slf4j.LoggerFactory;

/**
 * Listener for Mule notifications on flow start, end and completion.
 */
public class CustomLoggerNotificationListener
    implements PipelineMessageNotificationListener<PipelineMessageNotification> {

  private final org.slf4j.Logger classLogger = LoggerFactory.getLogger(CustomLoggerNotificationListener.class);
  private final CustomLoggerConfiguration config;

  public CustomLoggerNotificationListener(CustomLoggerConfiguration config) {
    this.config = config;
  }

  @Override
  public void onNotification(PipelineMessageNotification notification) {
    classLogger.debug("Received Notification [{}:{}]", notification.getClass().getName(), notification.getActionName());
    if (config != null) {
      try {
        CustomLogger logger = config.getCustomLogger();
        String logMessage = resolveLogMessageFromAction(notification);
        classLogger.debug(logMessage);
        logger.log(setUpLogProperties(logMessage),
                PropertiesFactory.messageAttributesSupplier().get(),
                PropertiesFactory.exceptionPropertiesSupplier().get(),
                setUpAdditionalProperties(),
                config,
                resolveComponentLocation(notification),
                resolveCorrelationId(notification));
      } catch (Exception e) {
        classLogger.error("Error processing flow notification", e);
      }
    } else {
      classLogger.warn(
          "Configuration hasn't been supplied to notification listener yet, flow logs won't be generated.");
    }
  }

  private AdditionalProperties setUpAdditionalProperties(){
    AdditionalProperties additionalProperties = PropertiesFactory.additionalPropertiesSupplier().get();
    additionalProperties.setIncludeLocationInfo(true);
    return additionalProperties;
  }

  private ComponentLocation resolveComponentLocation(PipelineMessageNotification notification){
    return notification.getComponent().getLocation();
  }

  private String resolveCorrelationId(PipelineMessageNotification notification){
    return notification.getEvent().getCorrelationId();
  }

  private LogProperties setUpLogProperties(String logMessage) {
    LogProperties logProperties = PropertiesFactory.logPropertiesSupplier().get();
    if (StringUtils.isNotBlank(config.getFlowCategorySuffix())) {
      logProperties.setCategorySuffix(config.getFlowCategorySuffix());
    }
    logProperties.setLevel(config.getFlowLogLevel());
    logProperties.setMessage(logMessage);
    return logProperties;
  }

  private String resolveLogMessageFromAction(PipelineMessageNotification notification) {
    String logMessage = "Event not processed yet, this should never be shown";
    switch (Integer.parseInt(notification.getAction().getIdentifier())) {
      case PipelineMessageNotification.PROCESS_START:
        logMessage = String.format("Flow [%s] start", notification.getResourceIdentifier());
        break;
      case PipelineMessageNotification.PROCESS_COMPLETE:
        logMessage = String.format("Flow [%s] end", notification.getResourceIdentifier());
        break;
      default:
        classLogger.debug("Not a flow start or complete event being processed, existing without logging.");
    }
    return logMessage;
  }

}
