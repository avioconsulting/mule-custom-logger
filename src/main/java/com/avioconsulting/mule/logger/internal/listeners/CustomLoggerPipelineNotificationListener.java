package com.avioconsulting.mule.logger.internal.listeners;

import com.avioconsulting.mule.logger.api.processor.FlowLogConfig;
import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.notification.PipelineMessageNotification;
import org.mule.runtime.api.notification.PipelineMessageNotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * Listener for Mule notifications on flow start, end and completion.
 */
public class CustomLoggerPipelineNotificationListener
    extends CustomLoggerAbstractNotificationListener
    implements PipelineMessageNotificationListener<PipelineMessageNotification> {

  private final org.slf4j.Logger classLogger = LoggerFactory
      .getLogger(CustomLoggerPipelineNotificationListener.class);

  public CustomLoggerPipelineNotificationListener(CustomLoggerConfiguration config) {
    super(config);
  }

  @Override
  public Logger getClassLogger() {
    return classLogger;
  }

  @Override
  public void onNotification(PipelineMessageNotification notification) {
    classLogger.debug(
        "Received Notification ["
            + notification.getClass().getName()
            + ":"
            + notification.getActionName()
            + "]");
    if (config != null) {
      try {
        String msgToAppend = "";
        Optional<Map.Entry<String, FlowLogConfig>> matchedEntry = config.getFlowLogConfigMap().entrySet()
            .stream()
            .filter(entry -> matchWildcard(entry.getKey(), notification.getResourceIdentifier()))
            .findFirst();
        if (matchedEntry.isPresent()) {
          FlowLogConfig flowLogConfig = matchedEntry.get().getValue();
          TypedValue<String> evaluate = (TypedValue<String>) config
              .getExpressionManager()
              .evaluate("#[" + flowLogConfig.getMessageExpressionText() + "]",
                  notification.getEvent().asBindingContext());
          msgToAppend = evaluate.getValue();
        }
        String message = "Event not processed yet, this should never be shown";
        switch (Integer.parseInt(notification.getAction().getIdentifier())) {
          case PipelineMessageNotification.PROCESS_START:
            message = "Flow [" + notification.getResourceIdentifier() + "]" + " start "
                + (msgToAppend != null ? msgToAppend : "");
            break;
          case PipelineMessageNotification.PROCESS_COMPLETE:
            message = "Flow [" + notification.getResourceIdentifier() + "]" + " end";
            break;
          default:
            classLogger
                .debug("Not a flow start or complete event being processed, existing without logging.");
            return;
        }
        classLogger.debug(message);
        Map<String, String> flowLogAttributes = getFlowLogAttributes(notification);
        logMessage(notification.getComponent().getLocation(), notification.getEvent(), message,
            config.getFlowCategorySuffix(),
            config.getFlowLogLevel(), flowLogAttributes);
      } catch (Exception e) {
        if (e.getClass().getName().equals("java.lang.ClassCastException")
            || e.getClass().getName().equals("ClassCastException")) {
          classLogger.error("Message expression text in flow-log-config needs to be a String", e);
        } else {
          classLogger.error("Error processing flow notification", e);
        }
      }
    } else {
      classLogger.warn(
          "Configuration hasn't been supplied to notification listener yet, flow logs won't be generated.");
    }
  }
}
