package com.avioconsulting.mule.logger.api.processor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.dsl.xml.ParameterDsl;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;

public class MessageAttributes {

  @Parameter
  @Optional(defaultValue = "#[vars.OTEL_TRACE_CONTEXT]")
  @Expression(ExpressionSupport.REQUIRED)
  @ParameterDsl(allowInlineDefinition = false, allowReferences = false)
  @DisplayName("OpenTelemetry Context")
  @Summary("The object that contains the open telemetry context variables to add the log message attributes. If you are using AVIO's Open Telemetry Module there is no more configuration necessary")
  private ParameterResolver<TypedValue<Object>> oTelContext;
  @Parameter
  @DisplayName("Message Attributes")
  @Optional
  @NullSafe
  @Summary("Discrete data elements you want to log as key value pairs.  Useful for adding data fields for reporting in log aggregation tools")
  private List<MessageAttribute> messageAttributes;

  private Object oTelContextObject;

  public MessageAttributes() {
    this.messageAttributes = new ArrayList<>();
  }

  public List<MessageAttribute> getAttributeList() {
    return this.messageAttributes;
  }

  public void addAttributes(Map<String, String> attributes) {
    attributes.forEach((key, value) -> messageAttributes.add(new MessageAttribute(key, value)));
  }

  public Map<String, String> getAttributes() {
    Map<String, String> attributes = new LinkedHashMap<>();
    if (messageAttributes != null) {
      for (MessageAttribute a : messageAttributes) {
        attributes.put(a.getKey(), a.getValue());
      }
    }
    return attributes;
  }

  public ParameterResolver<TypedValue<Object>> getOTelContext() {
    return oTelContext;
  }

  public void setOTelContext(ParameterResolver<TypedValue<Object>> oTelContext) {
    this.oTelContext = oTelContext;
  }

  public Object getOTelContextObject() {
    if (oTelContextObject != null) {
      return oTelContextObject;
    } else if (oTelContext != null) {
      return oTelContext.resolve().getValue();
    } else
      return null;
  }

  public void setOTelContextObject(Object oTelContextObject) {
    this.oTelContextObject = oTelContextObject;
  }

}
