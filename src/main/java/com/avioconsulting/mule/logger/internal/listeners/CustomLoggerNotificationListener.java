package com.avioconsulting.mule.logger.internal.listeners;

import com.avioconsulting.mule.logger.api.processor.AdditionalProperties;
import com.avioconsulting.mule.logger.api.processor.ExceptionProperties;
import com.avioconsulting.mule.logger.api.processor.LogProperties;
import com.avioconsulting.mule.logger.api.processor.MessageAttributes;
import com.avioconsulting.mule.logger.internal.CustomLogger;
import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.notification.PipelineMessageNotification;
import org.mule.runtime.api.notification.PipelineMessageNotificationListener;
import org.slf4j.LoggerFactory;

/*
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
        classLogger.debug(
                "Received Notification ["
                        + notification.getClass().getName()
                        + ":"
                        + notification.getActionName()
                        + "]");
        try {
            ComponentLocation location = notification.getComponent().getLocation();
            CustomLogger logger = config.getLogger();
            LogProperties logProperties = new LogProperties();
            MessageAttributes messageAttributes = new MessageAttributes();
            ExceptionProperties exceptionProperties = new ExceptionProperties();
            AdditionalProperties additionalProperties = new AdditionalProperties();
            additionalProperties.setIncludeLocationInfo(true);
            String correlationId = notification.getEvent().getCorrelationId();
            if(config.getFlowCategorySuffix() != null && !config.getFlowCategorySuffix().equals("")) {
                logProperties.setCategorySuffix(config.getFlowCategorySuffix());
            }
            logProperties.setLevel(config.getFlowLogLevel());
            String logMessage = "Event not processed yet, this should never be shown";
            switch (Integer.parseInt(notification.getAction().getIdentifier())) {
                case PipelineMessageNotification.PROCESS_START:
                    logMessage = "Flow [" + notification.getResourceIdentifier() + "]" + " start";
                    break;
                case PipelineMessageNotification.PROCESS_COMPLETE:
                    logMessage = "Flow [" + notification.getResourceIdentifier() + "]" + " end";
                    break;
                default:
                    classLogger.debug("Not a flow start or complete event being processed, existing without logging.");
                    return;
            }
            classLogger.debug(logMessage);
            logProperties.setMessage(logMessage);
            logger.log(logProperties, messageAttributes, exceptionProperties, additionalProperties, config, location, correlationId);
        } catch (Exception e) {
            classLogger.error("Error processing flow notification", e);
        }
    }
}
