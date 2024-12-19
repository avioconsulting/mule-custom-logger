package com.avioconsulting.mule.logger.internal.listeners;

import com.avioconsulting.mule.logger.api.processor.*;
import com.avioconsulting.mule.logger.internal.CustomLogger;
import com.avioconsulting.mule.logger.internal.config.CustomLoggerConfiguration;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.notification.EnrichedServerNotification;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class CustomLoggerAbstractNotificationListener {
  protected final CustomLoggerConfiguration config;
  private Map<String, String> emptyAttributes = Collections.emptyMap();

  public CustomLoggerAbstractNotificationListener(CustomLoggerConfiguration config) {
    this.config = config;
  }

  protected abstract org.slf4j.Logger getClassLogger();

  protected void logMessage(ComponentLocation location, Event event, String logMessage, String categoryPrefix,
      LogProperties.LogLevel level, Map<String, String> additionalAttributes) {
    CustomLogger logger = config.getLogger();
    LogProperties logProperties = new LogProperties();
    MessageAttributes messageAttributes = new MessageAttributes();
    if (event.getVariables().get("OTEL_TRACE_CONTEXT") != null) {
      Object oTelContextObject = event.getVariables().get("OTEL_TRACE_CONTEXT")
          .getValue();
      messageAttributes.setOTelContextObject(oTelContextObject);
    }
    messageAttributes.addAttributes(additionalAttributes);
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

  protected Map<String, String> getFlowLogAttributes(EnrichedServerNotification notification) {
    Map<String, String> value = emptyAttributes;
    FlowLogConfig flowLogConfig;
    /**
     * Flow name can contain wildcard (*)
     * We only look for wildcard either starting of the string or ending of the
     * string
     * ex: mq-listener-* will look for all the flows that starts with mq-listener
     * ex: *-mq-flow will look for all the flows that ends with -mq-flow
     **/
    Optional<Map.Entry<String, FlowLogConfig>> matchedEntry = config.getFlowLogConfigMap().entrySet().stream()
        .filter(entry -> matchWildcard(entry.getKey(), notification.getResourceIdentifier()))
        .findFirst();
    if (matchedEntry.isPresent()) {
      flowLogConfig = matchedEntry.get().getValue();
      TypedValue<Map<String, String>> evaluate = (TypedValue<Map<String, String>>) config
          .getExpressionManager()
          .evaluate("#[" + flowLogConfig.getAttributesExpressionText() + "]",
              notification.getEvent().asBindingContext());
      value = evaluate.getValue();
      if (value == null)
        value = emptyAttributes;
    }
    return value;
  }

  public boolean matchWildcard(String wildcardKey, String searchString) {
    // Trim the wildcard key
    String cleanWildcardKey = wildcardKey.trim();

    // Exact match if no wildcards
    if (searchString.equalsIgnoreCase(wildcardKey.toLowerCase())) {
      return true;
    }

    // Handle start wildcard
    if (cleanWildcardKey.startsWith("*")) {
      String suffix = cleanWildcardKey.substring(1);
      return searchString.endsWith(suffix);
    }

    // Handle end wildcard
    if (cleanWildcardKey.endsWith("*")) {
      String prefix = cleanWildcardKey.substring(0, cleanWildcardKey.length() - 1);
      return searchString.startsWith(prefix);
    }

    // If wildcard key is just '*', match everything
    return cleanWildcardKey.equals("*");
  }

}