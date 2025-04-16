package com.avioconsulting.mule.logger.internal.listeners;

import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import java.util.Map;
import javax.xml.namespace.QName;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.notification.MessageProcessorNotification;
import org.mule.runtime.api.notification.MessageProcessorNotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Listener for Mule notifications on flow start, end and completion.
 */
public class CustomLoggerFlowRefNotificationListener
    extends CustomLoggerAbstractNotificationListener
    implements MessageProcessorNotificationListener<MessageProcessorNotification> {

  private final org.slf4j.Logger classLogger = LoggerFactory.getLogger(CustomLoggerFlowRefNotificationListener.class);

  private final QName QNAME_COMPONENT_PARAMS = QName.valueOf("{config}componentParameters");
  private final String FLOW_REF_CATEGORY_SUFFIX;

  public CustomLoggerFlowRefNotificationListener(CustomLoggerConfiguration config) {
    super(config);
    FLOW_REF_CATEGORY_SUFFIX = config.getFlowCategorySuffix().concat(".flow-ref");
  }

  @Override
  public Logger getClassLogger() {
    return classLogger;
  }

  @Override
  public void onNotification(MessageProcessorNotification notification) {
    classLogger.debug(
        "Received Notification [{}:{} for component {}]", notification.getClass().getName(),
        notification.getActionName(), notification.getComponent());
    if (config != null) {
      try {
        ComponentLocation location = notification.getComponent().getLocation();
        if (!"flow-ref".equalsIgnoreCase(location.getComponentIdentifier().getIdentifier().getName())) {
          return;
        }
        Map<String, String> params = (Map<String, String>) notification.getComponent()
            .getAnnotation(QNAME_COMPONENT_PARAMS);
        String flowRefTarget = params.get("name");
        String message = "Event not processed yet, this should never be shown";
        switch (Integer.parseInt(notification.getAction().getIdentifier())) {
          case MessageProcessorNotification.MESSAGE_PROCESSOR_PRE_INVOKE:
            message = "Flow-ref with target [" + flowRefTarget + "]" + " start";
            break;
          case MessageProcessorNotification.MESSAGE_PROCESSOR_POST_INVOKE:
            message = "Flow-ref with target [" + flowRefTarget + "]" + " end";
            break;
          default:
            classLogger
                .debug("Not a flow-ref pre or post event being processed, existing without logging.");
            return;
        }
        classLogger.debug(message);
        Map<String, String> flowLogAttributes = getFlowLogAttributes(notification);
        logMessage(location, notification.getEvent(), message, FLOW_REF_CATEGORY_SUFFIX,
            config.getFlowLogLevel(), flowLogAttributes);
      } catch (Exception e) {
        classLogger.error("Error processing flow notification", e);
      }
    } else {
      classLogger.warn(
          "Configuration hasn't been supplied to notification listener yet, flow logs won't be generated.");
    }
  }

}
