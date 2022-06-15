package com.avioconsulting.mule.logger.api.processor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class MessageAttributes {

  @Parameter
  @DisplayName("Message Attributes")
  @Optional
  @NullSafe
  @Summary("Discrete data elements you want to log as key value pairs.  Useful for adding data fields for reporting in log aggregation tools")
  private List<MessageAttribute> messageAttributes;

  public MessageAttributes() {
    this.messageAttributes = new ArrayList<>();
  }

  public List<MessageAttribute> getAttributeList() {
    return this.messageAttributes;
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
}
